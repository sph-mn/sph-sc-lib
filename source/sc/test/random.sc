(pre-include "stdio.h" "../main/types.c"
  "./test.c" "../main/float.c" "../main/random.h" "../main/random.c")

(define (test-random) status-t
  status-declare
  (declare s sph-random-state-t out (array f64 200))
  (set s (sph-random-state-new 80))
  (sph-random &s 100 out)
  (sph-random &s 100 (+ 100 out))
  ;(for ((define i u32 0) (< i 200) (set i (+ 1 i))) (printf "%f " (array-get out i)))
  (test-helper-assert "value" (f64-nearly-equal 0.945766 (array-get out 199) 1.0e-4))
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-random)
  (label exit (test-helper-display-summary) (return status.id)))