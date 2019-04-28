#include <stdio.h>
#include <inttypes.h>
#include "./test.c"
#include "../main/quicksort.c"
#define test_element_count 100000
typedef struct {
  uint32_t value;
} test_struct_t;
uint8_t struct_less_p(void* a, void* b) { return ((((test_struct_t*)(a))->value < ((test_struct_t*)(b))->value)); };
void struct_swapper(void* a, void* b) {
  test_struct_t c;
  c = *((test_struct_t*)(a));
  *((test_struct_t*)(a)) = *((test_struct_t*)(b));
  *((test_struct_t*)(b)) = c;
};
uint8_t uint32_less_p(void* a, void* b) { return ((*((uint32_t*)(a)) < *((uint32_t*)(b)))); };
void uint32_swapper(void* a, void* b) {
  uint32_t c;
  c = *((uint32_t*)(a));
  *((uint32_t*)(a)) = *((uint32_t*)(b));
  *((uint32_t*)(b)) = c;
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
  quicksort(struct_less_p, struct_swapper, (sizeof(test_struct_t)), struct_array, test_element_count);
  quicksort(uint32_less_p, uint32_swapper, 4, uint32_array, test_element_count);
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