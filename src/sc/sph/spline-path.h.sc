(pre-include-guard-begin sph-spline-path-h)

(sc-comment
  "* spline-path creates discrete 2d paths interpolated between some given points
   * maps from one independent value to one dependent continuous value
   * only the dependent value is returned
   * kept minimal (only 2d, only selected generates, limited segment count) to be fast
   * negative independent values are not supported
   * segments-count must be greater than zero
   * higher-dimensional interpolation could only be archieved with multiple configs and calls
   * a copy of segments is made internally and only the copy is used
   * uses points as structs because pre-defined size arrays can not be used in structs
   * segments must be a valid spline-path segment configuration
   * generates are called with path-relative start/end inside segment and with out positioned at offset for this start/end block
   * all segment types require a fixed number of given points. line: 1, bezier1: 3, bezier2: 4, move: 1, constant: 0, path: 0
   * segments start at the previous point or (0 0)
   * bezier interpolation assume that output array values are set to zero before use
   * bezier sampling interpolates x and y, then fills gaps linearly in x between produced samples
   * bezier output x over the segment is non-decreasing; shapes that fold back in x are unsupported
   * segments draw from the start point inclusive to end point exclusive
   * both dimensions are float types for precision with internal calculations
   * threads using the same path should work on separate copies of the path
   * start must be smaller than end
   * out must have length difference(end, start)
   * spline_path_validate is available to check structure and monotone-x rules before use
   * spline_path_size_max must be representable in spline_path_value_t and size_t")

(pre-include "inttypes.h" "stdint.h")
(sc-comment "spline-path-size-max must be a value that fits in spline-path-value-t and size-t")

(pre-define-if-not-defined
  spline-path-value-t double
  spline-path-segment-count-t uint16-t
  spline-path-size-max (/ SIZE_MAX 2)
  spline-path-point-max 4
  spline-path-fabs fabs
  spline-path-exp exp
  spline-path-pow pow
  spline-path-sqrt sqrt)

(declare
  spline-path-point-t (type (struct (x spline-path-value-t) (y spline-path-value-t)))
  spline-path-segment-t struct
  spline-path-segment-t
  (type
    (struct
      spline-path-segment-t
      (points-count uint8-t)
      (points (array spline-path-point-t spline-path-point-max))
      (generate
        (function-pointer void size-t size-t (struct spline-path-segment-t*) spline-path-value-t*))
      (prepare (function-pointer uint8-t (struct spline-path-segment-t*)))
      (free (function-pointer void void*))
      (data void*)))
  spline-path-generate-t
  (type (function-pointer void size-t size-t spline-path-segment-t* spline-path-value-t*))
  spline-path-prepare-t (type (function-pointer uint8-t spline-path-segment-t*))
  spline-path-t
  (type
    (struct
      (segments-count spline-path-segment-count-t)
      (segments spline-path-segment-t*)
      (current-segment-index spline-path-segment-count-t)
      (previous-start size-t)))
  (spline-path-start path) (spline-path-point-t spline-path-t)
  (spline-path-end path) (spline-path-point-t spline-path-t)
  (spline-path-set path segments segments-count)
  (uint8-t spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  (spline-path-set-copy path segments segments-count)
  (uint8-t spline-path-t* spline-path-segment-t* spline-path-segment-count-t)
  (spline-path-segments-get segments segments-count start end out)
  (uint8-t spline-path-segment-t* spline-path-segment-count-t size-t size-t spline-path-value-t*)
  (spline-path-prepare-segments segments segments-count)
  (uint8-t spline-path-segment-t* spline-path-segment-count-t)
  (spline-path-size path) (size-t spline-path-t)
  (spline-path-free path) (void spline-path-t)
  (spline-path-perpendicular-point p1 p2 distance-factor)
  (spline-path-point-t spline-path-point-t spline-path-point-t spline-path-value-t)
  (spline-path-move x y) (spline-path-segment-t spline-path-value-t spline-path-value-t)
  (spline-path-line x y) (spline-path-segment-t spline-path-value-t spline-path-value-t)
  (spline-path-constant) (spline-path-segment-t)
  (spline-path-path path) (spline-path-segment-t spline-path-t)
  (spline-path-bezier1 x1 y1 x2 y2)
  (spline-path-segment-t spline-path-value-t spline-path-value-t
    spline-path-value-t spline-path-value-t)
  (spline-path-bezier2 x1 y1 x2 y2 x3 y3)
  (spline-path-segment-t spline-path-value-t spline-path-value-t
    spline-path-value-t spline-path-value-t spline-path-value-t spline-path-value-t)
  (spline-path-power x y gamma)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t)
  (spline-path-exponential x y gamma)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t)
  (spline-path-bezier-arc x y curvature)
  (spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t)
  (spline-path-get path start end out) (void spline-path-t* size-t size-t spline-path-value-t*)
  (spline-path-path-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-line-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-move-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-constant-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-bezier1-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-bezier2-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-power-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-exponential-generate start end s out)
  (void size-t size-t spline-path-segment-t* spline-path-value-t*)
  (spline-path-debug-print path indent) (void spline-path-t* uint8-t)
  (spline-path-validate path log) (uint8-t spline-path-t* uint8-t))

(pre-include-guard-end)
