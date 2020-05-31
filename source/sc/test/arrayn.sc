(pre-include "inttypes.h" "./test.c" "../main/array3.c" "../main/array4.c")
(pre-define test-element-count 100)
(array3-declare-type a3u64 uint64-t)
(array4-declare-type a4u64 uint64-t)

(define (test-arrayn) status-t
  status-declare
  (declare i size-t a3 a3u64-t a4 a4u64-t)
  (test-helper-assert "allocation a3" (not (a3u64-new test-element-count &a3)))
  (test-helper-assert "allocation a4" (not (a4u64-new test-element-count &a4)))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (array3-add a3 (+ 2 i))
    (array4-add a4 (+ 2 i)))
  (test-helper-assert "a3 get" (and (= 2 (array3-get a3 0)) (= 101 (array3-get a3 99))))
  (test-helper-assert "a4 get 1" (and (= 2 (array4-get a4)) (= 101 (array4-get-at a4 99))))
  (while (array4-in-range a4) (array4-forward a4))
  (test-helper-assert "a4 get 2" (= 101 (array4-get-at a4 (- a4.current 1))))
  (array4-rewind a4)
  (test-helper-assert "a4 get 3" (= 2 (array4-get a4)))
  (array3-free a3)
  (array4-free a4)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-arrayn)
  (label exit (test-helper-display-summary) (return status.id)))