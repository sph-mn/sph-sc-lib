(pre-include "inttypes.h" "stdlib.h" "string.h" "sph/test.h")
(sc-comment "array")
(pre-include "sph/array.h" "sph/array4.h")
(pre-define test-element-count 100)
(sph-array-declare-type a3u64 uint64-t)
(array4-declare-type a4u64 uint64-t)

(define (test-arrayn) status-t
  status-declare
  (declare i size-t a3 a3u64-t a4 a4u64-t)
  (status-require (a3u64-new test-element-count &a3))
  (test-helper-assert "allocation a4" (not (a4u64-new test-element-count &a4)))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (sph-array-add a3 (+ 2 i))
    (array4-add a4 (+ 2 i)))
  (test-helper-assert "a3 get" (and (= 2 (sph-array-get a3 0)) (= 101 (sph-array-get a3 99))))
  (test-helper-assert "a4 get 1" (and (= 2 (array4-get a4)) (= 101 (array4-get-at a4 99))))
  (while (array4-in-range a4) (array4-forward a4))
  (test-helper-assert "a4 get 2" (= 101 (array4-get-at a4 (- a4.current 1))))
  (array4-rewind a4)
  (test-helper-assert "a4 get 3" (= 2 (array4-get a4)))
  (a3u64-free &a3)
  (array4-free a4)
  (label exit status-return))

(sc-comment "queue")
(pre-include "sph/queue.h")
(declare test-element-t (type (struct (data uint32-t) (q sph-queue-node-t))))

(define (test-queue) status-t
  status-declare
  (declare
    a sph-queue-t
    e test-element-t*
    elements (array test-element-t test-element-count)
    i size-t
    j size-t)
  (sph-queue-init &a)
  (sc-comment "insert values")
  (for ((set j 0) (< j 1) (set j (+ 1 j)))
    (for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
      (set e (+ elements i) e:data (- test-element-count i))
      (sph-queue-enq &a &e:q))
    (test-helper-assert "size 1" (= test-element-count a.size))
    (for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
      (set e (sph-queue-get (sph-queue-deq &a) test-element-t q))
      (test-helper-assert "dequeued value" (= (- test-element-count i) e:data)))
    (test-helper-assert "size 2" (= 0 a.size)))
  (label exit status-return))

(sc-comment "list")
(pre-include "sph/list.h")
(sph-slist-declare-type slist-u64 uint64-t)
(sph-dlist-declare-type dlist-u64 uint64-t)

(define (test-slist) (status_t)
  status-declare
  (declare
    head (slist-u64-t*)
    node-a (slist-u64-t*)
    node-b (slist-u64-t*)
    node-c (slist-u64-t*)
    count-value size_t
    first-value uint64_t)
  (set
    head 0
    head (slist-u64-add-front head 3)
    head (slist-u64-add-front head 2)
    head (slist-u64-add-front head 1)
    count-value (slist-u64-count head))
  (test-helper-assert "slist count is 3" (= count-value 3))
  (set first-value head:value)
  (test-helper-assert "slist head value is 1" (= first-value 1))
  (set head (slist-u64-remove-front head) count-value (slist-u64-count head))
  (test-helper-assert "slist count is 2" (= count-value 2))
  (set first-value head:value)
  (test-helper-assert "slist head value is 2" (= first-value 2))
  (set node-a (slist-u64-add-front 0 10) node-b (slist-u64-add-front 0 20))
  (slist-u64-append node-a node-b)
  (set node-c node-a:next)
  (test-helper-assert "slist append linked" (= node-c node-b))
  (slist-u64-destroy node-a)
  (slist-u64-destroy head)
  (label exit status-return))

(define (test-dlist) (status_t)
  status-declare
  (declare head (dlist-u64-t*) node1 (dlist-u64-t*) node2 (dlist-u64-t*) node3 (dlist-u64-t*))
  (set
    head 0
    node1 (calloc 1 (sizeof dlist-u64-t))
    node2 (calloc 1 (sizeof dlist-u64-t))
    node3 (calloc 1 (sizeof dlist-u64-t)))
  (test-helper-assert "dlist alloc ok" (and node1 node2 node3))
  (set
    node1:value 1
    node2:value 2
    node3:value 3
    head node1
    node1:previous 0
    node1:next node2
    node2:previous node1
    node2:next node3
    node3:previous node2
    node3:next 0)
  (dlist-u64-validate head)
  (test-helper-assert "dlist validate ok" (= 1 1))
  (dlist-u64-reverse (address-of head))
  (test-helper-assert "dlist reverse head is old tail" (= head node3))
  (test-helper-assert "dlist reverse next chain" (and head:next (= head:next:value 2)))
  (test-helper-assert "dlist reverse tail previous" (and head:next:next (= head:next:next:value 1)))
  (dlist-u64-unlink (address-of head) head:next)
  (dlist-u64-validate head)
  (test-helper-assert "dlist unlink removed middle" (and head:next (= head:next:value 1)))
  (if head:next (set head:next:previous head))
  (dlist-u64-unlink (address-of head) head)
  (test-helper-assert "dlist unlink head moved" (and head (= head:value 1)))
  (dlist-u64-unlink (address-of head) head)
  (test-helper-assert "dlist list empty" (= head 0))
  (free node1)
  (free node2)
  (free node3)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-slist)
  (test-helper-test-one test-dlist)
  (test-helper-test-one test-arrayn)
  (test-helper-test-one test-queue)
  (label exit test-helper-display-summary (return status.id)))
