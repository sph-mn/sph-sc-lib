(pre-include "stdio.h" "inttypes.h" "./test.c" "../main/set.c")
(pre-define test-element-count 100)
(sph-set-declare-type set64 uint64-t)

(define (print-contents a) (void set64-t)
  (define i size-t 0)
  (printf "------\n")
  (while (< i a.size) (printf "%lu\n" (array-get a.values i)) (set+ i 1)))

(define (test-sph-set) status-t
  status-declare
  (declare a set64-t i uint64-t value uint64-t*)
  (set64-new test-element-count &a)
  (sc-comment "insert values")
  (printf "insert\n")
  (for ((set i 0) (< i test-element-count) (set+ i 1)) (set64-add a i))
  (printf "test insert\n")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set64-get a i))
    (test-helper-assert "insert value" (and value (if* (= 0 i) *value (= i *value)))))
  (printf "remove\n")
  (sc-comment "remove values")
  (for ((set i 0) (< i test-element-count) (set+ i 1)) (set64-remove a i))
  (printf "test remove\n")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set64-get a i))
    (test-helper-assert "remove value" (not value)))
  (set64-destroy a)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-sph-set)
  (label exit (test-helper-display-summary) (return status.id)))