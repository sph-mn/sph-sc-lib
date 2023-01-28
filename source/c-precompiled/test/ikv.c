
#define _GNU_SOURCE
#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../foreign/murmur3.c"
#include <sph/ikv.c>
status_t test_ikv() {
  ikv_t a;
  ikv_t b;
  ikv_value_t* value;
  status_declare;
  status_i_require((ikv_new(100, (&a))));
  /* read/write */
  status_require((ikv_read_file("other/ikv-test-data", a)));
  ikv_write_file(a, "temp/ikv-test");
  ikv_free_all(a);
  status_i_require((ikv_new(100, (&a))));
  status_require((ikv_read_file("temp/ikv-test", a)));
  /* top level */
  value = ikv_get(a, "key4");
  test_helper_assert("key4 string", (0 == strcmp("string7", (ikv_value_get_string(value, 0)))));
  value = ikv_get(a, "key3");
  test_helper_assert("key3 string array", (0 == strcmp("string3", (ikv_value_get_string(value, 2)))));
  /* nested */
  value = ikv_get(a, "nest1");
  b = ikv_value_get_ikv(value);
  value = ikv_get(b, "nest11");
  value = ikv_get((ikv_value_get_ikv(value)), "nest111");
  test_helper_assert("nest111 string", (0 == strcmp("string4", (ikv_value_get_string(value, 0)))));
  value = ikv_get(b, "nest12");
  test_helper_assert("nest12 integer", (9 == ikv_value_get_integer(value, 0)));
  ikv_free_all(a);
exit:
  return (status);
}
int main() {
  status_declare;
  test_helper_test_one(test_ikv);
exit:
  test_helper_display_summary();
  return ((status.id));
}
