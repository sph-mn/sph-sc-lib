
#include <inttypes.h>
#include "./test.c"
#include <sph/array3.c>
#include <sph/array4.c>
#define test_element_count 100
array3_declare_type(a3u64, uint64_t);
array4_declare_type(a4u64, uint64_t);
status_t test_arrayn() {
  status_declare;
  size_t i;
  a3u64_t a3;
  a4u64_t a4;
  test_helper_assert("allocation a3", (!a3u64_new(test_element_count, (&a3))));
  test_helper_assert("allocation a4", (!a4u64_new(test_element_count, (&a4))));
  for (i = 0; (i < test_element_count); i += 1) {
    array3_add(a3, (2 + i));
    array4_add(a4, (2 + i));
  };
  test_helper_assert("a3 get", ((2 == array3_get(a3, 0)) && (101 == array3_get(a3, 99))));
  test_helper_assert("a4 get 1", ((2 == array4_get(a4)) && (101 == array4_get_at(a4, 99))));
  while (array4_in_range(a4)) {
    array4_forward(a4);
  };
  test_helper_assert("a4 get 2", (101 == array4_get_at(a4, (a4.current - 1))));
  array4_rewind(a4);
  test_helper_assert("a4 get 3", (2 == array4_get(a4)));
  array3_free(a3);
  array4_free(a4);
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_arrayn);
exit:
  test_helper_display_summary();
  return ((status.id));
}
