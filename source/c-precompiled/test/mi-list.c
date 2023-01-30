
#include <inttypes.h>
#include "./test.c"
#include <sph/mi-list.h>
#define test_element_count 100
mi_list_declare_type(mi_list_64, uint64_t);
void print_contents(mi_list_64_t* a) {
  printf("print-contents\n");
  while (a) {
    printf("%lu\n", (mi_list_first(a)));
    a = mi_list_rest(a);
  };
}
status_t test_mi_list() {
  status_declare;
  mi_list_64_t* a;
  uint32_t i;
  a = 0;
  /* insert values */
  for (i = 0; (i < test_element_count); i = (1 + i)) {
    a = mi_list_64_add(a, i);
    test_helper_assert("inserted value accessible", (i == mi_list_first(a)));
  };
  /* check-value-existence */
  for (i = (test_element_count - 1); (i > 0); i = (i - 1), a = mi_list_rest(a)) {
    test_helper_assert("value equal", (i == mi_list_first(a)));
  };
  mi_list_64_destroy(a);
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_mi_list);
exit:
  test_helper_display_summary();
  return ((status.id));
}
