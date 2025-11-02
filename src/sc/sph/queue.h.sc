(pre-include-guard-begin sph-queue-h-included)

(sc-comment
  "a fifo queue with the operations enqueue and dequeue that can enqueue custom struct types and a mix of types.
   # example usage
   typedef struct {
     // custom field definitions ...
     sph_queue-_node_t queue_node;
   } element_t;
   element_t e;
   sph_queue_t q;
   sph_queue_init(&q);
   sph_queue_enq(&q, &e.queue_node);
   sph_queue_get(queue_deq(&q), element_t, queue_node);")

(pre-include "stdlib.h" "inttypes.h" "stddef.h")

(pre-define
  sph-queue-size-t uint32-t
  (sph-queue-get node type field)
  (begin
    "returns a pointer to the enqueued struct based on the offset of the sph_queue_node_t field in the struct.
     because of this, queue nodes do not have to be allocated separate from user data.
     downside is that the same user data object can not be contained multiple times.
     must only be called with a non-null pointer that points to the type"
    (convert-type (- (convert-type node char*) (offsetof type field)) type*)))

(declare
  sph-queue-node-t struct
  sph-queue-node-t (type (struct sph-queue-node-t (next (struct sph-queue-node-t*))))
  sph-queue-t
  (type (struct (size sph-queue-size-t) (first sph-queue-node-t*) (last sph-queue-node-t*))))

(define (sph-queue-init a) (void sph-queue-t*)
  "initialize a queue"
  (set a:first 0 a:last 0 a:size 0))

(define (sph-queue-enq a node) (void sph-queue-t* sph-queue-node-t*)
  "enqueue a node. the node must not already be in the queue. the node must not be null"
  (if a:first (set a:last:next node) (set a:first node))
  (set a:last node node:next 0 a:size (+ 1 a:size)))

(define (sph-queue-deq a) (sph-queue-node-t* sph-queue-t*)
  "must only be called when the queue is not empty. a.size can be used to check if the queue is empty"
  (declare n sph-queue-node-t*)
  (set n a:first)
  (if (not n:next) (set a:last 0))
  (set a:first n:next a:size (- a:size 1))
  (return n))

(pre-include-guard-end)
