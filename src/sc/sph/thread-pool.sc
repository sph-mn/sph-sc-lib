(pre-include-guard-begin sph-thread-pool-c-included)
(pre-include "errno.h" "stdlib.h" "sph/thread-pool.h")

(define (sph-thread-pool-destroy a) (int sph-thread-pool-t*)
  (define result int 0)
  (pthread-mutex-lock (address-of a:queue-mutex))
  (if (or a:size (not a:shutdown)) (begin (pthread-mutex-unlock &a:queue-mutex) (return EBUSY)))
  (pthread-mutex-unlock (address-of a:queue-mutex))
  (pthread-cond-destroy (address-of a:queue-not-empty))
  (pthread-mutex-destroy (address-of a:queue-mutex))
  (return result))

(define (sph-thread-pool-enqueue a task) (int sph-thread-pool-t* sph-thread-pool-task-t*)
  (define result int 0)
  (if (not task) (return EINVAL))
  (if (not task:f) (return EINVAL))
  (pthread-mutex-lock &a:queue-mutex)
  (if (not a:accepting) (begin (pthread-mutex-unlock &a:queue-mutex) (return EBUSY)))
  (sph-queue-enq &a:queue &task:q)
  (pthread-mutex-unlock &a:queue-mutex)
  (pthread-cond-signal &a:queue-not-empty)
  (return result))

(define (sph-thread-pool-worker b) (void* void*)
  (define a sph-thread-pool-t* b task sph-thread-pool-task-t* 0)
  (while 1
    (pthread-mutex-lock &a:queue-mutex)
    (while (not (or a:queue.size a:shutdown)) (pthread-cond-wait &a:queue-not-empty &a:queue-mutex))
    (if (and (not a:queue.size) a:shutdown)
      (begin (pthread-mutex-unlock &a:queue-mutex) (return 0)))
    (set task (sph-queue-get (sph-queue-deq &a:queue) sph-thread-pool-task-t q))
    (pthread-mutex-unlock &a:queue-mutex)
    (task:f task)))

(define (sph-thread-pool-request-shutdown a) ((static void) sph-thread-pool-t*)
  "must be called only by serialized control logic.
   sets accepting to false, marks the pool as shutting down, and wakes all waiting threads."
  (pthread-mutex-lock &a:queue-mutex)
  (set a:accepting 0 a:shutdown 1)
  (pthread-mutex-unlock &a:queue-mutex)
  (pthread-cond-broadcast &a:queue-not-empty))

(define (sph-thread-finish task) (void sph-thread-pool-task-t*) (free task) (pthread-exit 0))

(define (sph-thread-pool-finish a no-wait discard-queue) (int sph-thread-pool-t* uint8-t uint8-t)
  (return (sph-thread-pool-resize a 0 no-wait discard-queue)))

(define (sph-thread-pool-new size a) (int sph-thread-pool-size-t sph-thread-pool-t*)
  (declare i sph-thread-pool-size-t attr pthread-attr-t error int)
  (set error 0)
  (sph-queue-init &a:queue)
  (pthread-mutex-init &a:queue-mutex 0)
  (pthread-cond-init &a:queue-not-empty 0)
  (set a:accepting 1 a:shutdown 0)
  (pthread-attr-init &attr)
  (pthread-attr-setdetachstate &attr PTHREAD-CREATE-JOINABLE)
  (for ((set i 0) (< i size) (set+ i 1))
    (set error
      (pthread-create (+ i a:threads) &attr
        (convert-type sph-thread-pool-worker (function-pointer void* void*)) (convert-type a void*)))
    (if error
      (begin
        (if (< 0 i)
          (begin
            (sc-comment "try to finish previously created threads")
            (set a:size i)
            (sph-thread-pool-finish a #t 0)))
        (goto exit))))
  (set a:size size)
  (label exit (pthread-attr-destroy &attr) (return error)))

(define (sph-thread-pool-resize a size no-wait discard-queue)
  (int sph-thread-pool-t* sph-thread-pool-size-t uint8-t uint8-t)
  (declare
    attr pthread-attr-t
    join-value void*
    i sph-thread-pool-size-t
    task sph-thread-pool-task-t*)
  (define error-code int 0)
  (if (> size sph-thread-pool-thread-limit) (return EINVAL))
  (if (> size a:size)
    (begin
      (pthread-attr-init &attr)
      (pthread-attr-setdetachstate &attr PTHREAD-CREATE-JOINABLE)
      (set i a:size)
      (while (< i size)
        (if (pthread-create (+ a:threads i) &attr sph-thread-pool-worker a)
          (begin
            (pthread-mutex-lock &a:queue-mutex)
            (set a:size i)
            (pthread-mutex-unlock &a:queue-mutex)
            (set error-code EAGAIN i size))
          (set+ i 1)))
      (pthread-attr-destroy &attr)
      (pthread-mutex-lock &a:queue-mutex)
      (set a:accepting 1 a:shutdown 0 a:size size)
      (pthread-mutex-unlock &a:queue-mutex)
      (return error-code)))
  (if (not size) (sph-thread-pool-request-shutdown a))
  (if discard-queue
    (begin
      (pthread-mutex-lock &a:queue-mutex)
      (sph-queue-init &a:queue)
      (pthread-mutex-unlock &a:queue-mutex)
      (pthread-cond-broadcast &a:queue-not-empty)))
  (if size
    (begin
      (set i size)
      (while (< i a:size)
        (set task (malloc (sizeof sph-thread-pool-task-t)))
        (if (not task) (return ENOMEM))
        (set task:f sph-thread-finish)
        (if (sph-thread-pool-enqueue a task) (begin (free task) (return EBUSY)))
        (set+ i 1))))
  (if (not no-wait)
    (begin
      (set i size)
      (while (< i a:size) (pthread-join (array-get a:threads i) &join-value) (set+ i 1))
      (pthread-mutex-lock &a:queue-mutex)
      (set a:size size)
      (pthread-mutex-unlock &a:queue-mutex)
      (if (not size) (sph-thread-pool-destroy a)))
    (begin
      (pthread-mutex-lock &a:queue-mutex)
      (set a:size size)
      (pthread-mutex-unlock &a:queue-mutex)))
  (return 0))

(pre-include-guard-end)
