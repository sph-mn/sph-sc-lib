(pre-include "inttypes.h" "string.h"
  "./test.c" "sph/spline-path.h" "sph/spline-path.c"
  "sph/float.h" "sph/float.c" "sph/spline-path-circular-arc.c")

(pre-define error-margin 0.1)

(define (reset-output out length) (void spline-path-value-t* size-t)
  (for ((define i size-t 0) (< i length) (set+ i 1)) (set (array-get out i) 0)))

(define (test-spline-path) status-t
  status-declare
  (declare
    out (array spline-path-value-t 50)
    out-new-get (array spline-path-value-t 50)
    i size-t
    length size-t
    path spline-path-t
    segments (array spline-path-segment-t 4)
    segments-count spline-path-segment-count-t
    log-path-new-0 uint8-t
    log-path-new-1 uint8-t
    log-path-new-get-0 uint8-t
    log-path-new-get-1 uint8-t)
  (set log-path-new-0 #f log-path-new-1 #f log-path-new-get-0 #f log-path-new-get-1 #f length 50)
  (sc-comment "path 0 - will be written to output starting at offset 5")
  (reset-output out length)
  (reset-output out-new-get length)
  (set
    (array-get segments 0) (spline-path-move 10 5)
    (array-get segments 1) (spline-path-line 20 10)
    (array-get segments 2) (spline-path-bezier 25 15 30 20 40 25)
    (array-get segments 3) (spline-path-constant)
    segments-count 4)
  (status-i-require (spline-path-set-copy &path segments segments-count))
  (sc-comment "get value starting from x 5")
  (spline-path-get &path 5 25 out)
  (spline-path-get &path 25 (+ 5 length) (+ 20 out))
  (if log-path-new-0
    (for ((set i 0) (< i length) (set+ i 1)) (printf "%lu %f\n" i (array-get out i))))
  (test-helper-assert "path 0.0" (sph-f64-nearly-equal 0 (array-get out 0) error-margin))
  (test-helper-assert "path 0.4" (sph-f64-nearly-equal 0 (array-get out 4) error-margin))
  (test-helper-assert "path 0.5" (sph-f64-nearly-equal 5 (array-get out 5) error-margin))
  (test-helper-assert "path 0.15" (sph-f64-nearly-equal 10 (array-get out 15) error-margin))
  (test-helper-assert "path 0.16" (sph-f64-nearly-equal 10.75 (array-get out 16) error-margin))
  (test-helper-assert "path 0.34" (sph-f64-nearly-equal 24.25 (array-get out 34) error-margin))
  (test-helper-assert "path 0.35" (sph-f64-nearly-equal 25 (array-get out 35) error-margin))
  (test-helper-assert "path 0.49" (sph-f64-nearly-equal 25 (array-get out 49) error-margin))
  (free path.segments)
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
  (if log-path-new-1
    (for ((set i 0) (< i 12) (set i (+ 1 i))) (printf "%lu %f\n" i (array-get out i))))
  (test-helper-assert "path 1.9 - should reach maximum at 9"
    (sph-f64-nearly-equal 4.5 (array-get out 9) error-margin))
  (test-helper-assert "path 1.10 - should not set the next start point"
    (sph-f64-nearly-equal 0 (array-get out 10) error-margin))
  (test-helper-assert "path 1.11 - should be zero after segments"
    (sph-f64-nearly-equal 0 (array-get out 11) error-margin))
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
    end-x size-t
    i size-t
    segments (array spline-path-segment-t 4)
    segments2 (array spline-path-segment-t 2)
    log-path-0 uint8-t)
  (set end-x 50 log-path-0 #f)
  (reset-output out end-x)
  (set
    (array-get segments 0) (spline-path-move 1 5)
    (array-get segments 1) (spline-path-line 10 10)
    (array-get segments 2) (spline-path-bezier 20 15 30 5 40 15)
    (array-get segments 3) (spline-path-constant))
  (spline-path-set-copy &path segments 4)
  (spline-path-get &path 0 end-x out)
  (if log-path-0 (for ((set i 0) (< i end-x) (set+ i 1)) (printf "%lu %f\n" i (array-get out i))))
  (test-helper-assert "helper path 0" (sph-f64-nearly-equal 0 (array-get out 0) error-margin))
  (test-helper-assert "helper path 49" (sph-f64-nearly-equal 15 (array-get out 49) error-margin))
  (set
    (array-get segments2 0) (spline-path-line 5 10)
    (array-get segments2 1) (spline-path-path path))
  (sc-comment "note that the first point leaves a gap")
  (spline-path-segments-get segments2 2 0 end-x out)
  (free path.segments)
  (label exit status-return))

(define (test-spline-path-bezier-arc) status-t
  status-declare
  (declare
    p1 spline-path-point-t
    p2 spline-path-point-t
    pc spline-path-point-t
    out (array spline-path-value-t 50)
    log-path-0 uint8-t
    path spline-path-t
    i size-t
    end-x size-t
    end-y spline-path-value-t
    segments spline-path-segment-t)
  (set log-path-0 #f end-x 50 end-y 10)
  (set p1.x 0 p1.y 0 p2.x end-x p2.y end-y pc (spline-path-perpendicular-point p1 p2 1.0))
  (sc-comment
    (test-helper-assert "perpendicular point"
      (and (sph-f64-nearly-equal 31.73 pc.x error-margin)
        (sph-f64-nearly-equal 9.94 pc.y error-margin))))
  (reset-output out end-x)
  (set segments (spline-path-circular-arc 0 end-x end-y))
  (spline-path-set &path &segments 1)
  (spline-path-get &path 0 end-x out)
  (spline-path-free path)
  (if log-path-0
    (begin
      (printf "%f %f\n" pc.x pc.y)
      (for ((set i 0) (< i end-x) (set+ i 1)) (printf "%lu %f\n" i (array-get out i)))))
  status-return)

(define (test-spline-path-circular-arc) status-t
  status-declare
  (declare
    p1 spline-path-point-t
    p2 spline-path-point-t
    pc spline-path-point-t
    out (array spline-path-value-t 50)
    log-path-0 uint8-t
    path spline-path-t
    i size-t
    end-x size-t
    end-y spline-path-value-t
    segments spline-path-segment-t)
  (set log-path-0 #f end-x 50 end-y 10)
  (set p1.x 0 p1.y 0 p2.x end-x p2.y end-y pc (spline-path-perpendicular-point p1 p2 1.0))
  (sc-comment
    (test-helper-assert "perpendicular point"
      (and (sph-f64-nearly-equal 31.73 pc.x error-margin)
        (sph-f64-nearly-equal 9.94 pc.y error-margin))))
  (reset-output out end-x)
  (set segments (spline-path-circular-arc 1 end-x end-y))
  (spline-path-set &path &segments 1)
  (spline-path-get &path 0 end-x out)
  (spline-path-free path)
  (if log-path-0
    (begin
      (printf "%f %f\n" pc.x pc.y)
      (for ((set i 0) (< i end-x) (set+ i 1)) (printf "%lu %f\n" i (array-get out i)))))
  (goto exit)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-spline-path-bezier-arc)
  (test-helper-test-one test-spline-path-circular-arc)
  (test-helper-test-one test-spline-path)
  (test-helper-test-one test-spline-path-helpers)
  (label exit (test-helper-display-summary) (return status.id)))