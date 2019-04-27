(sc-comment
  "a fifo queue with the operations enqueue, dequeue and is-empty with custom element types.
  # example usage
  typedef struct {
    // custom field definitions ...
    queue_node_t queue_node;
  } element_t;
  element_t e;
  queue_t q;
  queue_init(&q);
  queue_enq(&q, &e.queue_node);
  queue_get(queue_deq(&q), element_t, queue_node);")

(pre-include "stdlib.h" "inttypes.h")
(pre-define queue-size-t uint32-t)

(pre-define (queue-get node type field)
  (convert-type (- (convert-type node char*) (offsetof type field)) type*))

(declare
  queue-node-t struct
  queue-node-t
  (type
    (struct
      next
      (struct
        queue-node-t*)))
  queue-t
  (type
    (struct
      (size queue-size-t)
      (first queue-node-t*)
      (last queue-node-t*))))

(define (queue-init a) (void queue-t*)
  "initialise or clear a queue"
  (set
    a:first 0
    a:last 0
    a:size 0))

(define (queue-enq a node) (void queue-t* queue-node-t*)
  (if a:first (set a:last:next node)
    (set a:first node))
  (set
    a:last node
    node:next 0
    a:size (+ 1 a:size)))

(define (queue-deq a) (queue-node-t* queue-t*)
  "queue must not be empty"
  (declare n queue-node-t*)
  (set n a:first)
  (if (!n:next) (set a:last 0))
  (set
    a:first n:next
    a:size (- a:size 1))
  (return n))