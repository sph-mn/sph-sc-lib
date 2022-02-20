
/* depends on spline-path-h.c */
#include <math.h>

/** p-rest length 1 */
void spline_path_i_move(spline_path_time_t start, spline_path_time_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out) { memset(out, 0, (sizeof(spline_path_value_t) * (end - start))); }

/** p-rest length 0 */
void spline_path_i_constant(spline_path_time_t start, spline_path_time_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out) {
  spline_path_time_t i;
  for (i = start; (i < end); i += 1) {
    out[(i - start)] = p_start.y;
  };
}

/** p-rest length 1 */
void spline_path_i_line(spline_path_time_t start, spline_path_time_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out) {
  spline_path_time_t i;
  spline_path_point_t p_end;
  spline_path_value_t s_size;
  spline_path_value_t t;
  p_end = p_rest[0];
  s_size = (p_end.x - p_start.x);
  for (i = start; (i < end); i += 1) {
    t = ((i - p_start.x) / s_size);
    out[(i - start)] = ((p_end.y * t) + (p_start.y * (1 - t)));
  };
}

/** p-rest length 3. this implementation ignores control point x values */
void spline_path_i_bezier(spline_path_time_t start, spline_path_time_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out) {
  spline_path_time_t i;
  spline_path_value_t mt;
  spline_path_point_t p_end;
  spline_path_value_t s_size;
  spline_path_value_t t;
  p_end = p_rest[2];
  s_size = (p_end.x - p_start.x);
  for (i = start; (i < end); i += 1) {
    t = ((i - p_start.x) / s_size);
    mt = (1 - t);
    out[(i - start)] = ((p_start.y * mt * mt * mt) + ((p_rest[0]).y * 3 * mt * mt * t) + ((p_rest[1]).y * 3 * mt * t * t) + (p_end.y * t * t * t));
  };
}
spline_path_point_calc_t complex_difference(spline_path_point_calc_t p1, spline_path_point_calc_t p2) {
  spline_path_point_calc_t result;
  result.x = (p1.x - p2.x);
  result.y = (p1.y - p2.y);
  return (result);
}
spline_path_point_t complex_multiplication(spline_path_point_calc_t p1, spline_path_point_calc_t p2) {
  spline_path_point_calc_t result;
  result.x = ((p1.x * p2.x) - (p1.y * p2.y));
  result.y = ((p1.x * p2.y) - (p1.y * p2.x));
  return (result);
}
spline_path_point_calc_t complex_inversion(spline_path_point_calc_t p) {
  spline_path_value_t scale;
  spline_path_point_calc_t result;
  scale = (1 / ((p.x * p.x) + (p.y * p.y)));
  result.x = (scale * p.x);
  result.y = ((-scale) * p.y);
  return (result);
}
spline_path_point_calc_t complex_division(spline_path_point_calc_t p1, spline_path_point_calc_t p2) { return ((complex_multiplication(p1, (complex_inversion(p2))))); }
spline_path_point_calc_t complex_linear_interpolation(spline_path_point_calc_t p1, spline_path_point_calc_t p2, spline_path_value_t t) {
  spline_path_point_calc_t result;
  result.x = ((p1.x * (1 - t)) + (p2.x * t));
  result.y = ((p1.y * (1 - t)) + (p2.y * t));
  printf("%lu %f %f\n", (result.x), (p2.x * t), t);
  return (result);
}
#include <stdio.h>

/** return a point on a perpendicular line across the midpoint */
spline_path_point_calc_t spline_path_i_circular_arc_control_point(spline_path_point_calc_t p1, spline_path_point_calc_t p2, spline_path_value_t c) {
  spline_path_value_t dx;
  spline_path_value_t dy;
  spline_path_value_t mx;
  spline_path_value_t my;
  spline_path_value_t d;
  spline_path_value_t ux;
  spline_path_value_t uy;
  spline_path_value_t scale;
  spline_path_point_t result;
  dx = (p2.x - p1.x);
  dy = (p2.y - p1.y);
  mx = ((p1.x + p2.x) / 2);
  my = ((p1.y + p2.y) / 2);
  d = sqrt(((dx * dx) + (dy * dy)));
  ux = ((-dy) / d);
  uy = (dx / d);
  scale = (c * (p2.y - my));
  result.x = ceil((mx + (ux * scale)));
  result.y = (my + (uy * scale));
  return (result);
}
typedef struct {
  spline_path_point_calc_t m_a;
  spline_path_point_calc_t b_m;
  spline_path_point_calc_t ab_m;
  spline_path_point_calc_t bm_a;
  spline_path_value_t s_size;
} spline_path_i_circular_arc_data_t;
/** p-rest length 2. circular arc interpolation formula from jacob rus,
   https://observablehq.com/@jrus/circle-arc-interpolation */
