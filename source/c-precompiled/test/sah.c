#define _GNU_SOURCE
#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../foreign/murmur3.c"
#include "../main/hashtable.c"
#include "../main/sah.c"
#define sah_value_get_string(a, index) ((uint8_t**)(a->data))[index]
status_t test_sah() {
  sah_t a;
  sah_t b;
  sah_value_t* value;
  status_declare;
  status_i_require((sah_new(100, (&a))));
  status_require((sah_read_file("/home/nonroot/testdata", a)));
  /* (sah-write-file a /tmp/sah-test) */
  value = sah_get(a, "key3c");
  /* (printf %s
 (sah-value-get-string value 0)) */
  value = sah_get(a, "key3y");
  /* (printf %s
 (sah-value-get-string value 0)) */
  value = sah_get(a, "nest1");
  b = *((sah_t*)(value->data));
  value = sah_get(b, "nest11");
  value = sah_get((*((sah_t*)(value->data))), "nest111");
  /* (printf %s
 (sah-value-get-string value 0)) */
  value = sah_get(b, "nest13");
  /* (printf %s %lu
 (sah-value-get-string value 0) value:size) */
  sah_free_all(a);
exit:
  return (status);
}
int main() {
  status_declare;
  test_helper_test_one(test_sah);
exit:
  test_helper_display_summary();
  return ((status.id));
}
