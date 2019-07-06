#include <stdio.h>
#include <inttypes.h>
#include <sys/types.h>
#include "./test.c"
#include "../main/quicksort.c"
#define test_element_count 10
typedef struct {
  uint32_t value;
} test_struct_t;
uint8_t struct_less_p(void* a, ssize_t b, ssize_t c) { return (((b + ((test_struct_t*)(a)))->value < (c + ((test_struct_t*)(a)))->value)); }
void struct_swapper(void* a, ssize_t b, ssize_t c) {
  test_struct_t d;
  d = *(b + ((test_struct_t*)(a)));
  *(b + ((test_struct_t*)(a))) = *(c + ((test_struct_t*)(a)));
  *(c + ((test_struct_t*)(a))) = d;
}
uint8_t uint32_less_p(void* a, ssize_t b, ssize_t c) { return ((((uint32_t*)(a))[b] < ((uint32_t*)(a))[c])); }
void uint32_swapper(void* a, ssize_t b, ssize_t c) {
  uint32_t d;
  d = ((uint32_t*)(a))[b];
  ((uint32_t*)(a))[b] = ((uint32_t*)(a))[c];
  ((uint32_t*)(a))[c] = d;
}
s_t test_quicksort() {
  s_declare;
  uint32_t i;
  test_struct_t struct_element;
  test_struct_t struct_array[(2 * test_element_count)];
  uint32_t uint32_array[(2 * test_element_count)];
  uint32_t uint32_array_short[2] = { 0, 12 };
  for (i = 0; (i < test_element_count); i = (1 + i)) {
    struct_element.value = (test_element_count - i);
    struct_array[i] = struct_element;
    uint32_array[i] = struct_element.value;
    struct_element.value = i;
    struct_array[(test_element_count + i)] = struct_element;
    uint32_array[(test_element_count + i)] = struct_element.value;
  };
  quicksort(struct_less_p, struct_swapper, struct_array, 0, ((2 * test_element_count) - 1));
  quicksort(uint32_less_p, uint32_swapper, uint32_array, 0, ((2 * test_element_count) - 1));
  test_helper_assert("quicksort uint32", ((0 == uint32_array[0]) && (5 == uint32_array[test_element_count]) && (10 == uint32_array[((2 * test_element_count) - 1)])));
  for (i = 1; (i < (2 * test_element_count)); i = (1 + i)) {
    test_helper_assert("quicksort uint32 relative", (uint32_array[i] >= uint32_array[(i - 1)]));
  };
  test_helper_assert("quicksort struct", ((0 == (struct_array[0]).value) && (5 == (struct_array[test_element_count]).value) && (10 == (struct_array[((2 * test_element_count) - 1)]).value)));
  for (i = 1; (i < (2 * test_element_count)); i = (1 + i)) {
    test_helper_assert("quicksort struct relative", ((struct_array[i]).value >= (struct_array[(i - 1)]).value));
  };
  quicksort(uint32_less_p, uint32_swapper, uint32_array_short, 0, 1);
  test_helper_assert("uint32-short", ((0 == uint32_array_short[0]) && (12 == uint32_array_short[1])));
exit:
  s_return;
}
int main() {
  s_declare;
  test_helper_test_one(test_quicksort);
exit:
  test_helper_display_summary();
  return ((s_current.id));
}
