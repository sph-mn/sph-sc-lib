(pre-include "float.h" "strings.h"
  "stdlib.h" "math.h" "stddef.h" "stdio.h" "string.h" "sph/spline-path.h")

(pre-define
  spline-path-bezier-resolution 2
  (linearly-interpolate a b t) (+ a (* t (- b a)))
  (spline-path-bezier2-interpolate t mt a b c d)
  (+ (* a mt mt mt) (* b 3 mt mt t) (* c 3 mt t t) (* d t t t))
  (spline-path-bezier1-interpolate t mt a b c) (+ (* a mt mt) (* b 2 mt t) (* c t t))
  (spline-path-declare-segment id) (define id spline-path-segment-t (struct-literal 0))
  (spline-path-points-get-with-offset points index field-offset)
  (pointer-get
    (convert-type (+ field-offset (convert-type (+ points index) uint8-t*)) spline-path-value-t*)))

(define (spline-path-debug-print path indent) (void spline-path-t* uint8-t)
  (define type uint8-t* 0)
  (declare pad (array uint8-t 32))
  (memset pad #\space (sizeof pad))
  (set (array-get pad (if* (< indent 30) indent 30)) 0)
  (printf "%spath segments: %zu\n" pad (convert-type path:segments-count size-t))
  (for ((define i size-t 0) (< i path:segments-count) (set+ i 1))
    (define
      s spline-path-segment-t* (address-of (array-get (struct-pointer-get path segments) i))
      p0 spline-path-point-t* (address-of (array-get (struct-pointer-get s points) 0))
      p1 spline-path-point-t*
      (address-of
        (array-get (struct-pointer-get s points) (- (struct-pointer-get s points-count) 1))))
    (case = s:generate
      (spline-path-line-generate (set type "line"))
      (spline-path-move-generate (set type "move"))
      (spline-path-constant-generate (set type "constant"))
      (spline-path-bezier1-generate (set type "bezier1"))
      (spline-path-bezier2-generate (set type "bezier2"))
      (spline-path-power-generate (set type "power"))
      (spline-path-exponential-generate (set type "exponential"))
      (spline-path-path-generate (set type "path"))
      (else (set type "custom")))
    (printf "%s  %s %.3f %.3f -> %.3f %.3f\n" pad
      (convert-type type char*) (struct-pointer-get p0 x) (struct-pointer-get p0 y)
      (struct-pointer-get p1 x) (struct-pointer-get p1 y))
    (if (and (= s:generate spline-path-path-generate) s:data)
      (begin
        (define sub spline-path-t* (convert-type (struct-pointer-get s data) spline-path-t*))
        (spline-path-debug-print sub (+ indent 2)))))
  return)

(define (spline-path-validate path log) (uint8-t spline-path-t* uint8-t)
  (if
    (or (not path) (not (struct-pointer-get path segments))
      (= (struct-pointer-get path segments-count) 0))
    (begin
      (if log (fprintf stderr "spline_path_validate: invalid path pointer or empty segment list\n"))
      (return 1)))
  (for ((define i size-t 0) (< i path:segments-count) (set+ i 1))
    (define
      s spline-path-segment-t* (address-of (array-get path:segments i))
      p0 spline-path-point-t* (address-of (array-get s:points 0))
      p1 spline-path-point-t* (address-of (array-get s:points 1)))
    (if (or (< s:points-count 2) (> s:points-count spline-path-point-max))
      (begin
        (if log
          (fprintf stderr "segment %zu: invalid points count %zu\n"
            i (convert-type s:points-count size-t)))
        (return 1)))
    (if (< p1:x p0:x)
      (begin
        (if log (fprintf stderr "segment %zu: end x (%f) < start x (%f)\n" i p1:x p0:x))
        (return 1)))
    (if (not s:generate)
      (begin (if log (fprintf stderr "segment %zu: missing generate function\n" i)) (return 1)))
    (if (= s:generate spline-path-path-generate)
      (begin
        (define sub spline-path-t* (convert-type s:data spline-path-t*))
        (if (or (not sub) (not sub:segments) (= sub:segments-count 0))
          (begin (if log (fprintf stderr "segment %zu: invalid nested path\n" i)) (return 1)))
        (if (spline-path-validate sub log)
          (begin
            (if log (fprintf stderr "segment %zu: nested path validation failed\n" i))
            (return 1))))))
  (return 0))

(define (spline-path-alloc-segment gen x y out data-size)
  ((static spline-path-segment-t) (function-pointer void size-t size-t spline-path-segment-t* spline-path-value-t*) spline-path-value-t spline-path-value-t void* size-t)
  (spline-path-declare-segment s)
  (struct-set s generate gen points-count 2)
  (struct-set (array-get s.points 1) x x y y)
  (set s.data (malloc data-size))
  (if s.data (memcpy s.data out data-size) (return (spline-path-constant)))
  (set s.free free)
  (return s))

(define (spline-path-get path start end out)
  (void spline-path-t* size-t size-t spline-path-value-t*)
  "get values on path between start (inclusive) and end (exclusive).
   since x values are integers, a path from (0 0) to (10 20) reaches 20 at the 11th point.
   out memory is managed by the caller. the size required for out is end minus start"
  (declare s spline-path-segment-t* s-start size-t s-end size-t out-start size-t)
  (for
    ( (define
        i spline-path-segment-count-t
        (if* (< start path:previous-start) 0 path:current-segment-index))
      (< i path:segments-count) (begin (set path:current-segment-index i) (set+ i 1)))
    (set
      s (+ path:segments i)
      s-start (struct-get (array-get s:points 0) x)
      s-end (struct-get (array-get s:points (- s:points-count 1)) x)
      path:previous-start start)
    (if (> s-start end) break)
    (if (< s-end start) continue)
    (set out-start (if* (> s-start start) (- s-start start) 0))
    (if (< s-start start) (set s-start start))
    (if (> s-end end) (set s-end end))
    (s:generate s-start s-end s (+ out-start out))))

(define (spline-path-start path) (spline-path-point-t spline-path-t)
  (return (array-get *path.segments:points)))

(define (spline-path-end path) (spline-path-point-t spline-path-t)
  (declare s spline-path-segment-t* p spline-path-point-t)
  (set s (+ path.segments (- path.segments-count 1)) p (array-get s:points (- s:points-count 1)))
  (return (if* (= spline-path-size-max p.x) (array-get s:points 0) p)))

(define (spline-path-size path) (size-t spline-path-t)
  (declare p spline-path-point-t)
  (set p (spline-path-end path))
  (return p.x))

(define (spline-path-path-prepare s) (uint8-t spline-path-segment-t*)
  (define
    path-end spline-path-point-t (spline-path-end (pointer-get (convert-type s:data spline-path-t*))))
  (set
    (struct-get (array-get s:points 1) x) (+ (struct-get (array-get s:points 0) x) path-end.x)
    (struct-get (array-get s:points 1) y) path-end.y)
  (return 0))

(define (spline-path-constant-prepare s) (uint8-t spline-path-segment-t*)
  (struct-set (array-get s:points 1) x spline-path-size-max y (struct-get (array-get s:points 0) y))
  (return 0))

(define (spline-path-prepare-segments segments segments-count)
  (uint8-t spline-path-segment-t* spline-path-segment-count-t)
  "set _start and _points_count for segments"
  (declare s spline-path-segment-t* start spline-path-point-t)
  (set start.x 0 start.y 0)
  (for ((define i spline-path-segment-count-t 0) (< i segments-count) (set+ i 1))
    (set s (+ segments i) (array-get s:points 0) start)
    (if (and s:prepare (s:prepare s)) (return 1))
    (set start (array-get s:points (- s:points-count 1))))
  (return 0))

(define (spline-path-set path segments segments-count)
  (uint8-t spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  "set segments for a path and initialize it"
  (if (spline-path-prepare-segments segments segments-count) (return 1))
  (set
    path:segments segments
    path:segments-count segments-count
    path:current-segment-index 0
    path:previous-start 0)
  (return 0))

(define (spline-path-set-copy path segments segments-count)
  (uint8-t spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  "like spline_path_set but copies segments to new memory which has to be freed explicitly after use"
  (define s spline-path-segment-t* (malloc (* segments-count (sizeof spline-path-segment-t))))
  (if (not s) (return 1))
  (memcpy s segments (* segments-count (sizeof spline-path-segment-t)))
  (return (spline-path-set path s segments-count)))

(define (spline-path-segments-get segments segments-count start end out)
  (uint8-t spline-path-segment-t* spline-path-segment-count-t size-t size-t spline-path-value-t*)
  "create a sampled array directly from segments"
  (declare path spline-path-t)
  (if (spline-path-set &path segments segments-count) (return 1))
  (spline-path-get &path start end out)
  (spline-path-free path)
  (return 0))

(define (spline-path-free path) (void spline-path-t)
  "only needed if a segment with state has been used,
   which is currently only spline_path_path"
  (declare s spline-path-segment-t* i spline-path-segment-count-t)
  (for ((set i 0) (< i path.segments-count) (set+ i 1))
    (set s (+ i path.segments))
    (if s:free (s:free s:data))))

(define (spline-path-move-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  "p_rest length 1"
  (memset out 0 (* (sizeof spline-path-value-t) (- end start))))

(define (spline-path-constant-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  "p_rest length 0"
  (declare y spline-path-value-t)
  (set y (struct-get (array-get s:points 0) y))
  (for ((define i size-t 0) (< i (- end start)) (set+ i 1)) (set (array-get out i) y)))

(define (spline-path-line-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (declare
    p-start spline-path-point-t
    p-end spline-path-point-t
    t spline-path-value-t
    s-size spline-path-value-t
    s-offset size-t)
  (set
    p-start (array-get s:points 0)
    p-end (array-get s:points 1)
    s-size (- p-end.x p-start.x)
    s-offset (- start p-start.x))
  (for ((define i size-t 0) (< i (- end start)) (set+ i 1))
    (set t (/ (+ i s-offset) s-size) (array-get out i) (linearly-interpolate p-start.y p-end.y t))))

(define (spline-path-path-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-get (convert-type s:data spline-path-t*) (- start (convert-type s:points:x size-t))
    (- end (convert-type s:points:x size-t)) out))

(define (spline-path-beziern-generate interpolator start end s out)
  (void (function-pointer spline-path-value-t spline-path-value-t spline-path-value-t spline-path-point-t* size-t) size-t size-t spline-path-segment-t* spline-path-value-t*)
  "quadratic bezier curve with one control point.
   bezier interpolation also interpolates x. higher resolution sampling and linear interpolation are used to fill gaps"
  (declare
    b-size size-t
    i size-t
    j size-t
    mt spline-path-value-t
    p-end spline-path-point-t
    p-start spline-path-point-t
    s-size spline-path-value-t
    t spline-path-value-t
    x-previous size-t
    x size-t
    y-previous spline-path-value-t
    y spline-path-value-t
    mt-prev spline-path-value-t
    t-prev spline-path-value-t
    xj size-t
    j-start size-t)
  (set
    x start
    p-start (array-get s:points 0)
    p-end (array-get s:points (- s:points-count 1))
    b-size (* spline-path-bezier-resolution (- end start))
    s-size (* spline-path-bezier-resolution (- p-end.x p-start.x)))
  (for
    ( (set i (- start p-start.x) x-previous start) (or (< i b-size) (< x end))
      (begin (set+ i 1) (set x-previous x)))
    (set
      t (/ i s-size)
      mt (- 1.0 t)
      x (round (interpolator t mt s:points (offsetof spline-path-point-t x)))
      y (interpolator t mt s:points (offsetof spline-path-point-t y)))
    (if (and (>= x start) (< x end)) (set (array-get out (- x start)) y))
    (if (> 2 (- x x-previous)) continue)
    (if (< start x-previous) (set y-previous (array-get out (- x-previous start)))
      (begin
        (sc-comment "gap at the beginning. find value for x before start")
        (set t-prev 0 mt-prev 1)
        (for ((set j i) j (set- j 1))
          (set
            t (/ j s-size)
            mt (- 1.0 t)
            xj (round (interpolator t mt s:points (offsetof spline-path-point-t x))))
          (if (< xj x) (begin (set x-previous xj t-prev t mt-prev mt) break)))
        (if j
          (set y-previous (interpolator t-prev mt-prev s:points (offsetof spline-path-point-t y)))
          (set y-previous p-start.y x-previous p-start.x))))
    (set j-start 1)
    (if (< (+ x-previous j-start) start) (set j-start (- start x-previous)))
    (for ((set j j-start) (and (< j (- x x-previous)) (< (+ x-previous j) end)) (set+ j 1))
      (set
        t (/ j (convert-type (- x x-previous) spline-path-value-t))
        (array-get out (- (+ x-previous j) start)) (linearly-interpolate y-previous y t)))))

(define (spline-path-bezier1-interpolate-points t mt points field-offset)
  (spline-path-value-t spline-path-value-t spline-path-value-t spline-path-point-t* size-t)
  (return
    (spline-path-bezier1-interpolate t mt
      (spline-path-points-get-with-offset points 0 field-offset)
      (spline-path-points-get-with-offset points 1 field-offset)
      (spline-path-points-get-with-offset points 2 field-offset))))

(define (spline-path-bezier2-interpolate-points t mt points field-offset)
  (spline-path-value-t spline-path-value-t spline-path-value-t spline-path-point-t* size-t)
  (return
    (spline-path-bezier2-interpolate t mt
      (spline-path-points-get-with-offset points 0 field-offset)
      (spline-path-points-get-with-offset points 1 field-offset)
      (spline-path-points-get-with-offset points 2 field-offset)
      (spline-path-points-get-with-offset points 3 field-offset))))

(define (spline-path-bezier1-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-beziern-generate spline-path-bezier1-interpolate-points start end s out))

(define (spline-path-bezier2-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-beziern-generate spline-path-bezier2-interpolate-points start end s out))

(define (spline-path-power-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (declare
    p0 spline-path-point-t
    p1 spline-path-point-t
    span spline-path-value-t
    off size-t
    gamma spline-path-value-t
    i size-t
    t spline-path-value-t
    f spline-path-value-t)
  (set
    p0 (array-get s:points 0)
    p1 (array-get s:points (- s:points-count 1))
    span (- p1.x p0.x)
    off (- start p0.x)
    gamma (if* s:data (pointer-get (convert-type s:data spline-path-value-t*)) 1.0))
  (for ((set i 0) (< i (- end start)) (set+ i 1))
    (set
      t (/ (+ i off) span)
      f (spline-path-pow t gamma)
      (array-get out i) (linearly-interpolate p0.y p1.y f))))

(define (spline-path-exponential-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (declare
    p0 spline-path-point-t
    p1 spline-path-point-t
    span spline-path-value-t
    off size-t
    gamma spline-path-value-t
    denom spline-path-value-t
    i size-t
    t spline-path-value-t
    f spline-path-value-t)
  (set
    p0 (array-get s:points 0)
    p1 (array-get s:points (- s:points-count 1))
    span (- p1.x p0.x)
    off (- start p0.x)
    gamma (if* s:data (pointer-get (convert-type s:data spline-path-value-t*)) 0.0)
    denom (if* (< (spline-path-fabs gamma) DBL_EPSILON) 1.0 (- (spline-path-exp gamma) 1.0)))
  (for ((set i 0) (< i (- end start)) (set+ i 1))
    (set
      t (/ (+ i off) span)
      f
      (if* (< (spline-path-fabs gamma) DBL-EPSILON) t
        (/ (- (spline-path-exp (* gamma t)) 1.0) denom))
      (array-get out i) (linearly-interpolate p0.y p1.y f))))

(define (spline-path-power x y gamma)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t)
  "power-curve interpolation. y = y0 + (y1 - y0) * t ** gamma"
  (return
    (spline-path-alloc-segment spline-path-power-generate x y &gamma (sizeof spline-path-value-t))))

(define (spline-path-exponential x y gamma)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t)
  "y = y0 + (y1 - y0) * ((e ** (gamma * t) - 1) / (e ** gamma - 1))"
  (return
    (spline-path-alloc-segment spline-path-exponential-generate x
      y &gamma (sizeof spline-path-value-t))))

(define (spline-path-move x y) (spline-path-segment-t spline-path-value-t spline-path-value-t)
  "returns a move segment to move to the specified point"
  (spline-path-declare-segment s)
  (struct-set s generate spline-path-move-generate points-count 2)
  (struct-set (array-get s.points 1) x x y y)
  (return s))

(define (spline-path-line x y) (spline-path-segment-t spline-path-value-t spline-path-value-t)
  (spline-path-declare-segment s)
  (struct-set s generate spline-path-line-generate points-count 2)
  (struct-set (array-get s.points 1) x x y y)
  (return s))

(define (spline-path-constant) spline-path-segment-t
  (spline-path-declare-segment s)
  (struct-set s
    generate spline-path-constant-generate
    prepare spline-path-constant-prepare
    points-count 2)
  (return s))

(define (spline-path-path path) (spline-path-segment-t spline-path-t)
  "return a segment that is another spline-path. length is the full length of the path.
   the path does not necessarily connect and is drawn as it would be on its own starting from the preceding segment"
  (spline-path-declare-segment s)
  (set s.data (malloc (sizeof spline-path-t)))
  (if (not s.data) (return (spline-path-constant)))
  (set (pointer-get (convert-type s.data spline-path-t*)) path)
  (struct-set s
    free free
    generate spline-path-path-generate
    points-count 2
    prepare spline-path-path-prepare)
  (return s))

(define (spline-path-bezier1 x1 y1 x2 y2)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t spline-path-value-t)
  (spline-path-declare-segment s)
  (struct-set s generate spline-path-bezier1-generate points-count 3)
  (struct-set (array-get s.points 1) x x1 y y1)
  (struct-set (array-get s.points 2) x x2 y y2)
  (return s))

(define (spline-path-bezier2 x1 y1 x2 y2 x3 y3)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t spline-path-value-t spline-path-value-t spline-path-value-t)
  (spline-path-declare-segment s)
  (struct-set s generate spline-path-bezier2-generate points-count 4)
  (struct-set (array-get s.points 1) x x1 y y1)
  (struct-set (array-get s.points 2) x x2 y y2)
  (struct-set (array-get s.points 3) x x3 y y3)
  (return s))

(define (spline-path-perpendicular-point p1 p2 distance)
  (spline-path-point-t spline-path-point-t spline-path-point-t spline-path-value-t)
  "return a point on a perpendicular line across the midpoint of a line between p1 and p2.
   distance -1 and 1 are the bounds of a rectangle where p1 and p2 are diagonally opposed edges"
  (declare
    c spline-path-point-t
    d spline-path-point-t
    b1 spline-path-point-t
    b2 spline-path-point-t
    t1 spline-path-point-t
    t2 spline-path-point-t
    i1 spline-path-point-t
    i2 spline-path-point-t
    m spline-path-value-t
    result spline-path-point-t)
  (sc-comment "center point")
  (set c.x (/ (+ p1.x p2.x) 2) c.y (/ (+ p1.y p2.y) 2))
  (if (= 0 distance) (return c))
  (sc-comment "perpendicular direction vector")
  (set d.x (* -1 (- p2.y p1.y)) d.y (- p2.x p1.x))
  (sc-comment "vertical, horizontal or else")
  (cond
    ((= 0 d.x) (set i1.x c.x i1.y 0 i2.x c.x i2.y p2.y))
    ((= 0 d.y) (set i1.x 0 i1.y c.y i2.x p2.x i2.y c.y))
    (else
      (sc-comment "border points")
      (if (> d.x 0) (set b1.x p2.x b2.x p1.x) (set b1.x p1.x b2.x p2.x))
      (if (> d.y 0) (set b1.y p2.y b2.y p1.y) (set b1.y p1.y b2.y p2.y))
      (set
        t1.x (/ (- b1.x c.x) d.x)
        t1.y (/ (- b1.y c.y) d.y)
        t2.x (/ (- b2.x c.x) d.x)
        t2.y (/ (- b2.y c.y) d.y))
      (if (<= t1.x t1.y) (set i1.x b1.x i1.y (+ c.y (* t1.x d.y)))
        (set i1.y b1.y i1.x (+ c.x (* t1.y d.x))))
      (if (>= t2.x t2.y) (set i2.x b2.x i2.y (+ c.y (* t2.x d.y)))
        (set i2.y b2.y i2.x (+ c.x (* t2.y d.x))))))
  (sc-comment "normalized direction vector")
  (set
    m (spline-path-sqrt (+ (* d.x d.x) (* d.y d.y)))
    d.x (/ (- i2.x i1.x) m)
    d.y (/ (- i2.y i1.y) m)
    result.x (+ c.x (* 0.5 distance m d.x))
    result.y (+ c.y (* 0.5 distance m d.y)))
  (return result))

(define (spline-path-bezier-arc-prepare s) (uint8-t spline-path-segment-t*)
  (set (array-get s:points 1)
    (spline-path-perpendicular-point (array-get s:points 0) (array-get s:points 2)
      (struct-get (array-get s:points 1) y)))
  (return 0))

(define (spline-path-bezier-arc x y curvature)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t)
  "curvature is a real between -1..1, with the maximum at the sides of
   a rectangle with the points as diagonally opposing edges.
   interpolates with a quadratic bezier with a midpoint sagitta proportional to chord length"
  (declare s spline-path-segment-t)
  (if (= 0.0 curvature) (set s (spline-path-line x y))
    (set s (spline-path-bezier1 0 curvature x y) s.prepare spline-path-bezier-arc-prepare))
  (return s))
