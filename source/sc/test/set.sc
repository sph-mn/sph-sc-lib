(pre-include "stdio.h" "inttypes.h" "./test.c" "../main/set.c")
(pre-define test-element-count 10000)
(sph-set-declare-type set64 uint64-t)

(define (print-contents a) (void set64-t)
  (define i size-t 0)
  (printf "------\n")
  (while (< i a.size) (printf "%lu " (array-get a.values i)) (set+ i 1)))

(define (test-sph-set) status-t
  status-declare
  (declare a set64-t i uint64-t value uint64-t*)
  (set64-new test-element-count &a)
  (sc-comment "insert values")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (test-helper-assert "insert" (set64-add a i)))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set64-get a i))
    (test-helper-assert "insert check" (and value (if* (= 0 i) *value (= i *value)))))
  (sc-comment "remove values")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (test-helper-assert "remove" (not (set64-remove a i))))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set64-get a i))
    (test-helper-assert "remove check" (not value)))
  (set64-free a)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-sph-set)
  (label exit (test-helper-display-summary) (return status.id)))