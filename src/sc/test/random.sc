(pre-include "stdio.h" "inttypes.h"
  "sph/test.h" "sph/float.h" "sph/float.c" "sph/random.h" "sph/random.c")

(define (test-random) status-t
  status-declare
  (declare s sph-random-state-t out (array double 200) out-u64 (array uint64-t 1000))
  (sc-comment "f64")
  (set s (sph-random-state-new 80))
  (sph-random-f64-array &s 100 out)
  (sph-random-f64-array &s 100 (+ 100 out))
  (test-helper-assert "f64" (sph-f64-nearly-equal 0.153695 (array-get out 199) 1.0e-4))
  (sc-comment "u64")
  (set s (sph-random-state-new 80))
  (sph-random-u64-array &s 100 out-u64)
  (test-helper-assert "u64" (= 16312392477912876050u (array-get out-u64 99)))
  (sc-comment "u64 bounded")
  (set s (sph-random-state-new 80))
  (sph-random-u64-bounded-array &s 10 100 out-u64)
  (test-helper-assert "u64 bounded 1" (= 0 (array-get out-u64 97)))
  (test-helper-assert "u64 bounded 2" (= 9 (array-get out-u64 61)))
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-random)
  (label exit test-helper-display-summary (return status.id)))