
#include <float.h>
#include <strings.h>
#include <stdlib.h>
#include <math.h>

#define spline_path_bezier_resolution 2
#define spline_path_cheap_round_positive(a) ((size_t)((0.5 + a)))
#define linearly_interpolate(a, b, t) (a + (t * (b - a)))
#define spline_path_bezier2_interpolate(mt, t, a, b, c, d) ((a * mt * mt * mt) + (b * 3 * mt * mt * t) + (c * 3 * mt * t * t) + (d * t * t * t))
#define spline_path_bezier1_interpolate(mt, t, a, b, c) ((a * mt * mt) + (b * 2 * mt * t) + (c * t * t))
#define spline_path_declare_segment(id) spline_path_segment_t id = { 0 }

/** get values on path between start (inclusive) and end (exclusive).
   since x values are integers, a path from (0 0) to (10 20) reaches 20 at the 11th point.
   out memory is managed by the caller. the size required for out is end minus start */
void spline_path_get(spline_path_t* path, size_t start, size_t end, spline_path_value_t* out) {
  spline_path_segment_t* s;
  size_t s_start;
  size_t s_end;
  size_t out_start;
  for (spline_path_segment_count_t i = ((start < path->previous_start) ? 0 : path->current_segment_index); (i < path->segments_count); path->current_segment_index = i, i += 1) {
    s = (path->segments + i);
    s_start = ((s->points)[0]).x;
    s_end = ((s->points)[(s->points_count - 1)]).x;
    path->previous_start = start;
    if (s_start > end) {
      break;
    };
    if (s_end < start) {
      continue;
    };
    out_start = ((s_start > start) ? (s_start - start) : 0);
    if (s_start < start) {
      s_start = start;
    };
    if (s_end > end) {
      s_end = end;
    };
    (s->generate)(s_start, s_end, s, (out_start + out));
  };
}
spline_path_point_t spline_path_start(spline_path_t path) { return ((*((path.segments)->points))); }
spline_path_point_t spline_path_end(spline_path_t path) {
  spline_path_segment_t* s;
  spline_path_point_t p;
  s = (path.segments + (path.segments_count - 1));
  p = (s->points)[(s->points_count - 1)];
  return (((spline_path_size_max == p.x) ? (s->points)[0] : p));
}
size_t spline_path_size(spline_path_t path) {
  spline_path_point_t p;
  p = spline_path_end(path);
  return ((p.x));
}
uint8_t spline_path_path_prepare(spline_path_segment_t* s) {
  (s->points)[1] = spline_path_end((*((spline_path_t*)(s->data))));
  return (0);
}
uint8_t spline_path_constant_prepare(spline_path_segment_t* s) {
  ((s->points)[1]).x = spline_path_size_max;
  ((s->points)[1]).y = ((s->points)[0]).y;
  return (0);
}

/** set _start and _points_count for segments */
uint8_t spline_path_prepare_segments(spline_path_segment_t* segments, spline_path_segment_count_t segments_count) {
  spline_path_segment_t* s;
  spline_path_point_t start;
  start.x = 0;
  start.y = 0;
  for (spline_path_segment_count_t i = 0; (i < segments_count); i += 1) {
    s = (segments + i);
    (s->points)[0] = start;
    if (s->prepare && (s->prepare)(s)) {
      return (1);
    };
    start = (s->points)[(s->points_count - 1)];
  };
  return (0);
}

/** set segments for a path and initialize it */
uint8_t spline_path_set(spline_path_t* path, spline_path_segment_t* segments, spline_path_segment_count_t segments_count) {
  if (spline_path_prepare_segments(segments, segments_count)) {
    return (1);
  };
  path->segments = segments;
  path->segments_count = segments_count;
  path->current_segment_index = 0;
  path->previous_start = 0;
  return (0);
}

/** like spline_path_set but copies segments to new memory which has to be freed after use */
uint8_t spline_path_set_copy(spline_path_t* path, spline_path_segment_t* segments, spline_path_segment_count_t segments_count) {
  spline_path_segment_t* s = malloc((segments_count * sizeof(spline_path_segment_t)));
  if (!s) {
    return (1);
  };
  memcpy(s, segments, (segments_count * sizeof(spline_path_segment_t)));
  return ((spline_path_set(path, s, segments_count)));
}

/** create a sampled array directly from segments */
uint8_t spline_path_segments_get(spline_path_segment_t* segments, spline_path_segment_count_t segments_count, size_t start, size_t end, spline_path_value_t* out) {
  spline_path_t path;
  if (spline_path_set((&path), segments, segments_count)) {
    return (1);
  };
  spline_path_get((&path), start, end, out);
  spline_path_free(path);
  return (0);
}

/** only needed if a segment with state has been used,
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

/** p_rest length 1 */
void spline_path_move_generate(size_t start, size_t end, spline_path_segment_t* s, spline_path_value_t* out) { memset(out, 0, (sizeof(spline_path_value_t) * (end - start))); }

