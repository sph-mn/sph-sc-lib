(sc-comment
  "* spline-path creates discrete 2d paths interpolated between some given points
   * maps from one independent value to one dependent continuous value
   * only the dependent value is returned
   * kept minimal (only 2d, only selected interpolators, limited segment count) to be extremely fast
   * negative independent values are not supported
   * segments-count must be greater than zero
   * multidimensional interpolation could only be archieved with multiple configs and calls
   * a copy of segments is made internally and only the copy is used
   * uses points as structs because pre-defined size arrays can not be used in structs
   * segments must be a valid spline-path segment configuration
   * interpolators are called with path-relative start/end inside segment and with out positioned at offset for this start/end block
   * all segment types require a fixed number of given points. line: 1, bezier: 3, move: 1, constant: 0, path: 0
   * segments start at the previous point or (0 0)
   * bezier and circular-arc interpolation assume that output array values are set to zero before use
   * segments draw from the start point inclusive to end point exclusive
   * both dimensions are float types for precision with internal calculations")

(pre-include "inttypes.h" "float.h" "strings.h" "stdlib.h")
(sc-comment "spline-path-size-max must be a value that fits in spline-path-value-t and size-t")

(pre-define-if-not-defined
  spline-path-value-t double
  spline-path-segment-count-t uint16-t
  spline-path-size-max (/ SIZE_MAX 2))

(pre-define (spline-path-segment-points-count s)
  (case* = s.interpolator (spline-path-i-bezier 3) (spline-path-i-circular-arc 2) (else 1)))

(declare
  spline-path-point-t (type (struct (x spline-path-value-t) (y spline-path-value-t)))
  spline-path-interpolator-t
  (type
    (function-pointer void size-t
      size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*))
  spline-path-segment-t
  (type
    (struct
      (_start spline-path-point-t)
      (_points-count uint8-t)
      (points (array spline-path-point-t 3))
      (interpolator spline-path-interpolator-t)
      (data void*)
      (free (function-pointer void void*))))
  spline-path-t
  (type
    (struct
      (segments-count spline-path-segment-count-t)
      (segments spline-path-segment-t*)
      (current-segment spline-path-segment-count-t)))
  (spline-path-i-move start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-i-constant start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-i-line start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-i-bezier start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-get path start end out) (void spline-path-t* size-t size-t spline-path-value-t*)
  (spline-path-i-path start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-start path) (spline-path-point-t spline-path-t)
  (spline-path-end path) (spline-path-point-t spline-path-t)
  (spline-path-set path segments segments-count)
  (void spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  (spline-path-set-copy path segments segments-count)
  (uint8-t spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  (spline-path-segments-get segments segments-count start end out)
  (uint8-t spline-path-segment-t* spline-path-segment-count-t size-t size-t spline-path-value-t*)
  (spline-path-move x y) (spline-path-segment-t spline-path-value-t spline-path-value-t)
  (spline-path-line x y) (spline-path-segment-t spline-path-value-t spline-path-value-t)
  (spline-path-bezier x1 y1 x2 y2 x3 y3)
  (spline-path-segment-t spline-path-value-t spline-path-value-t
    spline-path-value-t spline-path-value-t spline-path-value-t spline-path-value-t)
  (spline-path-constant) (spline-path-segment-t)
  (spline-path-path path) (spline-path-segment-t spline-path-t)
  (spline-path-prepare-segments segments segments-count)
  (void spline-path-segment-t* spline-path-segment-count-t)
  (spline-path-size path) (size-t spline-path-t)
  (spline-path-free path) (void spline-path-t)
  (spline-path-i-circular-arc start end p-start p-rest data out)
  (void size-t size-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-circular-arc curvature x2 y2)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t)
  (spline-path-perpendicular-point p1 p2 distance-factor)
  (spline-path-point-t spline-path-point-t spline-path-point-t spline-path-value-t))