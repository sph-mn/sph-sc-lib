(pre-include "stdio.h" "inttypes.h" "./test.c" "../main/float.c" "../main/random.c")

(define (test-random) status-t
  status-declare
  (declare s sph-random-state-t out (array double 200) out-u64 (array uint64-t 1000))
  (sc-comment "f64")
  (set s (sph-random-state-new 80))
  (sph-random-f64 &s 100 out)
  (sph-random-f64 &s 100 (+ 100 out))
  (test-helper-assert "value" (f64-nearly-equal 0.945766 (array-get out 199) 1.0e-4))
  (sc-comment "u64")
  (set s (sph-random-state-new 80))
  (sph-random-u64 &s 100 out-u64)
  (test-helper-assert "value" (= 16312392477912876050u (array-get out-u64 99)))
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-random)
  (label exit (test-helper-display-summary) (return status.id)))