/** p_rest length 0 */
void spline_path_constant_generate(size_t start, size_t end, spline_path_segment_t* s, spline_path_value_t* out) {
  spline_path_value_t y;
  y = ((s->points)[0]).y;
  for (size_t i = 0; (i < (end - start)); i += 1) {
    out[i] = y;
  };
}
void spline_path_line_generate(size_t start, size_t end, spline_path_segment_t* s, spline_path_value_t* out) {
  spline_path_point_t p_start;
  spline_path_point_t p_end;
  spline_path_value_t t;
  spline_path_value_t s_size;
  size_t s_offset;
  p_start = (s->points)[0];
  p_end = (s->points)[1];
  s_size = (p_end.x - p_start.x);
  s_offset = (start - p_start.x);
  for (size_t i = 0; (i < (end - start)); i += 1) {
    t = ((i + s_offset) / s_size);
    out[i] = linearly_interpolate((p_start.y), (p_end.y), t);
  };
}
void spline_path_path_generate(size_t start, size_t end, spline_path_segment_t* s, spline_path_value_t* out) { spline_path_get(((spline_path_t*)(s->data)), (start - s->points->x), (end - s->points->x), out); }

/** quadratic bezier curve with one control point.
   bezier interpolation also interpolates x. higher resolution sampling and linear interpolation are used to fill gaps */
void spline_path_bezier1_generate(size_t start, size_t end, spline_path_segment_t* s, spline_path_value_t* out) {
  spline_path_value_t mt;
  spline_path_point_t p_control;
  spline_path_point_t p_end;
  spline_path_point_t p_start;
  spline_path_value_t s_size;
  spline_path_value_t t;
  size_t b_size;
  size_t x;
  size_t x_previous;
  spline_path_value_t y_previous;
  size_t i;
  size_t j;
  spline_path_value_t y;
  p_start = (s->points)[0];
  p_control = (s->points)[1];
  p_end = (s->points)[2];
  b_size = (spline_path_bezier_resolution * (end - start));
  s_size = (spline_path_bezier_resolution * (p_end.x - p_start.x));
  printf("%f %f\n", (p_control.x), (p_control.y));
  for (i = (start - p_start.x), x_previous = start; ((i < b_size) || (x < end)); i += 1, x_previous = x) {
    t = (i / s_size);
    mt = (1.0 - t);
    x = round((spline_path_bezier1_interpolate(mt, t, (p_start.x), (p_control.x), (p_end.x))));
    y = spline_path_bezier1_interpolate(mt, t, (p_start.y), (p_control.y), (p_end.y));
    if (x < end) {
      out[(x - start)] = y;
    };
    if (2 > (x - x_previous)) {
      continue;
    };
    if (x_previous) {
      y_previous = out[(x_previous - start)];
    } else {
      /* gap at the beginning. find value for x before start */
      for (j = i; j; j -= 1) {
        t = (j / s_size);
        mt = (1.0 - t);
        x_previous = round((spline_path_bezier1_interpolate(mt, t, (p_start.x), (p_control.x), (p_end.x))));
        if (x_previous < x) {
          break;
        };
      };
      if (j) {
        y_previous = p_start.y;
      } else {
        y_previous = p_start.y;
        x_previous = p_start.x;
      };
    };
    for (j = 1; (j < (x - x_previous)); j += 1) {
      t = (j / ((spline_path_value_t)((x - x_previous))));
      out[((x_previous + j) - start)] = linearly_interpolate(y_previous, y, t);
    };
  };
}
void spline_path_bezier2_generate(size_t start, size_t end, spline_path_segment_t* s, spline_path_value_t* out) {
  spline_path_value_t mt;
  spline_path_point_t p_control1;
  spline_path_point_t p_control2;
  spline_path_point_t p_end;
  spline_path_point_t p_start;
  spline_path_value_t s_size;
  spline_path_value_t t;
  p_start = (s->points)[0];
  p_control1 = (s->points)[1];
  p_control2 = (s->points)[2];
  p_end = (s->points)[3];
  s_size = (p_end.x - p_start.x);
  for (size_t i = start; (i < end); i += 1) {
    t = ((i - start) / s_size);
    mt = (1.0 - t);
    out[i] = spline_path_bezier2_interpolate(mt, t, (p_start.y), (p_control1.y), (p_control2.y), (p_end.y));
  };
}

/** returns a move segment to move to the specified point */
spline_path_segment_t spline_path_move(spline_path_value_t x, spline_path_value_t y) {
  spline_path_declare_segment(s);
  s.generate = spline_path_move_generate;
  s.points_count = 2;
  ((s.points)[1]).x = x;
  ((s.points)[1]).y = y;
  return (s);
}
spline_path_segment_t spline_path_line(spline_path_value_t x, spline_path_value_t y) {
  spline_path_declare_segment(s);
  s.generate = spline_path_line_generate;
  s.points_count = 2;
  ((s.points)[1]).x = x;
  ((s.points)[1]).y = y;
  return (s);
}
spline_path_segment_t spline_path_constant() {
  spline_path_declare_segment(s);
  s.generate = spline_path_constant_generate;
  s.prepare = spline_path_constant_prepare;
  s.points_count = 1;
  return (s);
}

/** return a segment that is another spline-path. length is the full length of the path.
   the path does not necessarily connect and is drawn as it would be on its own starting from the preceding segment */
