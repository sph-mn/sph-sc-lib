(pre-include "stdio.h" "inttypes.h" "sys/types.h" "./test.c" "../main/quicksort.c")
(pre-define test-element-count 10)
(declare test-struct-t (type (struct (value uint32-t))))

(define (struct-less? a b c) (uint8-t void* ssize-t ssize-t)
  (return
    (< (: (+ b (convert-type a test-struct-t*)) value)
      (: (+ c (convert-type a test-struct-t*)) value))))

(define (struct-swapper a b c) (void void* ssize-t ssize-t)
  (declare d test-struct-t)
  (set
    d (pointer-get (+ b (convert-type a test-struct-t*)))
    (pointer-get (+ b (convert-type a test-struct-t*)))
    (pointer-get (+ c (convert-type a test-struct-t*)))
    (pointer-get (+ c (convert-type a test-struct-t*))) d))

(define (uint32-less? a b c) (uint8-t void* ssize-t ssize-t)
  (return (< (array-get (convert-type a uint32-t*) b) (array-get (convert-type a uint32-t*) c))))

(define (uint32-swapper a b c) (void void* ssize-t ssize-t)
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
    struct-array (array test-struct-t ((* 2 test-element-count)))
    uint32-array (array uint32-t ((* 2 test-element-count)))
    uint32-array-short (array uint32-t 2 0 12))
  (for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
    (set
      struct-element.value (- test-element-count i)
      (array-get struct-array i) struct-element
      (array-get uint32-array i) struct-element.value
      struct-element.value i
      (array-get struct-array (+ test-element-count i)) struct-element
      (array-get uint32-array (+ test-element-count i)) struct-element.value))
  (quicksort struct-less? struct-swapper struct-array 0 (- (* 2 test-element-count) 1))
  (quicksort uint32-less? uint32-swapper uint32-array 0 (- (* 2 test-element-count) 1))
  #;(for ((set i 0) (< i (* 2 test-element-count)) (set i (+ 1 i)))
    (printf "%lu " (array-get uint32-array i)))
  (test-helper-assert "quicksort uint32"
    (and (= 0 (array-get uint32-array 0)) (= 5 (array-get uint32-array test-element-count))
      (= 10 (array-get uint32-array (- (* 2 test-element-count) 1)))))
  (for ((set i 1) (< i (* 2 test-element-count)) (set i (+ 1 i)))
    (test-helper-assert "quicksort uint32 relative"
      (>= (array-get uint32-array i) (array-get uint32-array (- i 1)))))
  (test-helper-assert "quicksort struct"
    (and (= 0 (struct-get (array-get struct-array 0) value))
      (= 5 (struct-get (array-get struct-array test-element-count) value))
      (= 10 (struct-get (array-get struct-array (- (* 2 test-element-count) 1)) value))))
  (for ((set i 1) (< i (* 2 test-element-count)) (set i (+ 1 i)))
    (test-helper-assert "quicksort struct relative"
      (>= (struct-get (array-get struct-array i) value)
        (struct-get (array-get struct-array (- i 1)) value))))
  (quicksort uint32-less? uint32-swapper uint32-array-short 0 1)
  (test-helper-assert "uint32-short"
    (and (= 0 (array-get uint32-array-short 0)) (= 12 (array-get uint32-array-short 1))))
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-quicksort)
  (label exit (test-helper-display-summary) (return status.id)))