
#include "./test.c"
#include "../sph/queue.h"
#define test_element_count 10
typedef struct {
  uint32_t data;
  sph_queue_node_t q;
} test_element_t;
status_t test_queue() {
  status_declare;
  sph_queue_t a;
  test_element_t* e;
  test_element_t elements[test_element_count];
  size_t i;
  size_t j;
  sph_queue_init((&a));
  /* insert values */
  for (j = 0; (j < 1); j = (1 + j)) {
    for (i = 0; (i < test_element_count); i = (1 + i)) {
      e = (elements + i);
      e->data = (test_element_count - i);
      sph_queue_enq((&a), (&(e->q)));
    };
    test_helper_assert("size 1", (test_element_count == a.size));
    for (i = 0; (i < test_element_count); i = (1 + i)) {
      e = sph_queue_get((sph_queue_deq((&a))), test_element_t, q);
      test_helper_assert("dequeued value", ((test_element_count - i) == e->data));
    };
    test_helper_assert("size 2", (0 == a.size));
  };
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_queue);
exit:
  test_helper_display_summary();
  return ((status.id));
}