spline_path_segment_t spline_path_path(spline_path_t path) {
  spline_path_declare_segment(s);
  s.data = malloc((sizeof(spline_path_t)));
  if (s.data) {
    *((spline_path_t*)(s.data)) = path;
  } else {
    return ((spline_path_constant()));
  };
  s.free = free;
  s.generate = spline_path_path_generate;
  s.points_count = 1;
  s.prepare = spline_path_path_prepare;
  return (s);
}
spline_path_segment_t spline_path_bezier1(spline_path_value_t x1, spline_path_value_t y1, spline_path_value_t x2, spline_path_value_t y2) {
  spline_path_declare_segment(s);
  s.generate = spline_path_bezier1_generate;
  s.points_count = 3;
  ((s.points)[1]).x = x1;
  ((s.points)[1]).y = y1;
  ((s.points)[2]).x = x2;
  ((s.points)[2]).y = y2;
  return (s);
}
spline_path_segment_t spline_path_bezier2(spline_path_value_t x1, spline_path_value_t y1, spline_path_value_t x2, spline_path_value_t y2, spline_path_value_t x3, spline_path_value_t y3) {
  spline_path_declare_segment(s);
  s.generate = spline_path_bezier1_generate;
  s.points_count = 4;
  ((s.points)[1]).x = x1;
  ((s.points)[1]).y = y1;
  ((s.points)[2]).x = x2;
  ((s.points)[2]).y = y2;
  ((s.points)[3]).x = x3;
  ((s.points)[3]).y = y3;
  return (s);
}

/** return a point on a perpendicular line across the midpoint of a line between p1 and p2.
   distance -1 and 1 are the bounds of a rectangle where p1 and p2 are diagonally opposed edges */
spline_path_point_t spline_path_perpendicular_point(spline_path_point_t p1, spline_path_point_t p2, spline_path_value_t distance) {
  spline_path_point_t c;
  spline_path_point_t d;
  spline_path_point_t b1;
  spline_path_point_t b2;
  spline_path_point_t t1;
  spline_path_point_t t2;
  spline_path_point_t i1;
  spline_path_point_t i2;
  spline_path_value_t m;
  spline_path_point_t result;
  /* center point */
  c.x = ((p1.x + p2.x) / 2);
  c.y = ((p1.y + p2.y) / 2);
  if (0 == distance) {
    return (c);
  };
  printf("%f %f\n", (c.x), (c.y));
  /* perpendicular direction vector */
  d.x = (-1 * (p2.y - p1.y));
  d.y = (p2.x - p1.x);
  /* vertical, horizontal or else */
  if (0 == d.x) {
    i1.x = c.x;
    i1.y = 0;
    i2.x = c.x;
    i2.y = p2.y;
  } else if (0 == d.y) {
    i1.x = 0;
    i1.y = c.y;
    i2.x = p2.x;
    i2.y = c.y;
  } else {
    /* border points */
    if (d.x > 0) {
      b1.x = p2.x;
      b2.x = p1.x;
    } else {
      b1.x = p1.x;
      b2.x = p2.x;
    };
    if (d.y > 0) {
      b1.y = p2.y;
      b2.y = p1.y;
    } else {
      b1.y = p1.y;
      b2.y = p2.y;
    };
    t1.x = ((b1.x - c.x) / d.x);
    t1.y = ((b1.y - c.y) / d.y);
    t2.x = ((b2.x - c.x) / d.x);
    t2.y = ((b2.y - c.y) / d.y);
    if (t1.x <= t1.y) {
      i1.x = b1.x;
      i1.y = (c.y + (t1.x * d.y));
    } else {
      i1.x = (c.x + (t1.y * d.x));
      i1.y = b1.y;
    };
    if (t2.x >= t2.y) {
      i2.x = b2.x;
      i2.y = (c.y + (t2.x * d.y));
    } else {
      i2.x = (c.y + (t2.x * d.y));
      i2.y = b2.y;
    };
  };
  /* normalized direction vector */
  m = sqrt(((d.x * d.x) + (d.y * d.y)));
  d.x = ((i2.x - i1.x) / m);
  d.y = ((i2.y - i1.y) / m);
  result.x = (c.x + (0.5 * distance * m * d.x));
  result.y = (c.y + (0.5 * distance * m * d.y));
  return (result);
}
uint8_t spline_path_bezier_arc_prepare(spline_path_segment_t* s) {
  (s->points)[1] = spline_path_perpendicular_point(((s->points)[0]), ((s->points)[2]), (((s->points)[1]).y));
  return (0);
}

/** curvature is a real between -1..1, with the maximum at the sides of
   a rectangle with the points as diagonally opposing edges */
spline_path_segment_t spline_path_bezier_arc(spline_path_value_t x, spline_path_value_t y, spline_path_value_t curvature) {
  spline_path_segment_t s;
  if (0.0 == curvature) {
    s = spline_path_line(x, y);
  } else {
    s = spline_path_bezier1(0, curvature, x, y);
    s.prepare = spline_path_bezier_arc_prepare;
  };
  return (s);
}
