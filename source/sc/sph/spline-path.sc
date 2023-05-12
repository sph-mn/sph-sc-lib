(pre-include "float.h" "strings.h" "stdlib.h" "math.h")

(pre-define
  spline-path-bezier-resolution 2
  (spline-path-cheap-round-positive a) (convert-type (+ 0.5 a) size-t)
  (linearly-interpolate a b t) (+ a (* t (- b a)))
  (spline-path-bezier2-interpolate mt t a b c d)
  (+ (* a mt mt mt) (* b 3 mt mt t) (* c 3 mt t t) (* d t t t))
  (spline-path-bezier1-interpolate mt t a b c) (+ (* a mt mt) (* b 2 mt t) (* c t t))
  (spline-path-declare-segment id) (define id spline-path-segment-t (struct-literal 0)))

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
  (set (array-get s:points 1) (spline-path-end (pointer-get (convert-type s:data spline-path-t*))))
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
  "like spline_path_set but copies segments to new memory which has to be freed after use"
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
  (spline-path-get (convert-type s:data spline-path-t*) (- start s:points:x) (- end s:points:x) out))

(define (spline-path-bezier1-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  "quadratic bezier curve with one control point.
   bezier interpolation also interpolates x. higher resolution sampling and linear interpolation are used to fill gaps"
  (declare
    mt spline-path-value-t
    p-control spline-path-point-t
    p-end spline-path-point-t
    p-start spline-path-point-t
    s-size spline-path-value-t
    t spline-path-value-t
    b-size size-t
    x size-t
    x-previous size-t
    y-previous spline-path-value-t
    i size-t
    j size-t
    y spline-path-value-t)
  (set
    p-start (array-get s:points 0)
    p-control (array-get s:points 1)
    p-end (array-get s:points 2)
    b-size (* spline-path-bezier-resolution (- end start))
    s-size (* spline-path-bezier-resolution (- p-end.x p-start.x)))
  (printf "%f %f\n" p-control.x p-control.y)
  (for
    ( (set i (- start p-start.x) x-previous start) (or (< i b-size) (< x end))
      (begin (set+ i 1) (set x-previous x)))
    (set
      t (/ i s-size)
      mt (- 1.0 t)
      x (round (spline-path-bezier1-interpolate mt t p-start.x p-control.x p-end.x))
      y (spline-path-bezier1-interpolate mt t p-start.y p-control.y p-end.y))
    (if (< x end) (set (array-get out (- x start)) y))
    (if (> 2 (- x x-previous)) continue)
    (if x-previous (set y-previous (array-get out (- x-previous start)))
      (begin
        (sc-comment "gap at the beginning. find value for x before start")
        (for ((set j i) j (set- j 1))
          (set
            t (/ j s-size)
            mt (- 1.0 t)
            x-previous (round (spline-path-bezier1-interpolate mt t p-start.x p-control.x p-end.x)))
          (if (< x-previous x) break))
        (if j (set y-previous p-start.y) (set y-previous p-start.y x-previous p-start.x))))
    (for ((set j 1) (< j (- x x-previous)) (set+ j 1))
      (set
        t (/ j (convert-type (- x x-previous) spline-path-value-t))
        (array-get out (- (+ x-previous j) start)) (linearly-interpolate y-previous y t)))))

(define (spline-path-bezier2-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (declare
    mt spline-path-value-t
    p-control1 spline-path-point-t
    p-control2 spline-path-point-t
    p-end spline-path-point-t
    p-start spline-path-point-t
    s-size spline-path-value-t
    t spline-path-value-t)
  (set
    p-start (array-get s:points 0)
    p-control1 (array-get s:points 1)
    p-control2 (array-get s:points 2)
    p-end (array-get s:points 3)
    s-size (- p-end.x p-start.x))
  (for ((define i size-t start) (< i end) (set+ i 1))
    (set
      t (/ (- i start) s-size)
      mt (- 1.0 t)
      (array-get out i)
      (spline-path-bezier2-interpolate mt t p-start.y p-control1.y p-control2.y p-end.y))))

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
    points-count 1)
  (return s))

(define (spline-path-path path) (spline-path-segment-t spline-path-t)
  "return a segment that is another spline-path. length is the full length of the path.
   the path does not necessarily connect and is drawn as it would be on its own starting from the preceding segment"
  (spline-path-declare-segment s)
  (set s.data (malloc (sizeof spline-path-t)))
  (if s.data (set (pointer-get (convert-type s.data spline-path-t*)) path)
    (return (spline-path-constant)))
  (struct-set s
    free free
    generate spline-path-path-generate
    points-count 1
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
  (struct-set s generate spline-path-bezier1-generate points-count 4)
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
  (printf "%f %f\n" c.x c.y)
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
        (set i1.x (+ c.x (* t1.y d.x)) i1.y b1.y))
      (if (>= t2.x t2.y) (set i2.x b2.x i2.y (+ c.y (* t2.x d.y)))
        (set i2.x (+ c.y (* t2.x d.y)) i2.y b2.y))))
  (sc-comment "normalized direction vector")
  (set
    m (sqrt (+ (* d.x d.x) (* d.y d.y)))
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
   a rectangle with the points as diagonally opposing edges"
  (declare s spline-path-segment-t)
  (if (= 0.0 curvature) (set s (spline-path-line x y))
    (set s (spline-path-bezier1 0 curvature x y) s.prepare spline-path-bezier-arc-prepare))
  (return s))