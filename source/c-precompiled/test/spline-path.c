
#include <inttypes.h>
#include <string.h>
#include "./test.c"
#include <sph/spline-path.h>
#include <sph/spline-path.c>
#include <sph/float.h>
#include <sph/float.c>

#define error_margin 0.1
#define test_spline_path_length 50
#define test_spline_path_bezier_length 1000
#define feq(a, b) sph_f64_nearly_equal(a, b, (1.0e-7))
void reset_output(spline_path_value_t* out, size_t length) {
  for (size_t i = 0; (i < length); i += 1) {
    out[i] = 0;
  };
}
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
  segments[2] = spline_path_bezier2(25, 15, 30, 20, 40, 25);
  segments[3] = spline_path_constant();
  segments_count = 4;
  status_i_require((spline_path_set_copy((&path), segments, segments_count)));
  /* starting output shifted by offset 5 */
  spline_path_get((&path), 5, 25, out);
  spline_path_get((&path), 25, (5 + length), (20 + out));
  spline_path_free(path);
  free((path.segments));
  if (log_path_new_0) {
    for (i = 0; (i < length); i += 1) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  test_helper_assert("before move 1", (feq(0, (out[0]))));
  test_helper_assert("before move 2", (feq(0, (out[4]))));
  test_helper_assert("move end", (feq(5, (out[5]))));
  test_helper_assert("line end", (feq((9.5), (out[14]))));
  test_helper_assert("bezier2 start", (feq((10.375), (out[15]))));
  test_helper_assert("bezier2 end", (feq((24.625), (out[34]))));
  test_helper_assert("constant", (feq(25, (out[(length - 1)]))));
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
  test_helper_assert(("path 1.9 - should reach maximum at 9"), (feq((4.5), (out[9]))));
  test_helper_assert(("path 1.10 - should not set the next start point"), (feq(0, (out[10]))));
  test_helper_assert(("path 1.11 - should be zero after segments"), (feq(0, (out[11]))));
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
status_t test_spline_path_perpendicular_point() {
  status_declare;
  uint8_t log_line;
  spline_path_point_t p1;
  spline_path_point_t p2;
  spline_path_point_t p3;
  log_line = 0;
  p1.x = 0;
  p1.y = 0;
  p2.x = 10;
  p2.y = 10;
  p3 = spline_path_perpendicular_point(p1, p2, 0);
  if (log_line) {
    for (size_t i = 0; (i < p2.x); i += 1) {
      printf("%lu %lu\n", i, i);
    };
  };
  test_helper_assert("d 0", (feq((p3.x), (p3.y)) && feq((p3.x), (5.0))));
  p3 = spline_path_perpendicular_point(p1, p2, 1);
  test_helper_assert("d 1", (feq((p3.x), 10) && feq((p3.y), 0)));
  p3 = spline_path_perpendicular_point(p1, p2, -1);
  test_helper_assert("d -1", (feq((p3.x), 0) && feq((p3.y), 10)));
  p3 = spline_path_perpendicular_point(p1, p2, (0.5));
  test_helper_assert(("d 0.5"), (feq((p3.x), (7.5)) && feq((p3.y), (2.5))));
  p3 = spline_path_perpendicular_point(p1, p2, (-0.5));
  test_helper_assert(("d -0.5"), (feq((p3.x), (2.5)) && feq((p3.y), (7.5))));
exit:
  status_return;
}
status_t test_spline_path_bezier_arc() {
  status_declare;
  spline_path_value_t end_x;
  spline_path_value_t end_y;
  size_t i;
  uint8_t log_path_0;
  spline_path_value_t out[test_spline_path_bezier_length];
  spline_path_t path;
  spline_path_segment_t segments[2];
  log_path_0 = 0;
  end_x = test_spline_path_bezier_length;
  end_y = test_spline_path_bezier_length;
  memset(out, 0, test_spline_path_bezier_length);
  segments[0] = spline_path_bezier_arc((end_x / 3), (end_y / 3), 1);
  segments[1] = spline_path_bezier_arc(end_x, end_y, -1);
  spline_path_set((&path), segments, 2);
  spline_path_get((&path), 0, end_x, out);
  spline_path_free(path);
  if (log_path_0) {
    for (i = 0; (i < end_x); i += 1) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_spline_path);
  test_helper_test_one(test_spline_path_bezier_arc);
  test_helper_test_one(test_spline_path_perpendicular_point);
exit:
  test_helper_display_summary();
  return ((status.id));
}
