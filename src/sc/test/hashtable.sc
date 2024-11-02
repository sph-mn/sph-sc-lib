(pre-include "stdio.h" "inttypes.h" "./test.c" "sph/hashtable.h")
(pre-define test-element-count 10000)

(sph-hashtable-declare-type testht uint64-t
  uint64-t sph-hashtable-hash-integer sph-hashtable-equal-integer 2)

(define (print-contents a) (void testht-t)
  (define i size-t 0)
  (printf "------\n")
  (while (< i a.size) (printf "%lu\n" (array-get a.flags i)) (set+ i 1)))

(define (test-hashtable) status-t
  status-declare
  (declare a testht-t i uint64-t value uint64-t*)
  (testht-new test-element-count &a)
  (sc-comment "insert values")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (test-helper-assert "insert" (testht-set a i (+ 2 i))))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (testht-get a i))
    (test-helper-assert "insert check" (and value (= (+ 2 i) *value))))
  (sc-comment "remove values")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (test-helper-assert "remove" (not (testht-remove a i))))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (testht-get a i))
    (test-helper-assert "remove check" (not value)))
  (testht-free a)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-hashtable)
  (label exit (test-helper-display-summary) (return status.id)))