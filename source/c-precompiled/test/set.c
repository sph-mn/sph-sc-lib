#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../main/set.c"
#define test_element_count 100000000
sph_set_declare_type(set64, uint64_t);
void print_contents(set64_t a) {
  size_t i = 0;
  printf("------\n");
  while ((i < a.size)) {
    printf("%lu ", ((a.values)[i]));
    i += 1;
  };
}
status_t test_sph_set() {
  status_declare;
  set64_t a;
  uint64_t i;
  uint64_t* value;
  set64_new(test_element_count, (&a));
  /* insert values */
  for (i = 0; (i < test_element_count); i += 1) {
    test_helper_assert("insert", (set64_add(a, i)));
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = set64_get(a, i);
    test_helper_assert("insert check", (value && ((0 == i) ? *value : (i == *value))));
  };
  /* remove values */
  for (i = 0; (i < test_element_count); i += 1) {
    test_helper_assert("remove", (!set64_remove(a, i)));
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = set64_get(a, i);
    test_helper_assert("remove check", !value);
  };
  set64_destroy(a);
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_sph_set);
exit:
  test_helper_display_summary();
  return ((status.id));
}
