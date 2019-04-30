(pre-include "inttypes.h" "string.h" "../main/spline-path.c" "../main/float.c" "./test.c")
(define error-margin double 0.1)

(define (test-spline-path) status-t
  status-declare
  (declare
    out (array spline-path-value-t 50)
    out-new-get (array spline-path-value-t 50)
    i spline-path-time-t
    path spline-path-t
    p spline-path-point-t
    s spline-path-segment-t
    segments (array spline-path-segment-t 4)
    segments-len spline-path-segment-count-t
    log-path-new-0 uint8-t
    log-path-new-1 uint8-t
    log-path-new-get-0 uint8-t
    log-path-new-get-1 uint8-t)
  (set
    log-path-new-0 #f
    log-path-new-1 #f
    log-path-new-get-0 #f
    log-path-new-get-1 #f)
  (for ((set i 0) (< i 50) (set i (+ 1 i)))
    (set
      (array-get out i) 999
      (array-get out-new-get i) 999))
  (sc-comment "path 0")
  (set
    s.interpolator spline-path-i-move
    p.x 10
    p.y 5
    (array-get s.points 0) p
    (array-get segments 0) s
    s.interpolator spline-path-i-line
    p.x 20
    p.y 10
    (array-get s.points 0) p
    (array-get segments 1) s
    s.interpolator spline-path-i-bezier
    p.x 25
    p.y 15
    (array-get s.points 0) p
    p.x 30
    p.y 20
    (array-get s.points 1) p
    p.x 40
    p.y 25
    (array-get s.points 2) p
    (array-get segments 2) s
    s.interpolator spline-path-i-constant
    (array-get segments 3) s
    segments-len 4)
  (status-id-require (spline-path-new segments-len segments &path))
  (spline-path-get path 5 25 out)
  (spline-path-get path 25 55 (+ 20 out))
  (if log-path-new-0
    (for ((set i 0) (< i 50) (set i (+ 1 i)))
      (printf "%lu %f\n" i (array-get out i))))
  (test-helper-assert "path 0.0" (f64-nearly-equal 0 (array-get out 0) error-margin))
  (test-helper-assert "path 0.4" (f64-nearly-equal 0 (array-get out 4) error-margin))
  (test-helper-assert "path 0.5" (f64-nearly-equal 5 (array-get out 5) error-margin))
  (test-helper-assert "path 0.15" (f64-nearly-equal 10 (array-get out 15) error-margin))
  (test-helper-assert "path 0.16" (f64-nearly-equal 10.75 (array-get out 16) error-margin))
  (test-helper-assert "path 0.34" (f64-nearly-equal 24.25 (array-get out 34) error-margin))
  (test-helper-assert "path 0.35" (f64-nearly-equal 25 (array-get out 35) error-margin))
  (test-helper-assert "path 0.49" (f64-nearly-equal 25 (array-get out 49) error-margin))
  (spline-path-free path)
  (sc-comment "path 0 new-get")
  (status-id-require (spline-path-new-get segments-len segments 5 55 out-new-get))
  (if log-path-new-get-0
    (for ((set i 0) (< i 50) (set i (+ 1 i)))
      (printf "%lu %f\n" i (array-get out-new-get i))))
  (test-helper-assert
    "path 0 new-get equal" (not (memcmp out out-new-get (* (sizeof spline-path-value-t) 50))))
  (sc-comment "path 1 - test last point")
  (set
    s.interpolator spline-path-i-line
    p.x 10
    p.y 5
    (array-get s.points 0) p
    (array-get segments 0) s
    segments-len 1)
  (status-id-require (spline-path-new segments-len segments &path))
  (spline-path-get path 0 12 out)
  (if log-path-new-1
    (for ((set i 0) (< i 12) (set i (+ 1 i)))
      (printf "%lu %f\n" i (array-get out i))))
  (test-helper-assert "path 1.30" (f64-nearly-equal 5 (array-get out 10) error-margin))
  (test-helper-assert "path 1.31" (f64-nearly-equal 0 (array-get out 11) error-margin))
  (spline-path-free path)
  (sc-comment "path 1 new-get")
  (status-id-require (spline-path-new-get segments-len segments 0 12 out-new-get))
  (if log-path-new-get-1
    (for ((set i 0) (< i 12) (set i (+ 1 i)))
      (printf "%lu %f\n" i (array-get out-new-get i))))
  (test-helper-assert
    "path 1 new-get equal" (not (memcmp out out-new-get (* (sizeof spline-path-value-t) 12))))
  (label exit
    (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-spline-path)
  (label exit
    (test-helper-display-summary)
    (return status.id)))