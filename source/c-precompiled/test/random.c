#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../main/float.c"
#include "../main/types.c"
#include "../main/random.c"
status_t test_random() {
  status_declare;
  random_state_t s;
  f64 out[200];
  s = random_state_new(80);
  random((&s), 100, out);
  random((&s), 100, (100 + out));
  test_helper_assert("value", (f64_nearly_equal((0.945766), (out[199]), (1.0e-4))));
exit:
  return (status);
};
int main() {
  status_declare;
  test_helper_test_one(test_random);
exit:
  test_helper_display_summary();
  return ((status.id));
};