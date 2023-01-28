(pre-include "inttypes.h" "./test.c" "sph/mi-list.c")
(pre-define test-element-count 100)
(mi-list-declare-type mi-list-64 uint64-t)

(define (print-contents a) (void mi-list-64-t*)
  (printf "print-contents\n")
  (while a (printf "%lu\n" (mi-list-first a)) (set a (mi-list-rest a))))

(define (test-mi-list) status-t
  status-declare
  (declare a mi-list-64-t* i uint32-t)
  (set a 0)
  (sc-comment "insert values")
  (for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
    (set a (mi-list-64-add a i))
    (test-helper-assert "inserted value accessible" (= i (mi-list-first a))))
  (sc-comment "check-value-existence")
  (for ((set i (- test-element-count 1)) (> i 0) (set i (- i 1) a (mi-list-rest a)))
    (test-helper-assert "value equal" (= i (mi-list-first a))))
  (mi-list-64-destroy a)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-mi-list)
  (label exit (test-helper-display-summary) (return status.id)))