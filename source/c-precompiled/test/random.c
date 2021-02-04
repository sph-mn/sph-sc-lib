
#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../main/float.c"
#include "../main/random.c"
status_t test_random() {
  status_declare;
  sph_random_state_t s;
  double out[200];
  uint64_t out_u64[1000];
  /* f64 */
  s = sph_random_state_new(80);
  sph_random_f64_array((&s), 100, out);
  sph_random_f64_array((&s), 100, (100 + out));
  test_helper_assert("f64", (f64_nearly_equal((0.945766), (out[199]), (1.0e-4))));
  /* u64 */
  s = sph_random_state_new(80);
  sph_random_u64_array((&s), 100, out_u64);
  test_helper_assert("u64", (16312392477912876050u == out_u64[99]));
  /* u64 bounded */
  s = sph_random_state_new(80);
  sph_random_u64_bounded_array((&s), 10, 100, out_u64);
  test_helper_assert("u64 bounded 1", (0 == out_u64[97]));
  test_helper_assert("u64 bounded 2", (9 == out_u64[61]));
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_random);
exit:
  test_helper_display_summary();
  return ((status.id));
}
