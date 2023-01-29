(pre-include-guard-begin sph-thread-pool-h)
(pre-include "pthread.h")

(sc-comment
  "thread-pool that uses pthread condition variables to pause unused threads.
   based on the design of thread-pool.scm from sph-lib which has been stress tested in servers and digital signal processing.
   depends on queue.h")

(pre-include "inttypes.h")
(pre-define-if-not-defined sph-thread-pool-size-t uint8-t sph-thread-pool-thread-limit 128)

(declare
  sph-thread-pool-t
  (type
    (struct
      (queue sph-queue-t)
      (queue-mutex pthread-mutex-t)
      (queue-not-empty pthread-cond-t)
      (size sph-thread-pool-size-t)
      (threads (array pthread-t sph-thread-pool-thread-limit))))
  sph-thread-pool-task-t struct
  sph-thread-pool-task-t
  (type
    (struct
      sph-thread-pool-task-t
      (q sph-queue-node-t)
      (f (function-pointer void (struct sph-thread-pool-task-t*)))
      (data void*)))
  sph-thread-pool-task-f-t (type (function-pointer void (struct sph-thread-pool-task-t*)))
  (sph-thread-pool-destroy a) (void sph-thread-pool-t*)
  (sph-thread-finish task) (void sph-thread-pool-task-t*)
  (sph-thread-pool-enqueue a task) (void sph-thread-pool-t* sph-thread-pool-task-t*)
  (sph-thread-pool-finish a no-wait discard-queue) (int sph-thread-pool-t* uint8-t uint8-t)
  (sph-thread-pool-worker a) (void* sph-thread-pool-t*)
  (sph-thread-pool-new size a) (int sph-thread-pool-size-t sph-thread-pool-t*))

(pre-include-guard-end)