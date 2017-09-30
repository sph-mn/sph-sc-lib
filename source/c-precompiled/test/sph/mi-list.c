
#ifndef sc_included_stdio_h
#include <stdio.h>
#define sc_included_stdio_h
#endif
#ifndef sc_included_inttypes_h
#include <inttypes.h>
#define sc_included_inttypes_h
#endif
#ifndef sc_included_assert_h
#include <assert.h>
#define sc_included_assert_h
#endif
#ifndef sc_included_time_h
#include <time.h>
#define sc_included_time_h
#endif
include_sc("../../sph/mi-list");
#define test_element_count 100
mi_list_64_t *insert_values(mi_list_64_t *a) {
  size_t counter = test_element_count;
  while (counter) {
    a = mi_list_64_add(a, counter);
    counter = (counter - 1);
  };
  mi_list_64_add(a, counter);
};
uint8_t test_value_existence(mi_list_64_t *a) {
  size_t counter = 0;
  while ((counter <= test_element_count)) {
    assert((counter == mi_list_first(a)));
    a = mi_list_rest(a);
    counter = (counter - 1);
  };
};
void print_contents(mi_list_64_t *a) {
  printf("print-contents\n");
  while (a) {
    printf("%lu\n", mi_list_first(a));
    a = mi_list_rest(a);
  };
};
#define get_time() ((uint64_t)(time(0)))
#define print_time(a) printf("%u\n", a)
int main() {
  mi_list_64_t *a = 0;
  a = insert_values(a);
  test_value_existence(a);
  print_contents(a);
  mi_list_64_destroy(a);
  printf("success\n");
  return (0);
};