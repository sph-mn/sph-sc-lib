#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../main/sph-set.c"
#define test_element_count 10
sph_set_declare_type(set32, uint32_t);
void print_contents(set32_t a) {
  size_t i = 0;
  printf("------\n");
  while ((i < a.size)) {
    printf("%lu\n", ((a.values)[i]));
    i += 1;
  };
}
status_t test_sph_set() {
  status_declare;
  set32_t a;
  uint32_t i;
  uint32_t* value;
  set32_new(test_element_count, (&a));
  /* insert values */
  for (i = 0; (i < test_element_count); i += 1) {
    set32_add(a, i);
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = set32_get(a, i);
    test_helper_assert("insert value", (value && ((2 + i) == *value)));
  };
  /* remove values */
  for (i = 0; (i < test_element_count); i += 1) {
    set32_remove(a, i);
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = set32_get(a, i);
    test_helper_assert("remove value", !value);
  };
  set32_destroy(a);
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
