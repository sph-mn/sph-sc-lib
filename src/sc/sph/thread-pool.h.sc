(pre-include-guard-begin sph-thread-pool-h)
(pre-include "pthread.h" "inttypes.h")

(sc-comment
  "thread-pool that uses pthread condition variables to pause unused threads.
   based on the design of thread-pool.scm from sph-lib which has been stress tested in servers and digital signal processing.
   depends on queue.h")

(pre-define-if-not-defined sph-thread-pool-size-t uint8-t sph-thread-pool-thread-limit 128)

(sc-comment
  "each task object must remain valid for the entire duration of its function call.
   freeing or reusing the object is permitted only after the function returns.
   task functions must not call pthread_exit directly except through sph_thread_finish")

(pre-if-not-defined sph-thread-pool-task-t-defined
  (declare
    sph-thread-pool-task-t struct
    sph-thread-pool-task-t
    (type
      (struct
        sph-thread-pool-task-t
        (q sph-queue-node-t)
        (f (function-pointer void (struct sph-thread-pool-task-t*)))
        (data void*)))))

(sc-comment
  "the pool structure is opaque to the caller.
   fields must not be read or modified without holding the internal mutex")

(declare
  sph-thread-pool-t
  (type
    (struct
      (queue sph-queue-t)
      (queue-mutex pthread-mutex-t)
      (queue-not-empty pthread-cond-t)
      (size sph-thread-pool-size-t)
      (threads (array pthread-t sph-thread-pool-thread-limit))
      (accepting uint8-t)
      (shutdown uint8-t)))
  sph-thread-pool-task-f-t (type (function-pointer void (struct sph-thread-pool-task-t*))))

(sc-comment
  "the task function must not enqueue this same task again until it returns.
   any synchronization on the tasks data field is the callers responsibility.")

(declare (sph-thread-pool-worker a) (void* void*))

(sc-comment
  "returns zero when successful and a non-zero pthread error code otherwise.
   on nonzero return the pool may be only partially initialized. The caller must invoke finish with no_wait set to true to release all resources")

(declare (sph-thread-pool-new size a) (int sph-thread-pool-size-t sph-thread-pool-t*))
(sc-comment "this is a special task that exits the thread it is being executed in")
(declare (sph-thread-finish task) (void sph-thread-pool-task-t*))

(sc-comment
  "completes all enqueued tasks, closes worker threads, and frees resources unless no_wait is true.
   if no_wait is true, the call returns immediately and remaining tasks may continue running in the background.
   may be invoked again later with no_wait set to false to wait for all threads to exit cleanly.
   must not be called concurrently with destroy or resize. the caller must not enqueue new tasks once this call begins.
   if discard_queue is true, all pending tasks are dropped without execution, and the caller is responsible for any necessary cleanup")

(declare (sph-thread-pool-finish a no-wait discard-queue) (int sph-thread-pool-t* uint8-t uint8-t))

(sc-comment
  "requires a non-null task with a valid function pointer. the pool must still be accepting work.
   each task node may be enqueued only once and not reused until its function returns.
   if EBUSY is returned, the pool is no longer accepting work")

(declare (sph-thread-pool-enqueue a task) (int sph-thread-pool-t* sph-thread-pool-task-t*))

(sc-comment
  "requires the pool to be fully shut down and contain no active threads.
   caller must not invoke concurrently with resize or other control operations")

(declare (sph-thread-pool-destroy a) (int sph-thread-pool-t*))

(sc-comment
  "must not be called concurrently with destroy or another resize.
   the caller must not enqueue new tasks while resizing to zero.
   when increasing size, existing tasks may complete on either the old or new workers.
   if discard_queue is nonzero, the caller accepts that pending tasks are dropped and must perform any necessary cleanup")

(declare (sph-thread-pool-resize a size no-wait discard-queue)
  (int sph-thread-pool-t* sph-thread-pool-size-t uint8-t uint8-t))

(pre-include-guard-end)
