(pre-include "float.h" "math.h" "sph/types.c")

(pre-define (define-float-sum prefix type)
  (define ((pre-concat prefix _sum) numbers len) (type type* size-t)
    ; "sum numbers with rounding error compensation using kahan summation with neumaier modification"
    (declare
      temp type
      element type)
    (define correction type 0)
    (set len (- len 1))
    (define result type (array-get numbers len))
    (while len
      (set
        len (- len 1)
        element (array-get numbers len)
        temp (+ result element)
        correction
        (+
          correction
          (if* (>= result element) (+ (- result temp) element)
            (+ (- element temp) result)))
        result temp))
    (return (+ correction result))))

(pre-define (define-float-array-nearly-equal? prefix type)
  (define ((pre-concat prefix _array-nearly-equal?) a a-len b b-len error-margin)
    (uint8-t type* size-t type* size-t type)
    (define index size-t 0)
    (if (not (= a-len b-len)) (return #f))
    (while (< index a-len)
      (if
        (not
          ((pre-concat prefix _nearly-equal?) (array-get a index) (array-get b index) error-margin))
        (return #f))
      (set index (+ 1 index)))
    (return #t)))

(define (f64-nearly-equal? a b margin) (uint8-t f64 f64 f64)
  "approximate float comparison. margin is a factor and is low for low accepted differences.
   http://floating-point-gui.de/errors/comparison/"
  (if (= a b) (return #t)
    (begin
      (define diff f64 (fabs (- a b)))
      (return
        (if* (or (= 0 a) (= 0 b) (< diff DBL-MIN)) (< diff (* margin DBL-MIN))
          (< (/ diff (fmin (+ (fabs a) (fabs b)) DBL-MAX)) margin))))))

(define (f32-nearly-equal? a b margin) (uint8-t f32 f32 f32)
  "approximate float comparison. margin is a factor and is low for low accepted differences.
   http://floating-point-gui.de/errors/comparison/"
  (if (= a b) (return #t)
    (begin
      (define diff f32 (fabs (- a b)))
      (return
        (if* (or (= 0 a) (= 0 b) (< diff FLT-MIN)) (< diff (* margin FLT-MIN))
          (< (/ diff (fmin (+ (fabs a) (fabs b)) FLT-MAX)) margin))))))

(define-float-array-nearly-equal? f32 f32)
(define-float-array-nearly-equal? f64 f64)
(define-float-sum f32 f32)
(define-float-sum f64 f64)