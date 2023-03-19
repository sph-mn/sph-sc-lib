(pre-include "float.h" "strings.h" "stdlib.h" "math.h")

(pre-define
  (spline-path-min a b) (if* (< a b) a b)
  (spline-path-max a b) (if* (> a b) a b)
  (spline-path-abs a) (if* (> 0 a) (* -1 a) a)
  (spline-path-cheap-round-positive a) (convert-type (+ 0.5 a) size-t)
  (spline-path-limit x min-value max-value) (spline-path-max min-value (spline-path-min max-value x))
  (spline-path-i-bezier-interpolate mt t a b c d)
  (+ (* a mt mt mt) (* b 3 mt mt t) (* c 3 mt t t) (* d t t t))
  (lerp a b t) (+ a (* t (- b a)))
  (convert-point-x x x-min x-max)
  (convert-type (spline-path-cheap-round-positive (spline-path-limit x x-min x-max)) size-t))

(define (spline-path-perpendicular-point p1 p2 distance-factor)
  (spline-path-point-t spline-path-point-t spline-path-point-t spline-path-value-t)
  "return a point on a perpendicular line across the midpoint of a line between p1 and p2.
   can be used to find control points.
   distance-factor at -1 and 1 are the bounds of a rectangle where p1 and p2 are diagonally opposed edges,
   so that the distance will not be below or above the x and y values of the given points.
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
    scale (* distance-factor (/ (spline-path-min (spline-path-abs dx) (spline-path-abs dy)) 4.0))
    result.x (+ mx (* ux scale))
    result.y (+ my (* uy scale)))
  (return result))

(define (spline-path-set-missing-points out start end) (void spline-path-value-t* size-t size-t)
  "add missing intermediate points by interpolating between neighboring points.
   for interpolation methods that return float values for x that dont map directly to the currently interpolated index
   and may therefore leave gaps.
   assumes unset output values are 0"
  (declare i2 size-t b-size size-t)
  (set b-size (- end start))
  (for ((define i size-t 1) (< i b-size) (set+ i 1))
    (if (= 0.0 (array-get out i))
      (begin
        (set i2 (+ i 1))
        (while (< i2 b-size) (if (= 0.0 (array-get out i2)) (set+ i2 1) break))
        (if (< i2 b-size)
          (set (array-get out i) (lerp (array-get out (- i 1)) (array-get out i2) (/ 0.5 (- i2 i)))))))))

(define (spline-path-i-move start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void** spline-path-value-t*)
  "p_rest length 1"
  (memset out 0 (* (sizeof spline-path-value-t) (- end start))))

(define (spline-path-i-constant start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void** spline-path-value-t*)
  "p_rest length 0"
  (for ((define i size-t 0) (< i (- end start)) (set+ i 1)) (set (array-get out i) p-start.y)))

(define (spline-path-i-line start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void** spline-path-value-t*)
  "p_rest length 1"
  (declare
    p-end spline-path-point-t
    t spline-path-value-t
    p-start-x size-t
    s-size spline-path-value-t
    s-offset size-t)
  (set
    p-start-x p-start.x
    p-end (array-get p-rest 0)
    s-size (- p-end.x p-start.x)
    s-offset (- start p-start.x))
  (for ((define i size-t 0) (< i (- end start)) (set+ i 1))
    (set t (/ (+ i s-offset) s-size) (array-get out i) (lerp p-start.y p-end.y t))))

(define (spline-path-i-bezier start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void** spline-path-value-t*)
  "p_rest length 3"
  (declare
    b-size size-t
    i size-t
    ix size-t
    mt spline-path-value-t
    p-end spline-path-point-t
    p spline-path-point-t
    s-offset size-t
    s-size spline-path-value-t
    t spline-path-value-t)
  (set
    p-end (array-get p-rest 2)
    s-size (- p-end.x p-start.x)
    s-offset (- start p-start.x)
    b-size (- end start)
    i 0
    ix i)
  (while (< ix b-size)
    (set t (/ (+ i s-offset) s-size))
    (if (>= t 1.0) break)
    (set
      mt (- 1 t)
      p.x
      (spline-path-i-bezier-interpolate mt t
        p-start.x (struct-get (array-get p-rest 0) x) (struct-get (array-get p-rest 1) x) p-end.x)
      p.y
      (spline-path-i-bezier-interpolate mt t
        p-start.y (struct-get (array-get p-rest 0) y) (struct-get (array-get p-rest 1) y) p-end.y)
      ix (- (convert-point-x p.x start (- end 1)) start)
      (array-get out ix) p.y)
    (set+ i 1))
  (spline-path-set-missing-points out start end))

