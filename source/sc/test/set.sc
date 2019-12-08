(pre-include "stdio.h" "inttypes.h" "./test.c" "../main/sph-set.c")
(pre-define test-element-count 10)
(sph-set-declare-type set32 uint32-t)

(define (print-contents a) (void set32-t)
  (define i size-t 0)
  (printf "------\n")
  (while (< i a.size) (printf "%lu\n" (array-get a.values i)) (set+ i 1)))

(define (test-sph-set) status-t
  status-declare
  (declare a set32-t i uint32-t value uint32-t*)
  (set32-new test-element-count &a)
  (sc-comment "insert values")
  (for ((set i 0) (< i test-element-count) (set+ i 1)) (set32-add a i))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set32-get a i))
    (test-helper-assert "insert value" (and value (= (+ 2 i) *value))))
  (sc-comment "remove values")
  (for ((set i 0) (< i test-element-count) (set+ i 1)) (set32-remove a i))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set32-get a i))
    (test-helper-assert "remove value" (not value)))
  (set32-destroy a)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-sph-set)
  (label exit (test-helper-display-summary) (return status.id)))