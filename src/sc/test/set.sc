(pre-include "stdio.h" "inttypes.h" "./test.c" "sph/set.h")
(pre-define test-element-count 10000)
(sph-set-declare-type set64 uint64-t sph-set-hash-integer sph-set-equal-integer 0 1 2)
(sph-set-declare-type-nonull set64nn uint64-t sph-set-hash-integer sph-set-equal-integer 0 2)

(define (print-contents a) (void set64-t)
  (define i size-t 0)
  (printf "------\n")
  (while (< i a.size) (printf "%lu " (array-get a.values i)) (set+ i 1)))

(define (test-sph-set) status-t
  status-declare
  (declare a set64-t b set64nn-t i uint64-t value uint64-t* value-nn uint64-t*)
  (test-helper-assert "allocation" (not (set64-new test-element-count &a)))
  (test-helper-assert "allocation nn" (not (set64nn-new test-element-count &b)))
  (sc-comment "insert values")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (test-helper-assert "insert" (set64-add a i))
    (test-helper-assert "insert nn" (set64nn-add b (+ 1 i))))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set64-get a i) value-nn (set64nn-get b (+ 1 i)))
    (test-helper-assert "insert check" (and value (if* (= 0 i) *value (= i *value))))
    (test-helper-assert "insert check nn" (and value-nn (= (+ 1 i) *value-nn))))
  (sc-comment "remove values")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (test-helper-assert "remove" (not (set64-remove a i)))
    (test-helper-assert "remove nn" (not (set64nn-remove b (+ 1 i)))))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set64-get a i) value-nn (set64nn-get b (+ 1 i)))
    (test-helper-assert "remove check" (not value))
    (test-helper-assert "remove check nn" (not value-nn)))
  (set64-free a)
  (set64nn-free b)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-sph-set)
  (label exit (test-helper-display-summary) (return status.id)))