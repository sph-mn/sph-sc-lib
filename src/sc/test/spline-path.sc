(pre-include "inttypes.h" "string.h"
  "sph/test.h" "sph/spline-path.h" "sph/spline-path.c" "sph/float.h" "sph/float.c")

(pre-define
  error-margin 0.1
  test-spline-path-length 50
  test-spline-path-bezier-length 1000
  (feq a b) (sph-f64-nearly-equal a b 1.0e-7))

(define (reset-output out length) (void spline-path-value-t* size-t)
  (for ((define i size-t 0) (< i length) (set+ i 1)) (set (array-get out i) 0)))

(define (display-array a len) (void spline-path-value-t* size-t)
  "display a sample array in one line"
  (declare i size-t)
  (printf "%.17g" (array-get a 0))
  (for ((set i 1) (< i len) (set i (+ 1 i))) (printf " %.17g" (array-get a i)))
  (printf "\n"))

(define (test-spline-path) status-t
  status-declare
  (declare
    out (array spline-path-value-t test-spline-path-length)
    out-new-get (array spline-path-value-t test-spline-path-length)
    i size-t
    length size-t
    path spline-path-t
    segments (array spline-path-segment-t 4)
    segments-count spline-path-segment-count-t
    log-path-new-0 uint8-t
    log-path-new-1 uint8-t
    log-path-new-get-0 uint8-t
    log-path-new-get-1 uint8-t)
  (set
    log-path-new-0 #f
    log-path-new-1 #f
    log-path-new-get-0 #f
    log-path-new-get-1 #f
    length test-spline-path-length)
  (sc-comment "path 0 - will be written to output starting at offset 5")
  (reset-output out length)
  (reset-output out-new-get length)
  (set
    (array-get segments 0) (spline-path-move 10 5)
    (array-get segments 1) (spline-path-line 20 10)
    (array-get segments 2) (spline-path-bezier2 25 15 30 20 40 25)
    (array-get segments 3) (spline-path-constant)
    segments-count 4)
  (status-i-require (spline-path-set-copy &path segments segments-count))
  (sc-comment "starting output shifted by offset 5")
  (spline-path-get &path 5 25 out)
  (spline-path-get &path 25 (+ 5 length) (+ 20 out))
  (spline-path-free path)
  (free path.segments)
  (if log-path-new-0
    (for ((set i 0) (< i length) (set+ i 1)) (printf "%lu %f\n" i (array-get out i))))
  (test-helper-assert "before move 1" (feq 0 (array-get out 0)))
  (test-helper-assert "before move 2" (feq 0 (array-get out 4)))
  (test-helper-assert "move end" (feq 5 (array-get out 5)))
  (test-helper-assert "line end" (feq 9.5 (array-get out 14)))
  (test-helper-assert "bezier2 start" (feq 10.375 (array-get out 15)))
  (test-helper-assert "bezier2 end" (feq 24.625 (array-get out 34)))
  (test-helper-assert "constant" (feq 25 (array-get out (- length 1))))
  (sc-comment "path 0 new-get")
  (status-i-require (spline-path-segments-get segments segments-count 5 55 out-new-get))
  (if log-path-new-get-0
    (for ((set i 0) (< i length) (set+ i 1)) (printf "%lu %f\n" i (array-get out-new-get i))))
  (test-helper-assert "path 0 new-get equal"
    (not (memcmp out out-new-get (* (sizeof spline-path-value-t) length))))
  (sc-comment "path 1 - path that ends at 10")
  (reset-output out length)
  (reset-output out-new-get length)
  (set (array-get segments 0) (spline-path-line 10 5) segments-count 1)
  (status-i-require (spline-path-set-copy &path segments segments-count))
  (spline-path-get &path 0 12 out)
  (free path.segments)
  (if log-path-new-1
    (for ((set i 0) (< i 12) (set i (+ 1 i))) (printf "%lu %f\n" i (array-get out i))))
  (test-helper-assert "path 1.9 - should reach maximum at 9" (feq 4.5 (array-get out 9)))
  (test-helper-assert "path 1.10 - should not set the next start point" (feq 0 (array-get out 10)))
  (test-helper-assert "path 1.11 - should be zero after segments" (feq 0 (array-get out 11)))
  (status-i-require (spline-path-segments-get segments segments-count 0 12 out-new-get))
  (if log-path-new-get-1
    (for ((set i 0) (< i 12) (set+ i 1)) (printf "%lu %f\n" i (array-get out-new-get i))))
  (test-helper-assert "path 1 new-get equal"
    (not (memcmp out out-new-get (* (sizeof spline-path-value-t) 12))))
  (label exit status-return))

