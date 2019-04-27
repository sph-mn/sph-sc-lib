(pre-include "inttypes.h" "pthread.h")

(pre-define
  thread-pool-size-t uint8-t
  thread-pool-thread-limit 128)

(declare
  thread-pool-t
  (type
    (struct
      (queue queue-t)
      (queue-mutex pthread-mutex-t)
      (queue-not-empty pthread-cond-t)
      (size thread-pool-size-t)
      (threads (array pthread-t thread-pool-thread-limit))))
  thread-pool-task-t
  (type
    (struct
      (q queue-node-t)
      (f (function-pointer boolean)))))

(define (thread-pool-enqueue a task) (void thread-pool-t* thread-pool-task-t)
  (pthread-mutex-lock a.queue-mutex)
  (queue-enq &a.queue task)
  (pthread-cond-signal a.queue-not-empty)
  (pthread-mutex-unlock a.queue-mutex))

(define (thread-pool-worker a) (void thread-pool-t*)
  (declare task thread-pool-task-t)
  (label get-task
    (pthread-mutex-lock a.queue-mutex)
    (label wait
      (sc-comment "considers so-called spurious wakeups")
      (if (queue-is-empty a.queue)
        (begin
          (pthread-cond-wait a.queue-not-empty a.queue-mutex)
          (goto wait))
        (set task (queue-get (queue-deq a.queue) thread-pool-task-t q))))
    (pthread-mutex-unlock a.queue-mutex)
    (if (task.f) (goto get-task)
      (pthread-exit))))

(define (thread-pool-new size out-thread-pool) (void thread-pool-size-t thread-pool-t*)
  (declare
    a attr thread-pool-t)
  (set a.size size)
  (queue-init &a.queue)
  (pthread-mutex-init a.queue-mutex)
  (pthread-cond-init a.queue-not-empty)
  (sc-comment
    "explicitly create threads as joinable so thread-pool-finish can work"
    "even if an implementation creates non-joinable threads by default")
  (pthread-attr-init &attr)
  (pthread-attr-setdetachstate &attr PTHREAD-CREATE-JOINABLE)
  (while size
    (set size (- size 1))
    (pthread-create (+ size a.threads) attr thread-pool-worker (convert-type out-thread-pool void*)))
  (pthread-attr-destroy &attr))

(define (thread-finish) (boolean)
  "returning false instructs a thread in the pool to exit"
  (return #f))

(define (thread-pool-finish a) (status-t thread-pool-t*)
  status-declare
  (declare
    i thread-pool-size-t
    size thread-pool-size-t
    exit-status void*)
  (set size a:size)
  (for ((set i 0) (< size) (set (+ 1 i)))
    (thread-pool-enqueue a thread-finish))
  (for ((set i 0) (< size) (set (+ 1 i)))
    (set status.id (pthread-join (array-get a.threads i) exit-status)))
  (return status))

(define (thread-pool-destroy a) (void thread-pool-t*)
  (pthread-cond-destroy a.queue-not-empty)
  (pthread-mutex-destroy a.queue-mutex))