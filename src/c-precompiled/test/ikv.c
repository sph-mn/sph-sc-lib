
#define _GNU_SOURCE
#include <stdio.h>
#include <inttypes.h>
#include <sph/test.h>
#include "../foreign/murmur3.c"
#include <sph/hashtable.h>
#include <sph/ikv.h>
#include <sph/ikv.c>
status_t test_ikv(void) {
  ikv_t a;
  ikv_t b;
  ikv_value_t* value;
  status_declare;
  status_i_require((ikv_new(100, (&a))));
  /* read/write */
  status_require((ikv_read_file(((ikv_string_t*)("other/ikv-test-data")), a)));
  ikv_write_file(a, ((ikv_string_t*)("tmp/ikv-test")));
  ikv_free_all(a);
  status_i_require((ikv_new(100, (&a))));
  status_require((ikv_read_file(((ikv_string_t*)("tmp/ikv-test")), a)));
  /* top level */
  value = ikv_get(a, ((ikv_string_t*)("key4")));
  test_helper_assert("key4 string", (0 == strcmp("string7", ((char*)(ikv_value_get_string(value, 0))))));
  value = ikv_get(a, ((ikv_string_t*)("key3")));
  test_helper_assert("key3 string array", (0 == strcmp("string3", ((char*)(ikv_value_get_string(value, 2))))));
  /* nested */
  value = ikv_get(a, ((ikv_string_t*)("nest1")));
  b = ikv_value_get_ikv(value);
  value = ikv_get(b, ((ikv_string_t*)("nest11")));
  value = ikv_get((ikv_value_get_ikv(value)), ((ikv_string_t*)("nest111")));
  test_helper_assert("nest111 string", (0 == strcmp("string4", ((char*)(ikv_value_get_string(value, 0))))));
  value = ikv_get(b, ((ikv_string_t*)("nest12")));
  test_helper_assert("nest12 integer", (9 == ikv_value_get_integer(value, 0)));
  ikv_free_all(a);
exit:
  return (status);
}
int main(void) {
  status_declare;
  test_helper_test_one(test_ikv);
exit:
  test_helper_display_summary;
  return ((status.id));
}
