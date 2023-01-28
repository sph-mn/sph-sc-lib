(pre-include-guard-begin sph-float-h)
(pre-include "inttypes.h")

(pre-define (sph-define-float-sum prefix type)
  (define ((pre-concat prefix _sum) numbers len) (type type* size-t)
    (declare temp type element type)
    (define correction type 0)
    (set len (- len 1))
    (define result type (array-get numbers len))
    (while len
      (set
        len (- len 1)
        element (array-get numbers len)
        temp (+ result element)
        correction
        (+ correction
          (if* (>= result element) (+ (- result temp) element) (+ (- element temp) result)))
        result temp))
    (return (+ correction result))))

(pre-define (sph-define-float-array-nearly-equal prefix type)
  (define ((pre-concat prefix _array-nearly-equal) a a-len b b-len error-margin)
    (uint8-t type* size-t type* size-t type)
    (define index size-t 0)
    (if (not (= a-len b-len)) (return #f))
    (while (< index a-len)
      (if
        (not
          ((pre-concat prefix _nearly-equal) (array-get a index) (array-get b index) error-margin))
        (return #f))
      (set index (+ 1 index)))
    (return #t)))

(declare
  (sph-f64-nearly-equal a b margin) (uint8-t double double double)
  (sph-f32-nearly-equal a b margin) (uint8-t float float float)
  (sph-f32-array-nearly-equal a a-len b b-len error-margin)
  (uint8-t float* size-t float* size-t float)
  (sph-f64-array-nearly-equal a a-len b b-len error-margin)
  (uint8-t double* size-t double* size-t double)
  (sph-f32-sum numbers len) (float float* size-t)
  (sph-f64-sum numbers len) (double double* size-t))

(pre-include-guard-end)