
#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include <sph/hashtable.h>
#define test_element_count 10000
sph_hashtable_declare_type(testht, uint64_t, uint64_t, sph_hashtable_hash_integer, sph_hashtable_equal_integer, 2);
void print_contents(testht_t a) {
  size_t i = 0;
  printf("------\n");
  while ((i < a.size)) {
    printf("%lu\n", ((a.flags)[i]));
    i += 1;
  };
}
status_t test_hashtable() {
  status_declare;
  testht_t a;
  uint64_t i;
  uint64_t* value;
  testht_new(test_element_count, (&a));
  /* insert values */
  for (i = 0; (i < test_element_count); i += 1) {
    test_helper_assert("insert", (testht_set(a, i, (2 + i))));
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = testht_get(a, i);
    test_helper_assert("insert check", (value && ((2 + i) == *value)));
  };
  /* remove values */
  for (i = 0; (i < test_element_count); i += 1) {
    test_helper_assert("remove", (!testht_remove(a, i)));
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = testht_get(a, i);
    test_helper_assert("remove check", !value);
  };
  testht_free(a);
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_hashtable);
exit:
  test_helper_display_summary();
  return ((status.id));
}
