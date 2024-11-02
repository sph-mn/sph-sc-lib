(pre-include "./test.c" "../sph/queue.h")
(pre-define test-element-count 10)
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

(define (main) int
  status-declare
  (test-helper-test-one test-queue)
  (label exit (test-helper-display-summary) (return status.id)))