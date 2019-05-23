(pre-include "stdio.h" "inttypes.h"
  "./test.c" "../main/float.c" "../main/types.c" "../main/random.c")

(define (test-random) status-t
  status-declare
  (declare s random-state-t out (array f64 200))
  (set s (random-state-new 80))
  (random &s 100 out)
  (random &s 100 (+ 100 out))
  ;(for ((define i u32 0) (< i 200) (set i (+ 1 i))) (printf "%f " (array-get out i)))
  (test-helper-assert "value" (f64-nearly-equal 0.945766 (array-get out 199) 1.0e-4))
  (label exit (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-random)
  (label exit (test-helper-display-summary) (return status.id)))