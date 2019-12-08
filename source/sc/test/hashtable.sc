(pre-include "stdio.h" "inttypes.h" "./test.c" "../main/hashtable.c")
(pre-define test-element-count 100000)
(hashtable-declare-type ht6432 uint64-t uint32-t)

(define (print-contents a) (void ht6432-t)
  (define i size-t 0)
  (printf "------\n")
  (while (< i a.size) (printf "%lu\n" (array-get a.flags i)) (set+ i 1)))

(define (test-hashtable) status-t
  status-declare
  (declare a ht6432-t i uint32-t value uint32-t*)
  (ht6432-new test-element-count &a)
  (sc-comment "insert values")
  (for ((set i 0) (< i test-element-count) (set+ i 1)) (ht6432-set a i (+ 2 i)))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (ht6432-get a i))
    (test-helper-assert "insert value" (and value (= (+ 2 i) *value))))
  (sc-comment "remove values")
  (for ((set i 0) (< i test-element-count) (set+ i 1)) (ht6432-remove a i))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (ht6432-get a i))
    (test-helper-assert "remove value" (not value)))
  (ht6432-destroy a)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-hashtable)
  (label exit (test-helper-display-summary) (return status.id)))