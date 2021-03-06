(sc-comment
  "* spline-path creates discrete 2d paths interpolated between some given points
   * maps from one independent discrete value to one dependent continuous value
   * only the dependent value is returned
   * kept minimal (only 2d, only selected interpolators, limited segment count) to be extremely fast
   * multidimensional interpolation can be archieved with multiple configs and calls
   * a copy of segments is made internally and only the copy is used
   * uses points as structs because pre-defined size arrays can not be used in structs
   * segments-count must be greater than zero
   * segments must be a valid spline-path segment configuration
   * interpolators are called with path-relative start/end inside segment and with out positioned at the segment output
   * all segment types require a fixed number of given points. line: 1, bezier: 3, move: 1, constant: 0, path: 0
   * negative x values not supported
   * internally all segments start at (0 0) and no gaps are between segments
   * assumes that bit 0 is spline-path-value-t zero
   * segments draw to the endpoint inclusive, start point exclusive
   * spline-path-interpolator-points-count")

(pre-include "inttypes.h" "strings.h" "stdlib.h")

(pre-define-if-not-defined
  spline-path-time-t uint32-t
  spline-path-value-t double
  spline-path-segment-count-t uint16-t
  spline-path-time-max UINT32_MAX)

(pre-define (spline-path-segment-points-count s) (if* (= spline-path-i-bezier s.interpolator) 3 1))

(declare
  spline-path-point-t (type (struct (x spline-path-time-t) (y spline-path-value-t)))
  spline-path-interpolator-t
  (type
    (function-pointer void spline-path-time-t
      spline-path-time-t spline-path-point-t spline-path-point-t* void* spline-path-value-t*))
  spline-path-segment-t
  (type
    (struct
      (_start spline-path-point-t)
      (_points-count uint8-t)
      (points (array spline-path-point-t 3))
      (interpolator spline-path-interpolator-t)
      (options void*)))
  spline-path-t
  (type (struct (segments-count spline-path-segment-count-t) (segments spline-path-segment-t*)))
  (spline-path-i-move start end p-start p-rest options out)
  (void spline-path-time-t spline-path-time-t
    spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-i-constant start end p-start p-rest options out)
  (void spline-path-time-t spline-path-time-t
    spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-i-line start end p-start p-rest options out)
  (void spline-path-time-t spline-path-time-t
    spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-i-bezier start end p-start p-rest options out)
  (void spline-path-time-t spline-path-time-t
    spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-get path start end out)
  (void spline-path-t spline-path-time-t spline-path-time-t spline-path-value-t*)
  (spline-path-i-path start end p-start p-rest options out)
  (void spline-path-time-t spline-path-time-t
    spline-path-point-t spline-path-point-t* void* spline-path-value-t*)
  (spline-path-start path) (spline-path-point-t spline-path-t)
  (spline-path-end path) (spline-path-point-t spline-path-t)
  (spline-path-set path segments segments-count)
  (void spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  (spline-path-set-copy path segments segments-count)
  (uint8-t spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  (spline-path-segments-get segments segments-count start end out)
  (uint8-t spline-path-segment-t* spline-path-segment-count-t
    spline-path-time-t spline-path-time-t spline-path-value-t*)
  (spline-path-move x y) (spline-path-segment-t spline-path-time-t spline-path-value-t)
  (spline-path-line x y) (spline-path-segment-t spline-path-time-t spline-path-value-t)
  (spline-path-bezier x1 y1 x2 y2 x3 y3)
  (spline-path-segment-t spline-path-time-t spline-path-value-t
    spline-path-time-t spline-path-value-t spline-path-time-t spline-path-value-t)
  (spline-path-constant) (spline-path-segment-t)
  (spline-path-path path) (spline-path-segment-t spline-path-t*)
  (spline-path-prepare-segments segments segments-count)
  (void spline-path-segment-t* spline-path-segment-count-t)
  (spline-path-size path) (spline-path-time-t spline-path-t))