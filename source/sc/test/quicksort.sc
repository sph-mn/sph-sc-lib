(pre-include "stdio.h" "inttypes.h" "./test.c" "../main/quicksort.c")
(pre-define test-element-count 10)
(declare test-struct-t (type (struct (value uint32-t))))

(define (struct-less? a b c) (uint8-t void* size-t size-t)
  (return
    (< (: (+ b (convert-type a test-struct-t*)) value)
      (: (+ c (convert-type a test-struct-t*)) value))))

(define (struct-swapper a b c) (void void* size-t size-t)
  (declare d test-struct-t)
  (set
    d (pointer-get (+ b (convert-type a test-struct-t*)))
    (pointer-get (+ b (convert-type a test-struct-t*)))
    (pointer-get (+ c (convert-type a test-struct-t*)))
    (pointer-get (+ c (convert-type a test-struct-t*))) d))

(define (uint32-less? a b c) (uint8-t void* size-t size-t)
  (return (< (array-get (convert-type a uint32-t*) b) (array-get (convert-type a uint32-t*) c))))

(define (uint32-swapper a b c) (void void* size-t size-t)
  (declare d uint32-t)
  (set
    d (array-get (convert-type a uint32-t*) b)
    (array-get (convert-type a uint32-t*) b) (array-get (convert-type a uint32-t*) c)
    (array-get (convert-type a uint32-t*) c) d))

(define (test-quicksort) status-t
  status-declare
  (declare
    i uint32-t
    struct-element test-struct-t
    struct-array (array test-struct-t test-element-count)
    uint32-array (array uint32-t test-element-count))
  (for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
    (set
      struct-element.value (- test-element-count i)
      (array-get struct-array i) struct-element
      (array-get uint32-array i) struct-element.value))
  (quicksort struct-less? struct-swapper struct-array test-element-count 0)
  (quicksort uint32-less? uint32-swapper uint32-array test-element-count 0)
  #;(for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
    (printf "%lu " (array-get uint32-array i)))
  #;(for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
    (printf "%lu " (struct-get (array-get struct-array i) value)))
  (test-helper-assert "quicksort uint32"
    (and (= 1 (array-get uint32-array 0))
      (= (+ 1 (/ test-element-count 2)) (array-get uint32-array (/ test-element-count 2)))
      (= test-element-count (array-get uint32-array (- test-element-count 1)))))
  (test-helper-assert "quicksort struct"
    (and (= 1 struct-array:value)
      (= (+ 1 (/ test-element-count 2)) (: (+ struct-array (/ test-element-count 2)) value))
      (= test-element-count (: (+ struct-array (- test-element-count 1)) value))))
  (label exit (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-quicksort)
  (label exit (test-helper-display-summary) (return status.id)))