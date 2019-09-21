#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../main/imht-set.c"
#define test_element_count 10000000
void print_contents(imht_set_t* set) {
  size_t index = (set->size - 1);
  while (index) {
    printf("%lu\n", ((set->content)[index]));
    index = (index - 1);
  };
}
status_t test_imht_set() {
  status_declare;
  imht_set_t* a;
  uint32_t i;
  imht_set_create(test_element_count, (&a));
  /* test zero */
  test_helper_assert("zero 1", (0 == imht_set_find(a, 0)));
  imht_set_add(a, 0);
  test_helper_assert("zero 2", (!(0 == imht_set_find(a, 0))));
  imht_set_remove(a, 0);
  test_helper_assert("zero 3", (0 == imht_set_find(a, 0)));
  /* insert values */
  for (i = 0; (i < test_element_count); i = (1 + i)) {
    imht_set_add(a, i);
  };
  for (i = 0; (i < test_element_count); i = (1 + i)) {
    test_helper_assert("find value", (!(0 == imht_set_find(a, i))));
  };
  imht_set_destroy(a);
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_imht_set);
exit:
  test_helper_display_summary();
  return ((status.id));
}
