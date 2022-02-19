(pre-include "inttypes.h" "string.h"
  "../main/spline-path.h" "../main/spline-path.c" "../main/float.c" "./test.c")

(define error-margin double 0.1)

(define (test-spline-path) status-t
  status-declare
  (declare
    out (array spline-path-value-t 100)
    out-new-get (array spline-path-value-t 100)
    i spline-path-time-t
    length spline-path-time-t
    path spline-path-t
    segments (array spline-path-segment-t 4)
    segments-count spline-path-segment-count-t
    log-path-new-0 uint8-t
    log-path-new-1 uint8-t
    log-path-new-get-0 uint8-t
    log-path-new-get-1 uint8-t)
  (set log-path-new-0 #f log-path-new-1 #f log-path-new-get-0 #f log-path-new-get-1 #f length 50)
  (for ((set i 0) (< i length) (set+ i 1))
    (set (array-get out i) 999 (array-get out-new-get i) 999))
  (sc-comment "path 2 - a special case that lead to errors before")
  (set
    (array-get segments 0) (spline-path-move 0 6)
    (array-get segments 1) (spline-path-line 24 18)
    (array-get segments 2) (spline-path-line 96 24)
    (array-get segments 3) (spline-path-constant)
    segments-count 4)
  (status-i-require (spline-path-segments-get segments segments-count 0 100 out-new-get))
  (sc-comment "path 0 - will be written to output starting at offset 5")
  (set
    (array-get segments 0) (spline-path-move 10 5)
    (array-get segments 1) (spline-path-line 20 10)
    (array-get segments 2) (spline-path-bezier 25 15 30 20 40 25)
    (array-get segments 4) (spline-path-constant)
    segments-count 4)
  (status-i-require (spline-path-set-copy &path segments segments-count))
  (spline-path-get &path 5 25 out)
  (spline-path-get &path 25 (+ 5 length) (+ 20 out))
  (if log-path-new-0
    (for ((set i 0) (< i length) (set+ i 1)) (printf "%lu %f\n" i (array-get out i))))
  (test-helper-assert "path 0.0" (f64-nearly-equal 0 (array-get out 0) error-margin))
  (test-helper-assert "path 0.4" (f64-nearly-equal 0 (array-get out 4) error-margin))
  (test-helper-assert "path 0.5" (f64-nearly-equal 5 (array-get out 5) error-margin))
  (test-helper-assert "path 0.15" (f64-nearly-equal 10 (array-get out 15) error-margin))
  (test-helper-assert "path 0.16" (f64-nearly-equal 10.75 (array-get out 16) error-margin))
  (test-helper-assert "path 0.34" (f64-nearly-equal 24.25 (array-get out 34) error-margin))
  (test-helper-assert "path 0.35" (f64-nearly-equal 25 (array-get out 35) error-margin))
  (test-helper-assert "path 0.49" (f64-nearly-equal 25 (array-get out 49) error-margin))
  (free path.segments)
  (sc-comment "path 0 new-get")
  (status-i-require (spline-path-segments-get segments segments-count 5 55 out-new-get))
  (if log-path-new-get-0
    (for ((set i 0) (< i length) (set+ i 1)) (printf "%lu %f\n" i (array-get out-new-get i))))
  (test-helper-assert "path 0 new-get equal"
    (not (memcmp out out-new-get (* (sizeof spline-path-value-t) length))))
  (sc-comment "path 1 - path that ends at 10")
  (for ((set i 0) (< i length) (set+ i 1))
    (sc-comment "reset output arrays")
    (set (array-get out i) 999 (array-get out-new-get i) 999))
  (set (array-get segments 0) (spline-path-line 10 5) segments-count 1)
  (status-i-require (spline-path-set-copy &path segments segments-count))
  (spline-path-get &path 0 12 out)
  (if log-path-new-1
    (for ((set i 0) (< i 12) (set i (+ 1 i))) (printf "%lu %f\n" i (array-get out i))))
  (test-helper-assert "path 1.10 - should reach maximum at 10"
    (f64-nearly-equal 5 (array-get out 10) error-margin))
  (test-helper-assert "path 1.11 - should be zero after segments"
    (f64-nearly-equal 0 (array-get out 11) error-margin))
  (free path.segments)
  (sc-comment "path 1 new-get")
  (status-i-require (spline-path-segments-get segments segments-count 0 12 out-new-get))
  (if log-path-new-get-1
    (for ((set i 0) (< i 12) (set+ i 1)) (printf "%lu %f\n" i (array-get out-new-get i))))
  (test-helper-assert "path 1 new-get equal"
    (not (memcmp out out-new-get (* (sizeof spline-path-value-t) 12))))
  (spline-path-free path)
  (label exit status-return))

(define (test-spline-path-helpers) status-t
  status-declare
  (declare
    out (array spline-path-value-t 50)
    path spline-path-t
    i spline-path-time-t
    segments (array spline-path-segment-t 4)
    segments2 (array spline-path-segment-t 2))
  (for ((set i 0) (< i 50) (set+ i 1)) (set (array-get out i) 999))
  (set
    (array-get segments 0) (spline-path-move 1 5)
    (array-get segments 1) (spline-path-line 10 10)
    (array-get segments 2) (spline-path-bezier 20 15 30 5 40 15)
    (array-get segments 3) (spline-path-constant))
  (spline-path-set-copy &path segments 4)
  (spline-path-get &path 0 50 out)
  (test-helper-assert "helper path 0" (f64-nearly-equal 0 (array-get out 0) error-margin))
  (test-helper-assert "helper path 49" (f64-nearly-equal 15 (array-get out 49) error-margin))
  (set
    (array-get segments2 0) (spline-path-line 5 10)
    (array-get segments2 1) (spline-path-path path))
  (sc-comment "note that the first point leaves a gap")
  (spline-path-segments-get segments2 2 0 50 out)
  (free path.segments)
  (label exit status-return))

(define (test-spline-path-circular-arc) status-t
  status-declare
  (declare
    p1 spline-path-point-t
    p2 spline-path-point-t
    pc spline-path-point-t
    out (array spline-path-value-t 50)
    log-path-0 uint8-t
    path spline-path-t
    i spline-path-time-t
    length spline-path-time-t
    segments spline-path-segment-t)
  (set log-path-0 #f)
  (sc-comment "control-point")
  (set p1.x 0 p1.y 0 p2.x 10 p2.y 10 pc (spline-path-i-circular-arc-control-point p1 p2 1.0))
  (test-helper-assert "control point"
    (and (= pc.x 2) (f64-nearly-equal 8.535534 pc.y error-margin)))
  (sc-comment "interpolation")
  (set length 50)
  (for ((set i 0) (< i length) (set+ i 1)) (set (array-get out i) 999))
  (set segments (spline-path-circular-arc 1 10 10))
  (spline-path-set &path &segments 1)
  (spline-path-get &path 0 length out)
  (spline-path-free path)
  (if log-path-0
    (for ((set i 0) (< i length) (set+ i 1)) (printf "%lu %f\n" i (array-get out i))))
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-spline-path-circular-arc)
  (goto exit)
  (test-helper-test-one test-spline-path-helpers)
  (test-helper-test-one test-spline-path)
  (label exit (test-helper-display-summary) (return status.id)))