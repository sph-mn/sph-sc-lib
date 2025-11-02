(pre-include-guard-begin sph-float-c-included)
(pre-include "math.h" "sph/float.h")

(define (sph-f64-nearly-equal a b margin) (uint8-t double double double)
  "approximate float comparison. margin is a factor and is low for low accepted differences"
  (return (< (fabs (- a b)) margin)))

(define (sph-f32-nearly-equal a b margin) (uint8-t float float float)
  "approximate float comparison. margin is a factor and is low for low accepted differences"
  (return (< (fabs (- a b)) margin)))

(sc-no-semicolon (sph-define-float-array-nearly-equal sph-f32 float)
  (sph-define-float-array-nearly-equal sph-f64 double) (sph-define-float-sum sph-f32 float)
  (sph-define-float-sum sph-f64 double))

(pre-include-guard-end)
