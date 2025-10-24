
#include <stdio.h>
#include <inttypes.h>
#include <sph/test.h>
#include <sph/set.h>
#define test_element_count 10000
sph_set_declare_type(set64, uint64_t, sph_set_hash_integer, sph_set_equal_integer, 0, 2);
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
  test_helper_assert("allocation", (!set64_new(test_element_count, (&a))));
  /* insert values */
  for (i = 0; (i < test_element_count); i += 1) {
    test_helper_assert("insert", (set64_add((&a), i)));
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = set64_get(a, i);
    test_helper_assert("insert check", ((0 == i) ? !(0 == value) : (value && (i == *value))));
  };
  /* remove values */
  for (i = 0; (i < test_element_count); i += 1) {
    test_helper_assert("remove", (!set64_remove((&a), i)));
  };
  for (i = 0; (i < test_element_count); i += 1) {
    value = set64_get(a, i);
    test_helper_assert("remove check", !value);
  };
  set64_free(a);
exit:
  status_return;
}
status_t test_sph_set_null() {
  status_declare;
  set64_t a;
  uint64_t* p;
  test_helper_assert("alloc", (!set64_new(1024, (&a))));
  p = set64_get(a, 0);
  test_helper_assert("null absent initially", (p == 0));
  p = set64_add((&a), 0);
  test_helper_assert("add null returns ptr", (p != 0));
  p = set64_get(a, 0);
  test_helper_assert("get null after add", (p != 0));
  test_helper_assert("remove null ok", (!set64_remove((&a), 0)));
  p = set64_get(a, 0);
  test_helper_assert("null absent after remove", (p == 0));
  test_helper_assert("remove null again not found", (set64_remove((&a), 0) == 1));
  for (size_t i = 1; (i < 1000); i += 1) {
    test_helper_assert("add nonnull", (set64_add((&a), i) != 0));
  };
  p = set64_add((&a), 0);
  test_helper_assert("add null with load", (p != 0));
  test_helper_assert("remove null with load", (!set64_remove((&a), 0)));
  for (size_t i = 1; (i < 1000); i += 1) {
    test_helper_assert("nonnull still present", (set64_get(a, i) && (*(set64_get(a, i)) == i)));
  };
  set64_free(a);
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_sph_set);
  test_helper_test_one(test_sph_set_null);
exit:
  test_helper_display_summary;
  return ((status.id));
}
