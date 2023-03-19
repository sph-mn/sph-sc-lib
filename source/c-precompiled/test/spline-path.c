
#include <inttypes.h>
#include <string.h>
#include "./test.c"
#include <sph/spline-path.h>
#include <sph/spline-path.c>
#include <sph/float.h>
#include <sph/float.c>
#include <sph/spline-path-circular-arc.c>
#define error_margin 0.1
void reset_output(spline_path_value_t* out, size_t length) {
  for (size_t i = 0; (i < length); i += 1) {
    out[i] = 0;
  };
}
#define test_spline_path_length 50
status_t test_spline_path() {
  status_declare;
  spline_path_value_t out[test_spline_path_length];
  spline_path_value_t out_new_get[test_spline_path_length];
  size_t i;
  size_t length;
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
  length = test_spline_path_length;
  /* path 0 - will be written to output starting at offset 5 */
  reset_output(out, length);
  reset_output(out_new_get, length);
  segments[0] = spline_path_move(10, 5);
  segments[1] = spline_path_line(20, 10);
  segments[2] = spline_path_bezier(25, 15, 30, 20, 40, 25);
  segments[3] = spline_path_constant();
  segments_count = 4;
  status_i_require((spline_path_set_copy((&path), segments, segments_count)));
  /* get value starting from x 5 */
  spline_path_get((&path), 5, 25, out);
  spline_path_get((&path), 25, (5 + length), (20 + out));
  spline_path_free(path);
  free((path.segments));
  if (log_path_new_0) {
    for (i = 0; (i < length); i += 1) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  test_helper_assert(("path 0.0"), (sph_f64_nearly_equal(0, (out[0]), error_margin)));
  test_helper_assert(("path 0.4"), (sph_f64_nearly_equal(0, (out[4]), error_margin)));
  test_helper_assert(("path 0.5"), (sph_f64_nearly_equal(5, (out[5]), error_margin)));
  test_helper_assert(("path 0.15"), (sph_f64_nearly_equal(10, (out[15]), error_margin)));
  test_helper_assert(("path 0.16"), (sph_f64_nearly_equal((10.75), (out[16]), error_margin)));
  test_helper_assert(("path 0.34"), (sph_f64_nearly_equal((24.25), (out[34]), error_margin)));
  test_helper_assert(("path 0.35"), (sph_f64_nearly_equal(25, (out[35]), error_margin)));
  test_helper_assert(("path 0.49"), (sph_f64_nearly_equal(25, (out[49]), error_margin)));
  /* path 0 new-get */
  status_i_require((spline_path_segments_get(segments, segments_count, 5, 55, out_new_get)));
  if (log_path_new_get_0) {
    for (i = 0; (i < length); i += 1) {
      printf("%lu %f\n", i, (out_new_get[i]));
    };
  };
  test_helper_assert("path 0 new-get equal", (!memcmp(out, out_new_get, (sizeof(spline_path_value_t) * length))));
  /* path 1 - path that ends at 10 */
  reset_output(out, length);
  reset_output(out_new_get, length);
  segments[0] = spline_path_line(10, 5);
  segments_count = 1;
  status_i_require((spline_path_set_copy((&path), segments, segments_count)));
  spline_path_get((&path), 0, 12, out);
  free((path.segments));
  if (log_path_new_1) {
    for (i = 0; (i < 12); i = (1 + i)) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  test_helper_assert(("path 1.9 - should reach maximum at 9"), (sph_f64_nearly_equal((4.5), (out[9]), error_margin)));
  test_helper_assert(("path 1.10 - should not set the next start point"), (sph_f64_nearly_equal(0, (out[10]), error_margin)));
  test_helper_assert(("path 1.11 - should be zero after segments"), (sph_f64_nearly_equal(0, (out[11]), error_margin)));
  status_i_require((spline_path_segments_get(segments, segments_count, 0, 12, out_new_get)));
  if (log_path_new_get_1) {
    for (i = 0; (i < 12); i += 1) {
      printf("%lu %f\n", i, (out_new_get[i]));
    };
  };
  test_helper_assert("path 1 new-get equal", (!memcmp(out, out_new_get, (sizeof(spline_path_value_t) * 12))));
exit:
  status_return;
}
status_t test_spline_path_helpers() {
  status_declare;
  spline_path_value_t out[test_spline_path_length];
  spline_path_t path;
  size_t end_x;
  size_t i;
  spline_path_segment_t segments[4];
  spline_path_segment_t segments2[2];
  uint8_t log_path_0;
  end_x = test_spline_path_length;
  log_path_0 = 0;
  reset_output(out, end_x);
  segments[0] = spline_path_move(1, 5);
  segments[1] = spline_path_line(10, 10);
  segments[2] = spline_path_bezier(20, 15, 30, 5, 40, 15);
  segments[3] = spline_path_constant();
  spline_path_set_copy((&path), segments, 4);
  spline_path_get((&path), 0, end_x, out);
  if (log_path_0) {
    for (i = 0; (i < end_x); i += 1) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  test_helper_assert("helper path 0", (sph_f64_nearly_equal(0, (out[0]), error_margin)));
  test_helper_assert("helper path 49", (sph_f64_nearly_equal(15, (out[49]), error_margin)));
  segments2[0] = spline_path_line(5, 10);
  segments2[1] = spline_path_path(path);
  /* note that the first point leaves a gap */
  spline_path_segments_get(segments2, 2, 0, end_x, out);
exit:
  free((path.segments));
  status_return;
}
status_t test_spline_path_bezier_arc() {
  status_declare;
  spline_path_point_t pc;
  spline_path_value_t out[100];
  uint8_t log_path_0;
  spline_path_t path;
  size_t i;
  size_t end_x;
  spline_path_value_t end_y;
  spline_path_segment_t segments[2];
  log_path_0 = 0;
  end_x = 100;
  end_y = 100;
  segments[0] = spline_path_bezier_arc((end_x / 2), (end_y / 3), (0.25));
  segments[1] = spline_path_bezier_arc(end_x, end_y, 0);
  spline_path_set((&path), segments, 2);
  spline_path_get((&path), 0, end_x, out);
  spline_path_free(path);
  if (log_path_0) {
    printf("%f %f\n", (pc.x), (pc.y));
    for (i = 0; (i < end_x); i += 1) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  status_return;
}
status_t test_spline_path_circular_arc() {
  status_declare;
  spline_path_point_t p1;
  spline_path_point_t p2;
  spline_path_point_t pc;
  spline_path_value_t out[test_spline_path_length];
  uint8_t log_path_0;
  spline_path_t path;
  size_t i;
  size_t end_x;
  spline_path_value_t end_y;
  spline_path_segment_t segments;
  log_path_0 = 0;
  end_x = test_spline_path_length;
  end_y = 10;
  p1.x = 0;
  p1.y = 0;
  p2.x = end_x;
  p2.y = end_y;
  pc = spline_path_perpendicular_point(p1, p2, (1.0));
  /* (test-helper-assert perpendicular point (and (sph-f64-nearly-equal 31.73 pc.x error-margin) (sph-f64-nearly-equal 9.94 pc.y error-margin))) */
  reset_output(out, end_x);
  segments = spline_path_circular_arc(1, end_x, end_y);
  spline_path_set((&path), (&segments), 1);
  spline_path_get((&path), 0, end_x, out);
  spline_path_free(path);
  if (log_path_0) {
    printf("%f %f\n", (pc.x), (pc.y));
    for (i = 0; (i < end_x); i += 1) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_spline_path_bezier_arc);
  test_helper_test_one(test_spline_path);
  test_helper_test_one(test_spline_path_helpers);
  test_helper_test_one(test_spline_path_circular_arc);
exit:
  test_helper_display_summary();
  return ((status.id));
}
