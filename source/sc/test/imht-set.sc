(pre-include "stdio.h" "inttypes.h" "./test.c" "../main/imht-set.c")
(pre-define test-element-count 10000000)

(define (print-contents set) (void imht-set-t*)
  (define index size-t (- set:size 1))
  (while index (printf "%lu\n" (array-get set:content index)) (set index (- index 1))))

(define (test-imht-set) s-t
  s-declare
  (declare a imht-set-t* i uint32-t)
  (imht-set-create test-element-count &a)
  (sc-comment "test zero")
  (test-helper-assert "zero 1" (= 0 (imht-set-find a 0)))
  (imht-set-add a 0)
  (test-helper-assert "zero 2" (not (= 0 (imht-set-find a 0))))
  (imht-set-remove a 0)
  (test-helper-assert "zero 3" (= 0 (imht-set-find a 0)))
  (sc-comment "insert values")
  (for ((set i 0) (< i test-element-count) (set i (+ 1 i))) (imht-set-add a i))
  (for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
    (test-helper-assert "find value" (not (= 0 (imht-set-find a i)))))
  (imht-set-destroy a)
  (label exit s-return))

(define (main) int
  s-declare
  (test-helper-test-one test-imht-set)
  (label exit (test-helper-display-summary) (return s-current.id)))