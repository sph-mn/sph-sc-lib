
#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include <sph/set.c>
#define test_element_count 10000
sph_set_declare_type(set64, uint64_t, sph_set_hash_integer, sph_set_equal_integer, 0, 1, 2);
sph_set_declare_type_nonull(set64nn, uint64_t, sph_set_hash_integer, sph_set_equal_integer, 0, 2);
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
  set64nn_t b;
  uint64_t i;
  uint64_t* value;
  uint64_t* value_nn;
  test_helper_assert("allocation", (!set64_new(test_element_count, (&a))));
  test_helper_assert("allocation nn", (!set64nn_new(test_element_count, (&b))));
  /* insert values */
  for (i = 0; (i < test_element_count); i += 1) {
    test_helper_assert("insert", (set64_add(a, i)));
    test_helper_assert("insert nn", (set64nn_add(b, (1 + i))));
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = set64_get(a, i);
    value_nn = set64nn_get(b, (1 + i));
    test_helper_assert("insert check", (value && ((0 == i) ? *value : (i == *value))));
    test_helper_assert("insert check nn", (value_nn && ((1 + i) == *value_nn)));
  };
  /* remove values */
  for (i = 0; (i < test_element_count); i += 1) {
    test_helper_assert("remove", (!set64_remove(a, i)));
    test_helper_assert("remove nn", (!set64nn_remove(b, (1 + i))));
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = set64_get(a, i);
    value_nn = set64nn_get(b, (1 + i));
    test_helper_assert("remove check", !value);
    test_helper_assert("remove check nn", !value_nn);
  };
  set64_free(a);
  set64nn_free(b);
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
