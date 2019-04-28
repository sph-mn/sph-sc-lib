(sc-comment
  "fine-grain parallelism based on sph/thread-pool.c."
  "provides task objects with functions executed in a thread-pool that can be waited for to get a result value."
  "manages the memory of thread-pool task objects." "thread-pool.c must be included beforehand")

(sc-comment "for usleep")
(pre-include "unistd.h")
(define sph-futures-pool-is-initialised uint8-t #f)

(declare
  sph-futures-pool thread-pool-t
  future-f-t (type (function-pointer void* void*))
  future-t
  (type
    (struct
      (task thread-pool-task-t)
      (finished uint8-t)
      (f future-f-t))))

(define (future-init thread-count) (int thread-pool-size-t)
  "call once to initialise the future thread pool that persists for
  the whole process or until future-deinit is called.
  can be called multiple times and just returns if the thread pool already exists.
  returns zero on success"
  (declare status int)
  (if sph-futures-pool-is-initialised (return 0)
    (begin
      (set status (thread-pool-new thread-count &sph-futures-pool))
      (if (= 0 status) (set sph-futures-pool-is-initialised #t))
      (return status))))

(define (future-eval task) (void thread-pool-task-t*)
  "internal future worker.
  returns true to keep thread running.
  a->f returns because modifying data likely needs extra type conversions inside a->f.
  thread-pool does not have a finished field by default so that tasks can themselves free
  their object when they finish"
  (declare a future-t*)
  (set
    a (convert-type (- (convert-type task char*) (offsetof future-t task)) future-t*)
    task:data (a:f task:data)
    a:finished #t))

(define (future-new f data) (future-t* future-f-t void*)
  "return a new futures object or zero if memory allocation failed.
  the given function receives data as its sole argument"
  (declare a future-t*)
  (set a (malloc (sizeof future-t)))
  (if (not a) (return 0))
  (set
    a:finished #f
    a:f f
    a:task.f future-eval
    a:task.data data)
  (thread-pool-enqueue &sph-futures-pool &a:task)
  (return a))

(define (future-deinit) void
  "can be called to stop and free the main thread-pool.
  waits till all active futures are finished"
  (thread-pool-finish &sph-futures-pool 0 0)
  (thread-pool-destroy &sph-futures-pool))

(define (touch a) (void* future-t*)
  "blocks until future is finished and returns its result"
  (declare result void*)
  (label loop
    (if a:finished
      (begin
        (set result a:task.data)
        (free a)
        (return result))
      (begin
        (sc-comment "poll five times per second")
        (usleep 20000)
        (goto loop)))))