(define (test-spline-path-perpendicular-point) status-t
  status-declare
  (declare log-line uint8-t p1 spline-path-point-t p2 spline-path-point-t p3 spline-path-point-t)
  (set log-line #f p1.x 0 p1.y 0 p2.x 10 p2.y 10 p3 (spline-path-perpendicular-point p1 p2 0))
  (if log-line (for ((define i size-t 0) (< i p2.x) (set+ i 1)) (printf "%lu %lu\n" i i)))
  (test-helper-assert "d 0" (and (feq p3.x p3.y) (feq p3.x 5.0)))
  (set p3 (spline-path-perpendicular-point p1 p2 1))
  (test-helper-assert "d 1" (and (feq p3.x 10) (feq p3.y 0)))
  (set p3 (spline-path-perpendicular-point p1 p2 -1))
  (test-helper-assert "d -1" (and (feq p3.x 0) (feq p3.y 10)))
  (set p3 (spline-path-perpendicular-point p1 p2 0.5))
  (test-helper-assert "d 0.5" (and (feq p3.x 7.5) (feq p3.y 2.5)))
  (set p3 (spline-path-perpendicular-point p1 p2 -0.5))
  (test-helper-assert "d -0.5" (and (feq p3.x 2.5) (feq p3.y 7.5)))
  (label exit status-return))

(define (test-spline-path-bezier-arc) status-t
  status-declare
  (declare
    end-x spline-path-value-t
    end-y spline-path-value-t
    i size-t
    log-path-0 uint8-t
    out (array spline-path-value-t test-spline-path-bezier-length)
    path spline-path-t
    segments (array spline-path-segment-t 2))
  (set log-path-0 #f end-x test-spline-path-bezier-length end-y test-spline-path-bezier-length)
  (memset out 0 (* (sizeof spline-path-value-t) test-spline-path-bezier-length))
  (array-set* segments (spline-path-bezier-arc (/ end-x 3) (/ end-y 3) 1)
    (spline-path-bezier-arc end-x end-y -1))
  (spline-path-set &path segments 2)
  (spline-path-get &path 0 end-x out)
  (spline-path-free path)
  (if log-path-0 (for ((set i 0) (< i end-x) (set+ i 1)) (printf "%lu %f\n" i (array-get out i))))
  status-return)

(define (test-spline-path-power) (status_t)
  status-declare
  (declare
    out (array spline-path-value-t 11)
    path spline-path-t
    segments (array spline-path-segment-t 1))
  (define
    segments-count spline-path-segment-count-t 1
    x spline-path-value-t 10
    y spline-path-value-t 20
    gamma spline-path-value-t 2.0
    expected-mid spline-path-value-t (* y (pow 0.5 gamma)))
  (set (array-get segments 0) (spline-path-power x y gamma))
  (status-i-require (spline-path-set-copy (address-of path) segments segments-count))
  (spline-path-get (address-of path) 0 10 out)
  (test-helper-assert "power start" (feq (array-get out 0) 0.0))
  (test-helper-assert "power mid" (feq (array-get out 5) expected-mid))
  (test-helper-assert "power end-1" (< (array-get out 8) y))
  (test-helper-assert "power end" (feq (array-get out 9) 16.2))
  (spline-path-free path)
  (free (struct-get path segments))
  (label exit status-return))

(define (test-spline-path-exponential) (status_t)
  status-declare
  (declare
    out (array spline-path-value-t 11)
    path spline-path-t
    segments (array spline-path-segment-t 1))
  (define
    segments-count spline-path-segment-count-t 1
    x spline-path-value-t 10
    y spline-path-value-t 20
    gamma spline-path-value-t 2.0
    t spline-path-value-t 0.5
    denom spline-path-value-t (- (exp gamma) 1.0)
    expected-mid spline-path-value-t
    (if* (< denom 1.0e-12) (* 0.5 y) (* (/ (- (exp (* gamma t)) 1.0) denom) y)))
  (set (array-get segments 0) (spline-path-exponential x y gamma))
  (status-i-require (spline-path-set-copy (address-of path) segments segments-count))
  (spline-path-get (address-of path) 0 10 out)
  (spline-path-get (address-of path) 10 11 (address-of (array-get out 10)))
  (test-helper-assert "exponential start" (feq (array-get out 0) 0.0))
  (test-helper-assert "exponential mid" (feq (array-get out 5) expected-mid))
  (test-helper-assert "exponential end-1" (< (array-get out 8) y))
  (test-helper-assert "exponential end" (feq (array-get out 9) 15.80717835693481))
  (spline-path-free path)
  (free (struct-get path segments))
  (label exit status-return))

(define (test-spline-path-path-segment) (status-t)
  status-declare
  (declare
    out (array spline-path-value-t test-spline-path-length)
    inner-segments (array spline-path-segment-t 2)
    outer-segments (array spline-path-segment-t 2))
  (declare inner-path spline-path-t outer-path spline-path-t)
  (reset-output out test-spline-path-length)
  (set (array-get inner-segments 0) (spline-path-line 10 10))
  (status-i-require (spline-path-set-copy (address-of inner-path) inner-segments 1))
  (set
    (array-get outer-segments 0) (spline-path-move 0 0)
    (array-get outer-segments 1) (spline-path-path inner-path))
  (status-i-require (spline-path-set-copy (address-of outer-path) outer-segments 2))
  (spline-path-get (address-of outer-path) 0 12 out)
  (spline-path-free inner-path)
  (spline-path-free outer-path)
  (free (struct-get inner-path segments))
  (free (struct-get outer-path segments))
  (test-helper-assert "path segment y=0 before" (feq (array-get out 0) 0))
  (test-helper-assert "path segment linear mid"
    (and (> (array-get out 5) 4) (< (array-get out 5) 6)))
  (test-helper-assert "path segment y=10 end" (feq (array-get out 9) 9))
  (test-helper-assert "path segment after end" (feq (array-get out 11) 0))
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-spline-path-perpendicular-point)
  (test-helper-test-one test-spline-path-path-segment)
  (test-helper-test-one test-spline-path-power)
  (test-helper-test-one test-spline-path-exponential)
  (test-helper-test-one test-spline-path)
  (test-helper-test-one test-spline-path-bezier-arc)
  (label exit test-helper-display-summary (return status.id)))