void spline_path_i_circular_arc(spline_path_time_t start, spline_path_time_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out) {
  spline_path_value_t t;
  spline_path_i_circular_arc_data_t d;
  spline_path_point_calc_t p;
  d = *((spline_path_i_circular_arc_data_t*)(data));
  if (!d.s_size) {
    spline_path_i_circular_arc_data_t* dp;
    spline_path_value_t curvature;
    spline_path_point_calc_t m;
    spline_path_point_calc_t p_start_calc;
    spline_path_point_calc_t p_end;
    curvature = p_rest->y;
    p_start_calc.x = p_start.x;
    p_start_calc.y = p_start.y;
    p_end_calc.x = (p_rest[1])->x;
    p_end_calc.y = (p_rest[1])->y;
    dp = data;
    m = spline_path_i_circular_arc_control_point(p_start_calc, p_end_calc, curvature);
    dp->b_m = complex_difference(p_end_calc, m);
    dp->m_a = complex_difference(m, p_start);
    dp->ab_m = complex_multiplication(p_start, (dp->b_m));
    dp->bm_a = complex_multiplication(p_end_calc, (dp->m_a));
    dp->s_size = (p_end_calc.x - p_start_calc.x);
    d = *dp;
  };
  for (spline_path_time_t i = start; (i < end); i += 1) {
    t = ((i - p_start.x) / d.s_size);
    p = complex_division((complex_linear_interpolation((d.ab_m), (d.bm_a), t)), (complex_linear_interpolation((d.b_m), (d.m_a), t)));
    out[(i - start)] = p.y;
  };
}

/** get values on path between start (inclusive) and end (exclusive).
   since x values are integers, a path from (0 0) to (10 20) for example would have reached 20 only at the 11th point.
   out memory is managed by the caller. the size required for out is end minus start */
void spline_path_get(spline_path_t* path, spline_path_time_t start, spline_path_time_t end, spline_path_value_t* out) {
  /* find all segments that overlap with requested range */
  spline_path_segment_count_t i;
  spline_path_segment_t s;
  spline_path_time_t s_start;
  spline_path_time_t s_end;
  spline_path_time_t out_start;
  spline_path_segment_count_t segments_count;
  uint8_t second_search;
  segments_count = path->segments_count;
  i = path->current_segment;
  second_search = 0;
  while ((i < segments_count)) {
    s = (path->segments)[i];
    s_start = s._start.x;
    s_end = ((s.points)[(s._points_count - 1)]).x;
    if (s_start > end) {
      if (second_search || (0 == i)) {
        break;
      } else {
        /* to allow randomly ordered access */
        i = 0;
        second_search = 1;
        continue;
      };
    };
    if (s_end < start) {
      i += 1;
      continue;
    };
    path->current_segment = i;
    out_start = ((s_start > start) ? (s_start - start) : 0);
    s_start = ((s_start > start) ? s_start : start);
    s_end = ((s_end < end) ? s_end : end);
    (s.interpolator)(s_start, s_end, (s._start), (s.points), (s.data), (out_start + out));
    i += 1;
  };
  /* outside points are zero. set the last segment point which would be set by a following segment.
  can only be true for the last segment */
  if (end > s_end) {
    out[s_end] = ((s.points)[(s._points_count - 1)]).y;
    s_end = (1 + s_end);
    if (end > s_end) {
      memset((s_end + out), 0, ((end - s_end) * sizeof(spline_path_value_t)));
    };
  };
}

/** p-rest length 0. data is one spline-path-t */
void spline_path_i_path(spline_path_time_t start, spline_path_time_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void* data, spline_path_value_t* out) { spline_path_get(data, (start - p_start.x), (end - p_start.x), out); }
spline_path_point_t spline_path_start(spline_path_t path) {
  spline_path_point_t p;
  spline_path_segment_t s;
  s = (path.segments)[0];
  if (spline_path_i_move == s.interpolator) {
    p = (s.points)[0];
  } else {
    p.x = 0;
    p.y = 0;
  };
  return (p);
}

/** ends at constants */
spline_path_point_t spline_path_end(spline_path_t path) {
  spline_path_segment_t s;
  s = (path.segments)[(path.segments_count - 1)];
  if (spline_path_i_constant == s.interpolator) {
    s = (path.segments)[(path.segments_count - 2)];
  };
  return (((s.points)[(s._points_count - 1)]));
}
spline_path_time_t spline_path_size(spline_path_t path) {
  spline_path_point_t p;
  p = spline_path_end(path);
  return ((p.x));
}

