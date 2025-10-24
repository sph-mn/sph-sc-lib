
#include <inttypes.h>
#include <stdlib.h>
#include <string.h>
#include <sph/test.h>

/* array */
#include <sph/array.h>
#include <sph/array4.h>
#define test_element_count 100
sph_array_declare_type(a3u64, uint64_t);
array4_declare_type(a4u64, uint64_t);
status_t test_arrayn() {
  status_declare;
  size_t i;
  a3u64_t a3;
  a4u64_t a4;
  status_require((a3u64_new(test_element_count, (&a3))));
  test_helper_assert("allocation a4", (!a4u64_new(test_element_count, (&a4))));
  for (i = 0; (i < test_element_count); i += 1) {
    sph_array_add(a3, (2 + i));
    array4_add(a4, (2 + i));
  };
  test_helper_assert("a3 get", ((2 == sph_array_get(a3, 0)) && (101 == sph_array_get(a3, 99))));
  test_helper_assert("a4 get 1", ((2 == array4_get(a4)) && (101 == array4_get_at(a4, 99))));
  while (array4_in_range(a4)) {
    array4_forward(a4);
  };
  test_helper_assert("a4 get 2", (101 == array4_get_at(a4, (a4.current - 1))));
  array4_rewind(a4);
  test_helper_assert("a4 get 3", (2 == array4_get(a4)));
  a3u64_free((&a3));
  array4_free(a4);
exit:
  status_return;
}

/* queue */
#include <sph/queue.h>
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

/* list */
#include <sph/list.h>
sph_slist_declare_type(slist_u64, uint64_t);
sph_dlist_declare_type(dlist_u64, uint64_t);
status_t test_slist() {
  status_declare;
  slist_u64_t* head;
  slist_u64_t* node_a;
  slist_u64_t* node_b;
  slist_u64_t* node_c;
  size_t count_value;
  uint64_t first_value;
  head = 0;
  head = slist_u64_add_front(head, 3);
  head = slist_u64_add_front(head, 2);
  head = slist_u64_add_front(head, 1);
  count_value = slist_u64_count(head);
  test_helper_assert("slist count is 3", (count_value == 3));
  first_value = head->value;
  test_helper_assert("slist head value is 1", (first_value == 1));
  head = slist_u64_remove_front(head);
  count_value = slist_u64_count(head);
  test_helper_assert("slist count is 2", (count_value == 2));
  first_value = head->value;
  test_helper_assert("slist head value is 2", (first_value == 2));
  node_a = slist_u64_add_front(0, 10);
  node_b = slist_u64_add_front(0, 20);
  slist_u64_append(node_a, node_b);
  node_c = node_a->next;
  test_helper_assert("slist append linked", (node_c == node_b));
  slist_u64_destroy(node_a);
  slist_u64_destroy(head);
exit:
  status_return;
}
status_t test_dlist() {
  status_declare;
  dlist_u64_t* head;
  dlist_u64_t* node1;
  dlist_u64_t* node2;
  dlist_u64_t* node3;
  head = 0;
  node1 = calloc(1, (sizeof(dlist_u64_t)));
  node2 = calloc(1, (sizeof(dlist_u64_t)));
  node3 = calloc(1, (sizeof(dlist_u64_t)));
  test_helper_assert("dlist alloc ok", (node1 && node2 && node3));
  node1->value = 1;
  node2->value = 2;
  node3->value = 3;
  head = node1;
  node1->previous = 0;
  node1->next = node2;
  node2->previous = node1;
  node2->next = node3;
  node3->previous = node2;
  node3->next = 0;
  dlist_u64_validate(head);
  test_helper_assert("dlist validate ok", (1 == 1));
  dlist_u64_reverse((&head));
  test_helper_assert("dlist reverse head is old tail", (head == node3));
  test_helper_assert("dlist reverse next chain", (head->next && (head->next->value == 2)));
  test_helper_assert("dlist reverse tail previous", (head->next->next && (head->next->next->value == 1)));
  dlist_u64_unlink((&head), (head->next));
  dlist_u64_validate(head);
  test_helper_assert("dlist unlink removed middle", (head->next && (head->next->value == 1)));
  if (head->next) {
    head->next->previous = head;
  };
  dlist_u64_unlink((&head), head);
  test_helper_assert("dlist unlink head moved", (head && (head->value == 1)));
  dlist_u64_unlink((&head), head);
  test_helper_assert("dlist list empty", (head == 0));
  free(node1);
  free(node2);
  free(node3);
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_slist);
  test_helper_test_one(test_dlist);
  test_helper_test_one(test_arrayn);
  test_helper_test_one(test_queue);
exit:
  test_helper_display_summary;
  return ((status.id));
}
