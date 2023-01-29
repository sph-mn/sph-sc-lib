(sc-comment "depends thread-pool.c")
(declare sph-futures-pool sph-thread-pool-t)
(define sph-futures-pool-is-initialized uint8-t #f)

(define (sph-future-init thread-count) (int sph-thread-pool-size-t)
  "call once to initialize the future thread pool that persists for
   the whole process or until sph-future-deinit is called.
   can be called multiple times and just returns if the thread pool already exists.
   returns zero on success"
  (declare status int)
  (if sph-futures-pool-is-initialized (return 0)
    (begin
      (set status (sph-thread-pool-new thread-count &sph-futures-pool))
      (if (= 0 status) (set sph-futures-pool-is-initialized #t))
      (return status))))

(define (sph-future-eval task) (void sph-thread-pool-task-t*)
  "internal future worker.
   a->f returns because modifying data likely needs extra type conversions inside a->f.
   thread-pool does not have a finished field by default so that tasks can themselves free
   their object when they finish"
  (declare a sph-future-t*)
  (set
    a (convert-type (- (convert-type task uint8-t*) (offsetof sph-future-t task)) sph-future-t*)
    task:data (a:f task:data)
    a:finished #t))

(define (sph-future-new f data out) (void sph-future-f-t void* sph-future-t*)
  "prepare a future in out and possibly start evaluation in parallel.
   the given function receives data as its sole argument"
  (set out:finished #f out:f f out:task.f sph-future-eval out:task.data data)
  (sph-thread-pool-enqueue &sph-futures-pool &out:task))

(define (sph-future-deinit) void
  "can be called to stop and free the main thread-pool.
   waits till all active futures are finished"
  (sph-thread-pool-finish &sph-futures-pool 0 0)
  (sph-thread-pool-destroy &sph-futures-pool))

(define (sph-future-touch a) (void* sph-future-t*)
  "blocks until future is finished and returns its result"
  (define poll-interval (const struct timespec) sph-future-default-poll-interval)
  (while (not a:finished) (nanosleep &poll-interval 0))
  (return a:task.data))