/** set _start and _points_count for segments */
void spline_path_prepare_segments(spline_path_segment_t* segments, spline_path_segment_count_t segments_count) {
  spline_path_segment_count_t i;
  spline_path_segment_t s;
  spline_path_point_t start;
  start.x = 0;
  start.y = 0;
  for (i = 0; (i < segments_count); i += 1) {
    (segments[i])._start = start;
    s = segments[i];
    s._points_count = spline_path_segment_points_count(s);
    if (spline_path_i_path == s.interpolator) {
      start = spline_path_end((*((spline_path_t*)(s.data))));
      *(s.points) = start;
    } else if (spline_path_i_constant == s.interpolator) {
      *(s.points) = start;
      (s.points)->x = spline_path_time_max;
    } else {
      start = (s.points)[(s._points_count - 1)];
    };
    segments[i] = s;
  };
}

/** set segments for a path and initialise it */
void spline_path_set(spline_path_t* path, spline_path_segment_t* segments, spline_path_segment_count_t segments_count) {
  spline_path_prepare_segments(segments, segments_count);
  path->segments = segments;
  path->segments_count = segments_count;
  path->current_segment = 0;
}

/** like spline-path-set but copies segments to new memory in .segments that has to be freed
   when not needed anymore */
uint8_t spline_path_set_copy(spline_path_t* path, spline_path_segment_t* segments, spline_path_segment_count_t segments_count) {
  spline_path_segment_t* s = malloc((segments_count * sizeof(spline_path_segment_t)));
  if (!s) {
    return (1);
  };
  memcpy(s, segments, (segments_count * sizeof(spline_path_segment_t)));
  spline_path_set(path, s, segments_count);
  return (0);
}

/** create a path array immediately from segments without creating a path object */
uint8_t spline_path_segments_get(spline_path_segment_t* segments, spline_path_segment_count_t segments_count, spline_path_time_t start, spline_path_time_t end, spline_path_value_t* out) {
  spline_path_t path;
  spline_path_set((&path), segments, segments_count);
  spline_path_get((&path), start, end, out);
  return (0);
}

/** returns a move segment to move to the specified point */
spline_path_segment_t spline_path_move(spline_path_time_t x, spline_path_value_t y) {
  spline_path_segment_t s;
  s.interpolator = spline_path_i_move;
  (s.points)->x = x;
  (s.points)->y = y;
  s.free = 0;
  return (s);
}
spline_path_segment_t spline_path_line(spline_path_time_t x, spline_path_value_t y) {
  spline_path_segment_t s;
  s.interpolator = spline_path_i_line;
  (s.points)->x = x;
  (s.points)->y = y;
  s.free = 0;
  return (s);
}

/** curvature is a real between -1..1, with the maximum being the edge of the segment */
spline_path_segment_t spline_path_circular_arc(spline_path_value_t curvature, spline_path_time_t x2, spline_path_value_t y2) {
  spline_path_segment_t s;
  spline_path_i_circular_arc_data_t* d;
  d = malloc((sizeof(spline_path_i_circular_arc_data_t)));
  if (d) {
    s.free = free;
    d->s_size = 0;
    s.data = d;
    s.interpolator = spline_path_i_circular_arc;
    (s.points)->y = curvature;
    (1 + s.points)->x = x2;
    (1 + s.points)->y = y2;
  } else {
    s.free = 0;
    s.interpolator = spline_path_i_constant;
  };
  return (s);
}

/** the first two points are the control points */
spline_path_segment_t spline_path_bezier(spline_path_time_t x1, spline_path_value_t y1, spline_path_time_t x2, spline_path_value_t y2, spline_path_time_t x3, spline_path_value_t y3) {
  spline_path_segment_t s;
  s.free = 0;
  s.interpolator = spline_path_i_bezier;
  (s.points)->x = x1;
  (s.points)->y = y1;
  (1 + s.points)->x = x2;
  (1 + s.points)->y = y2;
  (2 + s.points)->x = x3;
  (2 + s.points)->y = y3;
  return (s);
}
spline_path_segment_t spline_path_constant() {
  spline_path_segment_t s;
  s.interpolator = spline_path_i_constant;
  s.free = 0;
  return (s);
}

/** return a segment that is another spline-path. length is the full length of the path.
   the path does not necessarily connect and is drawn as it would be on its own starting from the preceding segment */
spline_path_segment_t spline_path_path(spline_path_t path) {
  spline_path_segment_t s;
  s.data = malloc((sizeof(spline_path_t)));
  if (s.data) {
    *((spline_path_t*)(s.data)) = path;
    s.interpolator = spline_path_i_path;
    s.free = free;
  } else {
    s.interpolator = spline_path_i_constant;
    s.free = 0;
  };
  return (s);
}

/** only needs to be called if a segment with state has been used,
   which is currently only spline_path_path */
void spline_path_free(spline_path_t path) {
  spline_path_segment_t* s;
  spline_path_segment_count_t i;
  for (i = 0; (i < path.segments_count); i += 1) {
    s = (i + path.segments);
    if (s->free) {
      (s->free)((s->data));
    };
  };
}
