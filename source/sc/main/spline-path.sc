(sc-comment "depends on spline-path-h.c")
(pre-include "math.h")
(pre-include "stdio.h")

(define (spline-path-i-move start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  "p-rest length 1"
  (memset out 0 (* (sizeof spline-path-value-t) (- end start))))

(define (spline-path-i-constant start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  "p-rest length 0"
  (for ((define i size-t start) (< i end) (set+ i 1)) (set (array-get out (- i start)) p-start.y)))

(define (spline-path-i-line start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  "p-rest length 1"
  (declare i size-t p-end spline-path-point-t t spline-path-value-t p-start-x size-t)
  (set p-start-x p-start.x p-end (array-get p-rest 0))
  (for ((set i start) (< i end) (set+ i 1))
    (set
      t (/ (- i p-start-x) (- p-end.x p-start.x))
      (array-get out (- i start)) (+ (* p-end.y t) (* p-start.y (- 1 t))))))

(define (spline-path-i-bezier start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  "p-rest length 3. this implementation ignores control point x values"
  (declare
    i size-t
    mt spline-path-value-t
    p-end spline-path-point-t
    s-size spline-path-value-t
    t spline-path-value-t)
  (set p-end (array-get p-rest 2) s-size (- p-end.x p-start.x))
  (for ((set i start) (< i end) (set+ i 1))
    (set
      t (/ (- i p-start.x) s-size)
      mt (- 1 t)
      (array-get out (- i start))
      (+ (* p-start.y mt mt mt) (* (struct-get (array-get p-rest 0) y) 3 mt mt t)
        (* (struct-get (array-get p-rest 1) y) 3 mt t t) (* p-end.y t t t)))))

(define (complex-difference p1 p2) (spline-path-point-t spline-path-point-t spline-path-point-t)
  (declare result spline-path-point-t)
  (set result.x (- p1.x p2.x) result.y (- p1.y p2.y))
  (return result))

(define (complex-multiplication p1 p2)
  (spline-path-point-t spline-path-point-t spline-path-point-t)
  (declare result spline-path-point-t)
  (set result.x (- (* p1.x p2.x) (* p1.y p2.y)) result.y (+ (* p1.x p2.y) (* p1.y p2.x)))
  (return result))

(define (complex-inversion p) (spline-path-point-t spline-path-point-t)
  (declare scale spline-path-value-t result spline-path-point-t)
  (set scale (/ 1 (+ (* p.x p.x) (* p.y p.y))) result.x (* scale p.x) result.y (* (- scale) p.y))
  (return result))

(define (complex-division p1 p2) (spline-path-point-t spline-path-point-t spline-path-point-t)
  (return (complex-multiplication p1 (complex-inversion p2))))

(define (complex-linear-interpolation p1 p2 t)
  (spline-path-point-t spline-path-point-t spline-path-point-t spline-path-value-t)
  (declare result spline-path-point-t)
  (set result.x (+ (* p1.x (- 1 t)) (* p2.x t)) result.y (+ (* p1.y (- 1 t)) (* p2.y t)))
  (return result))

(define (spline-path-i-circular-arc-control-point p1 p2 c)
  (spline-path-point-t spline-path-point-t spline-path-point-t spline-path-value-t)
  "return a point on a perpendicular line across the midpoint.
   calculates the midpoint, the negative reciprocal slope and a unit vector"
  (declare
    dx spline-path-value-t
    dy spline-path-value-t
    mx spline-path-value-t
    my spline-path-value-t
    d spline-path-value-t
    ux spline-path-value-t
    uy spline-path-value-t
    scale spline-path-value-t
    result spline-path-point-t)
  (set
    dx (- p2.x p1.x)
    dy (- p2.y p1.y)
    mx (/ (+ p1.x p2.x) 2)
    my (/ (+ p1.y p2.y) 2)
    d (sqrt (+ (* dx dx) (* dy dy)))
    ux (/ (- dy) d)
    uy (/ dx d)
    scale (* c (- p2.y my))
    result.x (+ mx (* ux scale))
    result.y (+ my (* uy scale)))
  (return result))

(declare spline-path-i-circular-arc-data-t
  (type
    (struct
      (m-a spline-path-point-t)
      (b-m spline-path-point-t)
      (ab-m spline-path-point-t)
      (bm-a spline-path-point-t)
      (s-size spline-path-value-t))))

(define (spline-path-i-circular-arc start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  "p-rest length 2. circular arc interpolation formula from jacob rus,
   https://observablehq.com/@jrus/circle-arc-interpolation"
  (declare t spline-path-value-t d spline-path-i-circular-arc-data-t p spline-path-point-t)
  (set d (pointer-get (convert-type data spline-path-i-circular-arc-data-t*)))
  (if (not d.s-size)
    (begin
      (declare
        dp spline-path-i-circular-arc-data-t*
        m spline-path-point-t
        p-end spline-path-point-t)
      (set
        p-end (array-get p-rest 1)
        dp data
        m (spline-path-i-circular-arc-control-point p-start p-end p-rest:y)
        dp:b-m (complex-difference p-end m)
        dp:m-a (complex-difference m p-start)
        dp:ab-m (complex-multiplication p-start dp:b-m)
        dp:bm-a (complex-multiplication p-end dp:m-a)
        dp:s-size (- p-end.x p-start.x)
        d *dp)))
  (for ((define i size-t start) (< i end) (set+ i 1))
    (set
      t (/ (- i p-start.x) d.s-size)
      p
      (complex-division (complex-linear-interpolation d.ab-m d.bm-a t)
        (complex-linear-interpolation d.b-m d.m-a t))
      (array-get out (- i start)) p.y)))

(define (spline-path-get path start end out)
  (void spline-path-t* size-t size-t spline-path-value-t*)
  "get values on path between start (inclusive) and end (exclusive).
   since x values are integers, a path from (0 0) to (10 20) for example would have reached 20 only at the 11th point.
   out memory is managed by the caller. the size required for out is end minus start"
  (sc-comment "find all segments that overlap with requested range")
  (declare
    i spline-path-segment-count-t
    s spline-path-segment-t
    s-start size-t
    s-end size-t
    out-start size-t
    segments-count spline-path-segment-count-t
    second-search uint8-t)
  (set segments-count path:segments-count i path:current-segment second-search #f)
  (while (< i segments-count)
    (set
      s (array-get path:segments i)
      s-start s._start.x
      s-end (struct-get (array-get s.points (- s._points-count 1)) x))
    (if (> s-start end)
      (if (or second-search (= 0 i)) break
        (begin (sc-comment "to allow randomly ordered access") (set i 0 second-search #t) continue)))
    (if (< s-end start) (begin (set+ i 1) continue))
    (set
      path:current-segment i
      out-start (if* (> s-start start) (- s-start start) 0)
      s-start (if* (> s-start start) s-start start)
      s-end (if* (< s-end end) s-end end))
    (s.interpolator s-start s-end s._start s.points s.data (+ out-start out))
    (set+ i 1))
  (sc-comment
    "outside points are zero. set the last segment point which would be set by a following segment."
    "can only be true for the last segment")
  (if (> end s-end)
    (begin
      (set
        (array-get out s-end) (struct-get (array-get s.points (- s._points-count 1)) y)
        s-end (+ 1 s-end))
      (if (> end s-end) (memset (+ s-end out) 0 (* (- end s-end) (sizeof spline-path-value-t)))))))

(define (spline-path-i-path start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  "p-rest length 0. data is one spline-path-t"
  (spline-path-get data (- start p-start.x) (- end p-start.x) out))

(define (spline-path-start path) (spline-path-point-t spline-path-t)
  (declare p spline-path-point-t s spline-path-segment-t)
  (set s (array-get path.segments 0))
  (if (= spline-path-i-move s.interpolator) (set p (array-get s.points 0)) (set p.x 0 p.y 0))
  (return p))

(define (spline-path-end path) (spline-path-point-t spline-path-t)
  "ends at constants"
  (declare s spline-path-segment-t)
  (set s (array-get path.segments (- path.segments-count 1)))
  (if (= spline-path-i-constant s.interpolator)
    (set s (array-get path.segments (- path.segments-count 2))))
  (return (array-get s.points (- s._points-count 1))))

(define (spline-path-size path) (size-t spline-path-t)
  (declare p spline-path-point-t)
  (set p (spline-path-end path))
  (return p.x))

(define (spline-path-prepare-segments segments segments-count)
  (void spline-path-segment-t* spline-path-segment-count-t)
  "set _start and _points_count for segments"
  (declare i spline-path-segment-count-t s spline-path-segment-t start spline-path-point-t)
  (set start.x 0 start.y 0)
  (for ((set i 0) (< i segments-count) (set+ i 1))
    (set
      (struct-get (array-get segments i) _start) start
      s (array-get segments i)
      s._points-count (spline-path-segment-points-count s))
    (case = s.interpolator
      (spline-path-i-path
        (set
          start (spline-path-end (pointer-get (convert-type s.data spline-path-t*)))
          *s.points start))
      (spline-path-i-constant (set *s.points start s.points:x spline-path-size-max))
      (else (set start (array-get s.points (- s._points-count 1)))))
    (set (array-get segments i) s)))

(define (spline-path-set path segments segments-count)
  (void spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  "set segments for a path and initialise it"
  (spline-path-prepare-segments segments segments-count)
  (set path:segments segments path:segments-count segments-count path:current-segment 0))

(define (spline-path-set-copy path segments segments-count)
  (uint8-t spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  "like spline-path-set but copies segments to new memory in .segments that has to be freed
   when not needed anymore"
  (define s spline-path-segment-t* (malloc (* segments-count (sizeof spline-path-segment-t))))
  (if (not s) (return 1))
  (memcpy s segments (* segments-count (sizeof spline-path-segment-t)))
  (spline-path-set path s segments-count)
  (return 0))

(define (spline-path-segments-get segments segments-count start end out)
  (uint8-t spline-path-segment-t* spline-path-segment-count-t size-t size-t spline-path-value-t*)
  "create a path array immediately from segments without creating a path object"
  (declare path spline-path-t)
  (spline-path-set &path segments segments-count)
  (spline-path-get &path start end out)
  (return 0))

(define (spline-path-move x y) (spline-path-segment-t size-t spline-path-value-t)
  "returns a move segment to move to the specified point"
  (declare s spline-path-segment-t)
  (set s.interpolator spline-path-i-move s.points:x x s.points:y y s.free 0)
  (return s))

(define (spline-path-line x y) (spline-path-segment-t size-t spline-path-value-t)
  (declare s spline-path-segment-t)
  (set s.interpolator spline-path-i-line s.points:x x s.points:y y s.free 0)
  (return s))

(define (spline-path-circular-arc curvature x2 y2)
  (spline-path-segment-t spline-path-value-t size-t spline-path-value-t)
  "curvature is a real between -1..1, with the maximum being the edge of the segment"
  (declare s spline-path-segment-t d spline-path-i-circular-arc-data-t*)
  (set d (malloc (sizeof spline-path-i-circular-arc-data-t)))
  (if d
    (set
      s.free free
      d:s-size 0
      s.data d
      s.interpolator spline-path-i-circular-arc
      s.points:y curvature
      (: (+ 1 s.points) x) x2
      (: (+ 1 s.points) y) y2)
    (set s.free 0 s.interpolator spline-path-i-constant))
  (return s))

(define (spline-path-bezier x1 y1 x2 y2 x3 y3)
  (spline-path-segment-t size-t spline-path-value-t size-t spline-path-value-t size-t spline-path-value-t)
  "the first two points are the control points"
  (declare s spline-path-segment-t)
  (set
    s.free 0
    s.interpolator spline-path-i-bezier
    s.points:x x1
    s.points:y y1
    (: (+ 1 s.points) x) x2
    (: (+ 1 s.points) y) y2
    (: (+ 2 s.points) x) x3
    (: (+ 2 s.points) y) y3)
  (return s))

(define (spline-path-constant) (spline-path-segment-t)
  (declare s spline-path-segment-t)
  (set s.interpolator spline-path-i-constant s.free 0)
  (return s))

(define (spline-path-path path) (spline-path-segment-t spline-path-t)
  "return a segment that is another spline-path. length is the full length of the path.
   the path does not necessarily connect and is drawn as it would be on its own starting from the preceding segment"
  (declare s spline-path-segment-t)
  (set s.data (malloc (sizeof spline-path-t)))
  (if s.data
    (set
      (pointer-get (convert-type s.data spline-path-t*)) path
      s.interpolator spline-path-i-path
      s.free free)
    (set s.interpolator spline-path-i-constant s.free 0))
  (return s))

(define (spline-path-free path) (void spline-path-t)
  "only needs to be called if a segment with state has been used,
   which is currently only spline_path_path"
  (declare s spline-path-segment-t* i spline-path-segment-count-t)
  (for ((set i 0) (< i path.segments-count) (set+ i 1))
    (set s (+ i path.segments))
    (if s:free (s:free s:data))))