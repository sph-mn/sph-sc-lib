(pre-include-guard-begin sph-futures-h-included)

(sc-comment
  "fine-grain parallelism based on sph/thread-pool.c."
  "provides task objects with functions executed in threads that can be waited for to get a result value."
  "manages the memory of thread-pool task objects")

(pre-include "inttypes.h" "time.h" "stdatomic.h" "sph/thread-pool.h")
(pre-define-if-not-defined sph-future-default-poll-interval (struct-literal 0 200000000))

(declare
  sph-future-f-t (type (function-pointer void* void*))
  sph-future-t
  (type (struct (task sph-thread-pool-task-t) (finished (_Atomic uint8-t)) (f sph-future-f-t)))
  (sph-future-init thread-count) (int sph-thread-pool-size-t)
  (sph-future-eval task) (void sph-thread-pool-task-t*)
  (sph-future-new f data out) (void sph-future-f-t void* sph-future-t*)
  (sph-future-deinit) void
  (sph-future-touch a) (void* sph-future-t*))

(pre-include-guard-end)
