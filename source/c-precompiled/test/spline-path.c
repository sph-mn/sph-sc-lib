
#include <inttypes.h>
#include <string.h>
#include "../main/spline-path.h"
#include "../main/spline-path.c"
#include "../main/float.c"
#include "./test.c"
double error_margin = 0.1;
status_t test_spline_path() {
  status_declare;
  spline_path_value_t out[100];
  spline_path_value_t out_new_get[100];
  spline_path_time_t i;
  spline_path_time_t length;
  spline_path_t path;
  spline_path_segment_t segments[4];
  spline_path_segment_count_t segments_count;
  uint8_t log_path_new_0;
  uint8_t log_path_new_1;
  uint8_t log_path_new_get_0;
  uint8_t log_path_new_get_1;
  log_path_new_0 = 0;
  log_path_new_1 = 0;
  log_path_new_get_0 = 0;
  log_path_new_get_1 = 0;
  length = 50;
  for (i = 0; (i < length); i += 1) {
    out[i] = 999;
    out_new_get[i] = 999;
  };
  /* path 2 - a special case that lead to errors before */
  segments[0] = spline_path_move(0, 6);
  segments[1] = spline_path_line(24, 18);
  segments[2] = spline_path_line(96, 24);
  segments[3] = spline_path_constant();
  segments_count = 4;
  status_i_require((spline_path_segments_get(segments, segments_count, 0, 100, out_new_get)));
  /* path 0 - will be written to output starting at offset 5 */
  segments[0] = spline_path_move(10, 5);
  segments[1] = spline_path_line(20, 10);
  segments[2] = spline_path_bezier(25, 15, 30, 20, 40, 25);
  segments[4] = spline_path_constant();
  segments_count = 4;
  status_i_require((spline_path_set_copy((&path), segments, segments_count)));
  spline_path_get((&path), 5, 25, out);
  spline_path_get((&path), 25, (5 + length), (20 + out));
  if (log_path_new_0) {
    for (i = 0; (i < length); i += 1) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  test_helper_assert(("path 0.0"), (f64_nearly_equal(0, (out[0]), error_margin)));
  test_helper_assert(("path 0.4"), (f64_nearly_equal(0, (out[4]), error_margin)));
  test_helper_assert(("path 0.5"), (f64_nearly_equal(5, (out[5]), error_margin)));
  test_helper_assert(("path 0.15"), (f64_nearly_equal(10, (out[15]), error_margin)));
  test_helper_assert(("path 0.16"), (f64_nearly_equal((10.75), (out[16]), error_margin)));
  test_helper_assert(("path 0.34"), (f64_nearly_equal((24.25), (out[34]), error_margin)));
  test_helper_assert(("path 0.35"), (f64_nearly_equal(25, (out[35]), error_margin)));
  test_helper_assert(("path 0.49"), (f64_nearly_equal(25, (out[49]), error_margin)));
  free((path.segments));
  /* path 0 new-get */
  status_i_require((spline_path_segments_get(segments, segments_count, 5, 55, out_new_get)));
  if (log_path_new_get_0) {
    for (i = 0; (i < length); i += 1) {
      printf("%lu %f\n", i, (out_new_get[i]));
    };
  };
  test_helper_assert("path 0 new-get equal", (!memcmp(out, out_new_get, (sizeof(spline_path_value_t) * length))));
  /* path 1 - path that ends at 10 */
  for (i = 0; (i < length); i += 1) {
    /* reset output arrays */
    out[i] = 999;
    out_new_get[i] = 999;
  };
  segments[0] = spline_path_line(10, 5);
  segments_count = 1;
  status_i_require((spline_path_set_copy((&path), segments, segments_count)));
  spline_path_get((&path), 0, 12, out);
  if (log_path_new_1) {
    for (i = 0; (i < 12); i = (1 + i)) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  test_helper_assert(("path 1.10 - should reach maximum at 10"), (f64_nearly_equal(5, (out[10]), error_margin)));
  test_helper_assert(("path 1.11 - should be zero after segments"), (f64_nearly_equal(0, (out[11]), error_margin)));
  free((path.segments));
  /* path 1 new-get */
  status_i_require((spline_path_segments_get(segments, segments_count, 0, 12, out_new_get)));
  if (log_path_new_get_1) {
    for (i = 0; (i < 12); i += 1) {
      printf("%lu %f\n", i, (out_new_get[i]));
    };
  };
  test_helper_assert("path 1 new-get equal", (!memcmp(out, out_new_get, (sizeof(spline_path_value_t) * 12))));
  spline_path_free(path);
exit:
  status_return;
}
status_t test_spline_path_helpers() {
  status_declare;
  spline_path_value_t out[50];
  spline_path_t path;
  spline_path_time_t i;
  spline_path_segment_t segments[4];
  spline_path_segment_t segments2[2];
  for (i = 0; (i < 50); i += 1) {
    out[i] = 999;
  };
  segments[0] = spline_path_move(1, 5);
  segments[1] = spline_path_line(10, 10);
  segments[2] = spline_path_bezier(20, 15, 30, 5, 40, 15);
  segments[3] = spline_path_constant();
  spline_path_set_copy((&path), segments, 4);
  spline_path_get((&path), 0, 50, out);
  test_helper_assert("helper path 0", (f64_nearly_equal(0, (out[0]), error_margin)));
  test_helper_assert("helper path 49", (f64_nearly_equal(15, (out[49]), error_margin)));
  segments2[0] = spline_path_line(5, 10);
  segments2[1] = spline_path_path(path);
  /* note that the first point leaves a gap */
  spline_path_segments_get(segments2, 2, 0, 50, out);
  free((path.segments));
exit:
  status_return;
}
status_t test_spline_path_circular_arc() {
  status_declare;
  spline_path_point_t p1;
  spline_path_point_t p2;
  spline_path_point_t pc;
  spline_path_value_t out[50];
  uint8_t log_path_0;
  spline_path_t path;
  spline_path_time_t i;
  spline_path_time_t length;
  spline_path_segment_t segments;
  log_path_0 = 0;
  /* control-point */
  p1.x = 0;
  p1.y = 0;
  p2.x = 10;
  p2.y = 10;
  pc = spline_path_i_circular_arc_control_point(p1, p2, (1.0));
  test_helper_assert("control point", ((pc.x == 2) && f64_nearly_equal((8.535534), (pc.y), error_margin)));
  /* interpolation */
  length = 50;
  for (i = 0; (i < length); i += 1) {
    out[i] = 999;
  };
  segments = spline_path_circular_arc(1, 10, 10);
  spline_path_set((&path), (&segments), 1);
  spline_path_get((&path), 0, length, out);
  spline_path_free(path);
  if (log_path_0) {
    for (i = 0; (i < length); i += 1) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_spline_path_circular_arc);
  goto exit;
  test_helper_test_one(test_spline_path_helpers);
  test_helper_test_one(test_spline_path);
exit:
  test_helper_display_summary();
  return ((status.id));
}
