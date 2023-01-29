(sc-comment
  (pre-define (spline-path-segment-points-count s)
    (case* = s.interpolator
      ((spline-path-i-bezier spline-path-i-bezier-arc) 3)
      (spline-path-i-circular-arc 2)
      (else 1)))
  (spline-path-i-circular-arc start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void** spline-path-value-t*)
  (spline-path-circular-arc curvature x y)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t))

(declare spline-path-i-circular-arc-data-t
  (type
    (struct
      (m-a spline-path-point-t)
      (b-m spline-path-point-t)
      (ab-m spline-path-point-t)
      (bm-a spline-path-point-t)
      (s-size spline-path-value-t))))

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

(define (spline-path-i-circular-arc start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void** spline-path-value-t*)
  "p-rest length 2. circular arc interpolation formula from jacob rus,
   https://observablehq.com/@jrus/circle-arc-interpolation"
  (declare
    b-size size-t
    d spline-path-i-circular-arc-data-t
    i size-t
    ix size-t
    p-end spline-path-point-t
    p spline-path-point-t
    s-offset size-t
    s-size spline-path-value-t
    t spline-path-value-t)
  (set
    d (pointer-get (convert-type *data spline-path-i-circular-arc-data-t*))
    p-end (array-get p-rest 1)
    b-size (- end start)
    s-size (- p-end.x p-start.x)
    s-offset (- start p-start.x))
  (if (not d.s-size)
    (begin
      (declare dp spline-path-i-circular-arc-data-t* m spline-path-point-t)
      (set
        dp *data
        m (spline-path-perpendicular-point p-start p-end p-rest:y)
        dp:b-m (complex-difference p-end m)
        dp:m-a (complex-difference m p-start)
        dp:ab-m (complex-multiplication p-start dp:b-m)
        dp:bm-a (complex-multiplication p-end dp:m-a)
        dp:s-size (- p-end.x p-start.x)
        d *dp)))
  (set i 0 ix i)
  (while (< ix b-size)
    (set t (/ (+ i s-offset) s-size))
    (if (>= t 1.0) break)
    (set
      p
      (complex-division (complex-linear-interpolation d.ab-m d.bm-a t)
        (complex-linear-interpolation d.b-m d.m-a t))
      ix (- (convert-point-x p.x start end) start)
      (array-get out ix) p.y)
    (set+ i 1))
  (spline-path-set-missing-points out start end))

(define (spline-path-circular-arc curvature x y)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t)
  "curvature is a real between -1..1, with the maximum being the edge of the segment.
   the current implementation turned out to be extremely slow"
  (declare s spline-path-segment-t d spline-path-i-circular-arc-data-t*)
  (set d (malloc (sizeof spline-path-i-circular-arc-data-t)))
  (if d
    (set
      s.free free
      d:s-size 0
      s.data d
      s.interpolator spline-path-i-circular-arc
      s.points:y curvature
      (: (+ 1 s.points) x) x
      (: (+ 1 s.points) y) y)
    (set s.free 0 s.interpolator spline-path-i-constant))
  (return s))