(define (spline-path-i-bezier-arc start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void** spline-path-value-t*)
  (if (not *data)
    (begin
      (declare
        distance spline-path-value-t
        p-end spline-path-point-t
        p-temp spline-path-point-t
        x-distance spline-path-value-t
        y-distance spline-path-value-t)
      (set
        distance (* p-rest:y (/ 4.0 3.0) (- (sqrt 2.0) 1.0))
        p-end (array-get p-rest 2)
        x-distance (if* (> p-end.x p-start.x) (- p-end.x p-start.x) (- p-start.x p-end.x))
        y-distance (if* (> p-end.y p-start.y) (- p-end.y p-start.y) (- p-start.y p-end.y))
        p-temp.x (+ p-end.x x-distance)
        p-temp.y (+ p-end.y y-distance)
        (array-get p-rest 1) (spline-path-perpendicular-point p-start p-temp distance)
        p-temp.x (- p-start.x x-distance)
        p-temp.y (- p-start.y y-distance)
        (array-get p-rest 0) (spline-path-perpendicular-point p-temp p-end distance)
        *data (convert-type 1 void*))))
  (spline-path-i-bezier start end p-start p-rest data out))

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
        (begin (sc-comment "to allow random access") (set i 0 second-search #t) continue)))
    (if (< s-end start) (begin (set+ i 1) continue))
    (set
      path:current-segment i
      out-start (if* (> s-start start) (- s-start start) 0)
      s-start (if* (> s-start start) s-start start)
      s-end (if* (< s-end end) s-end end))
    (s.interpolator s-start s-end s._start s.points &s.data (+ out-start out))
    (set+ i 1))
  (sc-comment "outside points are zero")
  (if (> end s-end)
    (begin
      (set out-start (if* (> start s-end) 0 (- s-end start)))
      (memset (+ out out-start) 0 (* (- end out-start) (sizeof spline-path-value-t))))))

(define (spline-path-i-path start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void** spline-path-value-t*)
  "p_rest length 0. data is one spline_path_t"
  (spline-path-get *data (- start p-start.x) (- end p-start.x) out))

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
      (spline-path-i-constant (set s.points:x spline-path-size-max s.points:y start.y))
      (else (set start (array-get s.points (- s._points-count 1)))))
    (set (array-get segments i) s)))

(define (spline-path-set path segments segments-count)
  (void spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  "set segments for a path and initialize it"
  (spline-path-prepare-segments segments segments-count)
  (set path:segments segments path:segments-count segments-count path:current-segment 0))

(define (spline-path-set-copy path segments segments-count)
  (uint8-t spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  "like spline_path_set but copies segments to new memory in .segments that has to be freed
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
  (spline-path-free path)
  (return 0))

(define (spline-path-move x y) (spline-path-segment-t spline-path-value-t spline-path-value-t)
  "returns a move segment to move to the specified point"
  (declare s spline-path-segment-t)
  (set s.interpolator spline-path-i-move s.points:x x s.points:y y s.free 0)
  (return s))

(define (spline-path-line x y) (spline-path-segment-t spline-path-value-t spline-path-value-t)
  (declare s spline-path-segment-t)
  (set s.interpolator spline-path-i-line s.points:x x s.points:y y s.free 0)
  (return s))

(define (spline-path-bezier x1 y1 x2 y2 x3 y3)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t spline-path-value-t spline-path-value-t spline-path-value-t)
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

(define (spline-path-bezier-arc x y curvature)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t)
  "curvature is a real between -1..1, with the maximum being the edge of the segment"
  (declare s spline-path-segment-t)
  (set
    s.free 0
    s.data 0
    s.interpolator spline-path-i-bezier-arc
    s.points:y curvature
    (: (+ 2 s.points) x) x
    (: (+ 2 s.points) y) y)
  (return s))

(define (spline-path-constant) spline-path-segment-t
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