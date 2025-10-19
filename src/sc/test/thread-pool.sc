(pre-include "inttypes.h" "sph/test.h"
  "sph/queue.h" "sph/thread-pool.h" "sph/thread-pool.c" "sph/futures.h" "sph/futures.c")

(define (test-thread-pool) status-t
  status-declare
  (declare pool sph-thread-pool-t)
  (test-helper-assert "thread-pool new" (not (sph-thread-pool-new 10 &pool)))
  (test-helper-assert "thread-pool finish" (not (sph-thread-pool-finish &pool 0 0)))
  (label exit status-return))

(define (future-work data) (void* void*)
  (declare a uint8-t*)
  (set a (malloc (sizeof uint8-t)) (pointer-get a) (+ 2 (pointer-get (convert-type data uint8-t*))))
  (return a))

(define (test-futures) status-t
  status-declare
  (declare future sph-future-t data uint8-t result uint8-t*)
  (set data 8)
  (test-helper-assert "future-init" (not (sph-future-init 10)))
  (sph-future-new future-work &data &future)
  (set result (convert-type (sph-future-touch &future) uint8-t*))
  (test-helper-assert "touch result" (= (+ 2 data) *result))
  (free result)
  (sph-future-deinit)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-futures)
  (test-helper-test-one test-thread-pool)
  (label exit (test-helper-display-summary) (return status.id)))