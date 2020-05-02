/* a fifo queue with the operations enqueue and dequeue that can enqueue custom struct types and a mix of types.
   # example usage
   typedef struct {
     // custom field definitions ...
     queue_node_t queue_node;
   } element_t;
   element_t e;
   queue_t q;
   queue_init(&q);
   queue_enq(&q, &e.queue_node);
   queue_get(queue_deq(&q), element_t, queue_node); */
#include <stdlib.h>
#include <inttypes.h>
#include <stddef.h>
#define queue_size_t uint32_t
/** returns a pointer to the enqueued struct based on the offset of the queue_node_t field in the struct.
     because of this queue nodes dont have to be allocated separate from user data.
     downside is that the same user data object cant be contained multiple times */
#define queue_get(node, type, field) ((type*)((((char*)(node)) - offsetof(type, field))))
struct queue_node_t;
typedef struct queue_node_t {
  struct queue_node_t* next;
} queue_node_t;
typedef struct {
  queue_size_t size;
  queue_node_t* first;
  queue_node_t* last;
} queue_t;
/** initialise a queue or remove all elements */
void queue_init(queue_t* a) {
  a->first = 0;
  a->last = 0;
  a->size = 0;
}
/** enqueue a node. the node must not already be in the queue */
void queue_enq(queue_t* a, queue_node_t* node) {
  if (a->first) {
    a->last->next = node;
  } else {
    a->first = node;
  };
  a->last = node;
  node->next = 0;
  a->size = (1 + a->size);
}
/** queue must not be empty. a.size can be checked to see if the queue is empty */
queue_node_t* queue_deq(queue_t* a) {
  queue_node_t* n;
  n = a->first;
  if (!n->next) {
    a->last = 0;
  };
  a->first = n->next;
  a->size = (a->size - 1);
  return (n);
}
