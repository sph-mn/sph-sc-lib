#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../main/hashtable.c"
#define test_element_count 100000
hashtable_declare_type(ht6432, uint64_t, uint32_t);
void print_contents(ht6432_t a) {
  size_t i = 0;
  printf("------\n");
  while ((i < a.size)) {
    printf("%lu\n", ((a.flags)[i]));
    i += 1;
  };
}
status_t test_hashtable() {
  status_declare;
  ht6432_t a;
  uint32_t i;
  uint32_t* value;
  ht6432_new(test_element_count, (&a));
  /* insert values */
  for (i = 0; (i < test_element_count); i += 1) {
    ht6432_set(a, i, (2 + i));
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = ht6432_get(a, i);
    test_helper_assert("insert value", (value && ((2 + i) == *value)));
  };
  /* remove values */
  for (i = 0; (i < test_element_count); i += 1) {
    ht6432_remove(a, i);
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = ht6432_get(a, i);
    test_helper_assert("remove value", !value);
  };
  ht6432_destroy(a);
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
