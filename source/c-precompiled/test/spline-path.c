#include <inttypes.h>
#include <string.h>
#include "../main/spline-path.c"
#include "../main/float.c"
#include "./test.c"
double error_margin = 0.1;
status_t test_spline_path() {
  status_declare;
  spline_path_value_t out[100];
  spline_path_value_t out_new_get[100];
  spline_path_time_t i;
  spline_path_t path;
  spline_path_point_t p;
  spline_path_segment_t s;
  spline_path_segment_t segments[4];
  spline_path_segment_count_t segments_len;
  uint8_t log_path_new_0;
  uint8_t log_path_new_1;
  uint8_t log_path_new_get_0;
  uint8_t log_path_new_get_1;
  log_path_new_0 = 0;
  log_path_new_1 = 0;
  log_path_new_get_0 = 0;
  log_path_new_get_1 = 0;
  for (i = 0; (i < 50); i = (1 + i)) {
    out[i] = 999;
    out_new_get[i] = 999;
  };
  /* path 2 - a special case that lead to errors */
  s.interpolator = spline_path_i_move;
  p.x = 0;
  p.y = 6;
  (s.points)[0] = p;
  segments[0] = s;
  s.interpolator = spline_path_i_line;
  p.x = 24;
  p.y = 18;
  (s.points)[0] = p;
  segments[1] = s;
  s.interpolator = spline_path_i_line;
  p.x = 96;
  p.y = 24;
  (s.points)[0] = p;
  segments[2] = s;
  s.interpolator = spline_path_i_constant;
  segments[3] = s;
  segments_len = 4;
  status_id_require((spline_path_new_get(segments_len, segments, 0, 100, out_new_get)));
  /* path 0 - will be written to output starting at offset 5 */
  s.interpolator = spline_path_i_move;
  p.x = 10;
  p.y = 5;
  (s.points)[0] = p;
  segments[0] = s;
  s.interpolator = spline_path_i_line;
  p.x = 20;
  p.y = 10;
  (s.points)[0] = p;
  segments[1] = s;
  s.interpolator = spline_path_i_bezier;
  p.x = 25;
  p.y = 15;
  (s.points)[0] = p;
  p.x = 30;
  p.y = 20;
  (s.points)[1] = p;
  p.x = 40;
  p.y = 25;
  (s.points)[2] = p;
  segments[2] = s;
  s.interpolator = spline_path_i_constant;
  segments[3] = s;
  segments_len = 4;
  status_id_require((spline_path_new(segments_len, segments, (&path))));
  spline_path_get(path, 5, 25, out);
  spline_path_get(path, 25, 55, (20 + out));
  if (log_path_new_0) {
    for (i = 0; (i < 50); i = (1 + i)) {
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
  spline_path_free(path);
  /* path 0 new-get */
  status_id_require((spline_path_new_get(segments_len, segments, 5, 55, out_new_get)));
  if (log_path_new_get_0) {
    for (i = 0; (i < 50); i = (1 + i)) {
      printf("%lu %f\n", i, (out_new_get[i]));
    };
  };
  test_helper_assert("path 0 new-get equal", (!memcmp(out, out_new_get, (sizeof(spline_path_value_t) * 50))));
  /* path 1 - path that ends at 10 */
  for (i = 0; (i < 50); i = (1 + i)) {
    /* reset output arrays */
    out[i] = 999;
    out_new_get[i] = 999;
  };
  s.interpolator = spline_path_i_line;
  p.x = 10;
  p.y = 5;
  (s.points)[0] = p;
  segments[0] = s;
  segments_len = 1;
  status_id_require((spline_path_new(segments_len, segments, (&path))));
  spline_path_get(path, 0, 12, out);
  if (log_path_new_1) {
    for (i = 0; (i < 12); i = (1 + i)) {
      printf("%lu %f\n", i, (out[i]));
    };
  };
  test_helper_assert(("path 1.10 - should reach maximum at 10"), (f64_nearly_equal(5, (out[10]), error_margin)));
  test_helper_assert(("path 1.11 - should be zero after segments"), (f64_nearly_equal(0, (out[11]), error_margin)));
  spline_path_free(path);
  /* path 1 new-get */
  status_id_require((spline_path_new_get(segments_len, segments, 0, 12, out_new_get)));
  if (log_path_new_get_1) {
    for (i = 0; (i < 12); i = (1 + i)) {
      printf("%lu %f\n", i, (out_new_get[i]));
    };
  };
  test_helper_assert("path 1 new-get equal", (!memcmp(out, out_new_get, (sizeof(spline_path_value_t) * 12))));
exit:
  return (status);
};
status_t test_spline_path_helpers() {
  status_declare;
  spline_path_value_t out[50];
  spline_path_t path;
  spline_path_time_t i;
  spline_path_segment_t segments[4];
  spline_path_segment_t segments2[2];
  for (i = 0; (i < 50); i = (1 + i)) {
    out[i] = 999;
  };
  segments[0] = spline_path_move(1, 5);
  segments[1] = spline_path_line(10, 10);
  segments[2] = spline_path_bezier(20, 15, 30, 5, 40, 15);
  segments[3] = spline_path_constant();
  spline_path_new(4, segments, (&path));
  spline_path_get(path, 0, 50, out);
  test_helper_assert("helper path 0", (f64_nearly_equal(0, (out[0]), error_margin)));
  test_helper_assert("helper path 49", (f64_nearly_equal(15, (out[49]), error_margin)));
  segments2[0] = spline_path_line(5, 10);
  segments2[1] = spline_path_path((&path));
  /* note that the first point leaves a gap */
  spline_path_new_get(2, segments2, 0, 50, out);
  spline_path_free(path);
exit:
  return (status);
};
int main() {
  status_declare;
  test_helper_test_one(test_spline_path_helpers);
exit:
  test_helper_display_summary();
  return ((status.id));
};