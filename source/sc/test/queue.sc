(pre-include "inttypes.h" "./test.c" "../main/queue.c")
(pre-define test-element-count 10)
(declare test-element-t (type (struct (data uint32-t) (q queue-node-t))))

(define (test-queue) status-t
  status-declare
  (declare
    a queue-t
    e test-element-t*
    elements (array test-element-t test-element-count)
    i size-t
    j size-t)
  (queue-init &a)
  (sc-comment "insert values")
  (for ((set j 0) (< j 1) (set j (+ 1 j)))
    (for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
      (set e (+ elements i) e:data (- test-element-count i))
      (queue-enq &a &e:q))
    (test-helper-assert "size 1" (= test-element-count a.size))
    (for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
      (set e (queue-get (queue-deq &a) test-element-t q))
      (test-helper-assert "dequeued value" (= (- test-element-count i) e:data)))
    (test-helper-assert "size 2" (= 0 a.size)))
  (label exit (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-queue)
  (label exit (test-helper-display-summary) (return status.id)))