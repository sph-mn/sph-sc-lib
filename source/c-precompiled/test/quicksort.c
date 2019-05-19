#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../main/quicksort.c"
#define test_element_count 10
typedef struct {
  uint32_t value;
} test_struct_t;
uint8_t struct_less_p(void* a, size_t b, size_t c) { return (((b + ((test_struct_t*)(a)))->value < (c + ((test_struct_t*)(a)))->value)); };
void struct_swapper(void* a, size_t b, size_t c) {
  test_struct_t d;
  d = *(b + ((test_struct_t*)(a)));
  *(b + ((test_struct_t*)(a))) = *(c + ((test_struct_t*)(a)));
  *(c + ((test_struct_t*)(a))) = d;
};
uint8_t uint32_less_p(void* a, size_t b, size_t c) { return ((((uint32_t*)(a))[b] < ((uint32_t*)(a))[c])); };
void uint32_swapper(void* a, size_t b, size_t c) {
  uint32_t d;
  d = ((uint32_t*)(a))[b];
  ((uint32_t*)(a))[b] = ((uint32_t*)(a))[c];
  ((uint32_t*)(a))[c] = d;
};
status_t test_quicksort() {
  status_declare;
  uint32_t i;
  test_struct_t struct_element;
  test_struct_t struct_array[test_element_count];
  uint32_t uint32_array[test_element_count];
  for (i = 0; (i < test_element_count); i = (1 + i)) {
    struct_element.value = (test_element_count - i);
    struct_array[i] = struct_element;
    uint32_array[i] = struct_element.value;
  };
  quicksort(struct_less_p, struct_swapper, struct_array, test_element_count, 0);
  quicksort(uint32_less_p, uint32_swapper, uint32_array, test_element_count, 0);
  test_helper_assert("quicksort uint32", ((1 == uint32_array[0]) && ((1 + (test_element_count / 2)) == uint32_array[(test_element_count / 2)]) && (test_element_count == uint32_array[(test_element_count - 1)])));
  test_helper_assert("quicksort struct", ((1 == struct_array->value) && ((1 + (test_element_count / 2)) == (struct_array + (test_element_count / 2))->value) && (test_element_count == (struct_array + (test_element_count - 1))->value)));
exit:
  return (status);
};
int main() {
  status_declare;
  test_helper_test_one(test_quicksort);
exit:
  test_helper_display_summary();
  return ((status.id));
};