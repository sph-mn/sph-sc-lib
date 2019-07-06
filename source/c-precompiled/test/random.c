#include <stdio.h>
#include "../main/types.c"
#include "./test.c"
#include "../main/float.c"
#include "../main/random-h.c"
#include "../main/random.c"
s_t test_random() {
  s_declare;
  sph_random_state_t s;
  f64 out[200];
  s = sph_random_state_new(80);
  sph_random((&s), 100, out);
  sph_random((&s), 100, (100 + out));
  test_helper_assert("value", (f64_nearly_equal((0.945766), (out[199]), (1.0e-4))));
exit:
  s_return;
}
int main() {
  s_declare;
  test_helper_test_one(test_random);
exit:
  test_helper_display_summary();
  return ((s_current.id));
}
