(pre-include-guard-begin sph-random-h)
(pre-include "inttypes.h")

(declare
  sph-random-state-t (type (struct (data (array uint64-t 4))))
  (sph-random-state-new seed) (sph-random-state-t uint64-t)
  (sph-random-u64 state) (uint64-t sph-random-state-t*)
  (sph-random-u64-bounded state range) (uint64-t sph-random-state-t* uint64-t)
  (sph-random-f64-bounded state range) (double sph-random-state-t* double)
  (sph-random-f64 state) (double sph-random-state-t*)
  (sph-random-u64-array state size out) (void sph-random-state-t* size-t uint64-t*)
  (sph-random-u64-bounded-array state range size out)
  (void sph-random-state-t* uint64-t size-t uint64-t*)
  (sph-random-f64-array state size out) (void sph-random-state-t* size-t double*)
  (sph-random-f64-bounded-array state range size out)
  (void sph-random-state-t* double size-t double*)
  (sph-random-u32 state) (uint32-t sph-random-state-t*)
  (sph-random-u32-bounded state range) (uint32-t sph-random-state-t* uint32-t)
  (sph-random-u32-array state size out) (void sph-random-state-t* size-t uint32-t*)
  (sph-random-u32-bounded-array state range size out)
  (void sph-random-state-t* uint32-t size-t uint32-t*)
  (sph-random-f64-1to1 state) (double sph-random-state-t*)
  (sph-random-f64-array-1to1 state size out) (void sph-random-state-t* size-t double*)
  (sph-random-f64-0to1 state) (double sph-random-state-t*)
  (sph-random-f64-array-0to1 state size out) (void sph-random-state-t* size-t double*))

(pre-include-guard-end)