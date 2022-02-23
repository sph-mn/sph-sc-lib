
/* * spline-path creates discrete 2d paths interpolated between some given points
 * maps from one independent value to one dependent continuous value
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
 * segments draw to the endpoint inclusive, start point exclusive
 * spline-path-interpolator-points-count */
#include <inttypes.h>
#include <float.h>
#include <strings.h>
#include <stdlib.h>

/* spline-path-size-max must be a value that fits in spline-path-value-t and size-t */

#ifndef spline_path_value_t
#define spline_path_value_t double
#endif
#ifndef spline_path_segment_count_t
#define spline_path_segment_count_t uint16_t
#endif
#ifndef spline_path_size_max
#define spline_path_size_max (SIZE_MAX / 2)
#endif
#define spline_path_segment_points_count(s) ((spline_path_i_bezier == s.interpolator) ? 3 : ((spline_path_i_circular_arc == s.interpolator) ? 2 : 1))
typedef struct {
  spline_path_value_t x;
  spline_path_value_t y;
} spline_path_point_t;
typedef void (*spline_path_interpolator_t)(size_t, size_t, spline_path_point_t, spline_path_point_t*, void*, spline_path_value_t*);
typedef struct {
  spline_path_point_t _start;
  uint8_t _points_count;
  spline_path_point_t points[3];
  spline_path_interpolator_t interpolator;
  void* data;
  void (*free)(void*);
} spline_path_segment_t;
typedef struct {
  spline_path_segment_count_t segments_count;
  spline_path_segment_t* segments;
  spline_path_segment_count_t current_segment;
} spline_path_t;
void spline_path_i_move(size_t start, size_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out);
void spline_path_i_constant(size_t start, size_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out);
void spline_path_i_line(size_t start, size_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out);
void spline_path_i_bezier(size_t start, size_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out);
void spline_path_get(spline_path_t* path, size_t start, size_t end, spline_path_value_t* out);
void spline_path_i_path(size_t start, size_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out);
spline_path_point_t spline_path_start(spline_path_t path);
spline_path_point_t spline_path_end(spline_path_t path);
void spline_path_set(spline_path_t* path, spline_path_segment_t* segments, spline_path_segment_count_t segments_count);
uint8_t spline_path_set_copy(spline_path_t* path, spline_path_segment_t* segments, spline_path_segment_count_t segments_count);
uint8_t spline_path_segments_get(spline_path_segment_t* segments, spline_path_segment_count_t segments_count, size_t start, size_t end, spline_path_value_t* out);
spline_path_segment_t spline_path_move(size_t x, spline_path_value_t y);
spline_path_segment_t spline_path_line(size_t x, spline_path_value_t y);
spline_path_segment_t spline_path_bezier(size_t x1, spline_path_value_t y1, size_t x2, spline_path_value_t y2, size_t x3, spline_path_value_t y3);
spline_path_segment_t spline_path_constant();
spline_path_segment_t spline_path_path(spline_path_t path);
void spline_path_prepare_segments(spline_path_segment_t* segments, spline_path_segment_count_t segments_count);
size_t spline_path_size(spline_path_t path);
void spline_path_free(spline_path_t path);
void spline_path_i_circular_arc(size_t start, size_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out);
spline_path_segment_t spline_path_circular_arc(spline_path_value_t curvature, size_t x2, spline_path_value_t y2);
spline_path_point_t spline_path_i_circular_arc_control_point(spline_path_point_t p1, spline_path_point_t p2, spline_path_value_t c);