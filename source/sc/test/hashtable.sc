(pre-include "stdio.h" "inttypes.h" "./test.c" "../main/hashtable.c")
(pre-define test-element-count 10000000)
(hashtable-declare-type ht6432 uint64-t uint32-t)

(define (print-contents a) (void ht6432-t)
  (define i size-t (- a.size 1))
  (while i (printf "%lu\n" (array-get a.values i)) (set- i 1)))

(define (test-hashtable) status-t
  status-declare
  (declare a ht6432-t i uint32-t value uint32-t)
  (ht6432-new test-element-count &a)
  (sc-comment "test zero")
  (test-helper-assert "zero 1" (= 0 (ht6432-find a 0)))
  (ht6432-add a 0)
  (test-helper-assert "zero 2" (not (= 0 (ht6432-find a 0))))
  (ht6432-remove a 0)
  (test-helper-assert "zero 3" (= 0 (ht6432-find a 0)))
  (sc-comment "insert values")
  (for ((set i 0) (< i test-element-count) (set+ 1 i)) (ht6432-set a i (+ 2 i)))
  (for ((set i 0) (< i test-element-count) (set+ 1 i))
    (set value (ht6432-find a i))
    (test-helper-assert "find value" (and value (= (+ 2 i) value))))
  (ht6432-destroy a)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-hashtable)
  (label exit (test-helper-display-summary) (return status.id)))