(pre-include "pthread.h" "sph/thread-pool.h")

(define (sph-thread-pool-destroy a) (void sph-thread-pool-t*)
  (pthread-cond-destroy &a:queue-not-empty)
  (pthread-mutex-destroy &a:queue-mutex))

(define (sph-thread-finish task) (void sph-thread-pool-task-t*)
  "if enqueued call pthread-exit to end the thread it was dequeued in"
  (free task)
  (pthread-exit 0))

(define (sph-thread-pool-enqueue a task) (void sph-thread-pool-t* sph-thread-pool-task-t*)
  "add a task to be processed by the next free thread.
   mutexes are used so that the queue is only ever accessed by a single thread"
  (pthread-mutex-lock &a:queue-mutex)
  (sph-queue-enq &a:queue &task:q)
  (pthread-cond-signal &a:queue-not-empty)
  (pthread-mutex-unlock &a:queue-mutex))

(define (sph-thread-pool-finish a no-wait discard-queue) (int sph-thread-pool-t* uint8-t uint8-t)
  "let threads complete all currently enqueued tasks, close the threads and free resources unless no_wait is true.
   if no_wait is true then the call is non-blocking and threads might still be running until they finish the queue after this call.
   thread_pool_finish can be called again without no_wait. with only no_wait thread_pool_destroy will not be called
   and it is unclear when it can be used to free some final resources.
   if discard_queue is true then the current queue is emptied, but note
   that if enqueued tasks free their task object these tasks wont get called anymore"
  (declare
    exit-value void*
    i sph-thread-pool-size-t
    size sph-thread-pool-size-t
    task sph-thread-pool-task-t*)
  (if discard-queue
    (begin
      (pthread-mutex-lock &a:queue-mutex)
      (sph-queue-init &a:queue)
      (pthread-mutex-unlock &a:queue-mutex)))
  (set size a:size)
  (for ((set i 0) (< i size) (set i (+ 1 i)))
    (set task (malloc (sizeof sph-thread-pool-task-t)))
    (if (not task) (return 1))
    (set task:f sph-thread-finish)
    (sph-thread-pool-enqueue a task))
  (if (not no-wait)
    (for ((set i 0) (< i size) (set i (+ 1 i)))
      (if (= 0 (pthread-join (array-get a:threads i) &exit-value))
        (begin (set a:size (- a:size 1)) (if (= 0 a:size) (sph-thread-pool-destroy a))))))
  (return 0))

(define (sph-thread-pool-worker a) (void* sph-thread-pool-t*)
  "internal worker routine"
  (declare task sph-thread-pool-task-t*)
  (label get-task
    (pthread-mutex-lock &a:queue-mutex)
    (label wait
      (sc-comment "considers so-called spurious wakeups")
      (if a:queue.size (set task (sph-queue-get (sph-queue-deq &a:queue) sph-thread-pool-task-t q))
        (begin (pthread-cond-wait &a:queue-not-empty &a:queue-mutex) (goto wait))))
    (pthread-mutex-unlock &a:queue-mutex)
    (task:f task)
    (goto get-task)))

(define (sph-thread-pool-new size a) (int sph-thread-pool-size-t sph-thread-pool-t*)
  "returns zero when successful and a non-zero pthread error code otherwise"
  (declare i sph-thread-pool-size-t attr pthread-attr-t error int)
  (set error 0)
  (sph-queue-init &a:queue)
  (pthread-mutex-init &a:queue-mutex 0)
  (pthread-cond-init &a:queue-not-empty 0)
  (pthread-attr-init &attr)
  (pthread-attr-setdetachstate &attr PTHREAD-CREATE-JOINABLE)
  (for ((set i 0) (< i size) (set i (+ 1 i)))
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