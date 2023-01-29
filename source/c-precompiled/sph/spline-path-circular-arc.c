
/* (pre-define (spline-path-segment-points-count s) (case* = s.interpolator ((spline-path-i-bezier spline-path-i-bezier-arc) 3) (spline-path-i-circular-arc 2) (else 1)))
(spline-path-i-circular-arc start end p-start p-rest data out)
(void size-t size-t spline-path-point-t spline-path-point-t* void** spline-path-value-t*)
(spline-path-circular-arc curvature x y)
(spline-path-segment-t spline-path-value-t spline-path-value-t spline-path-value-t) */
typedef struct {
  spline_path_point_t m_a;
  spline_path_point_t b_m;
  spline_path_point_t ab_m;
  spline_path_point_t bm_a;
  spline_path_value_t s_size;
} spline_path_i_circular_arc_data_t;
spline_path_point_t complex_difference(spline_path_point_t p1, spline_path_point_t p2) {
  spline_path_point_t result;
  result.x = (p1.x - p2.x);
  result.y = (p1.y - p2.y);
  return (result);
}
spline_path_point_t complex_multiplication(spline_path_point_t p1, spline_path_point_t p2) {
  spline_path_point_t result;
  result.x = ((p1.x * p2.x) - (p1.y * p2.y));
  result.y = ((p1.x * p2.y) + (p1.y * p2.x));
  return (result);
}
spline_path_point_t complex_inversion(spline_path_point_t p) {
  spline_path_value_t scale;
  spline_path_point_t result;
  scale = (1 / ((p.x * p.x) + (p.y * p.y)));
  result.x = (scale * p.x);
  result.y = ((-scale) * p.y);
  return (result);
}
spline_path_point_t complex_division(spline_path_point_t p1, spline_path_point_t p2) { return ((complex_multiplication(p1, (complex_inversion(p2))))); }
spline_path_point_t complex_linear_interpolation(spline_path_point_t p1, spline_path_point_t p2, spline_path_value_t t) {
  spline_path_point_t result;
  result.x = ((p1.x * (1 - t)) + (p2.x * t));
  result.y = ((p1.y * (1 - t)) + (p2.y * t));
  return (result);
}

/** p-rest length 2. circular arc interpolation formula from jacob rus,
   https://observablehq.com/@jrus/circle-arc-interpolation */
void spline_path_i_circular_arc(size_t start, size_t end, spline_path_point_t p_start, spline_path_point_t* p_rest, void** data, spline_path_value_t* out) {
  size_t b_size;
  spline_path_i_circular_arc_data_t d;
  size_t i;
  size_t ix;
  spline_path_point_t p_end;
  spline_path_point_t p;
  size_t s_offset;
  spline_path_value_t s_size;
  spline_path_value_t t;
  d = *((spline_path_i_circular_arc_data_t*)(*data));
  p_end = p_rest[1];
  b_size = (end - start);
  s_size = (p_end.x - p_start.x);
  s_offset = (start - p_start.x);
  if (!d.s_size) {
    spline_path_i_circular_arc_data_t* dp;
    spline_path_point_t m;
    dp = *data;
    m = spline_path_perpendicular_point(p_start, p_end, (p_rest->y));
    dp->b_m = complex_difference(p_end, m);
    dp->m_a = complex_difference(m, p_start);
    dp->ab_m = complex_multiplication(p_start, (dp->b_m));
    dp->bm_a = complex_multiplication(p_end, (dp->m_a));
    dp->s_size = (p_end.x - p_start.x);
    d = *dp;
  };
  i = 0;
  ix = i;
  while ((ix < b_size)) {
    t = ((i + s_offset) / s_size);
    if (t >= 1.0) {
      break;
    };
    p = complex_division((complex_linear_interpolation((d.ab_m), (d.bm_a), t)), (complex_linear_interpolation((d.b_m), (d.m_a), t)));
    ix = (convert_point_x((p.x), start, end) - start);
    out[ix] = p.y;
    i += 1;
  };
  spline_path_set_missing_points(out, start, end);
}

/** curvature is a real between -1..1, with the maximum being the edge of the segment.
   the current implementation turned out to be extremely slow */
spline_path_segment_t spline_path_circular_arc(spline_path_value_t curvature, spline_path_value_t x, spline_path_value_t y) {
  spline_path_segment_t s;
  spline_path_i_circular_arc_data_t* d;
  d = malloc((sizeof(spline_path_i_circular_arc_data_t)));
  if (d) {
    s.free = free;
    d->s_size = 0;
    s.data = d;
    s.interpolator = spline_path_i_circular_arc;
    (s.points)->y = curvature;
    (1 + s.points)->x = x;
    (1 + s.points)->y = y;
  } else {
    s.free = 0;
    s.interpolator = spline_path_i_constant;
  };
  return (s);
}
