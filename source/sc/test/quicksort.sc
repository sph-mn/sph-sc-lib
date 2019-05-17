(pre-include "stdio.h" "inttypes.h" "./test.c" "../main/quicksort.c")
(pre-define test-element-count 100000)
(declare test-struct-t (type (struct (value uint32-t))))

(define (struct-less? a b) (uint8-t void* void*)
  (return (< (: (convert-type a test-struct-t*) value) (: (convert-type b test-struct-t*) value))))

(define (struct-swapper a b) (void void* void*)
  (declare c test-struct-t)
  (set
    c (pointer-get (convert-type a test-struct-t*))
    (pointer-get (convert-type a test-struct-t*)) (pointer-get (convert-type b test-struct-t*))
    (pointer-get (convert-type b test-struct-t*)) c))

(define (uint32-less? a b) (uint8-t void* void*)
  (return (< (pointer-get (convert-type a uint32-t*)) (pointer-get (convert-type b uint32-t*)))))

(define (uint32-swapper a b) (void void* void*)
  (declare c uint32-t)
  (set
    c (pointer-get (convert-type a uint32-t*))
    (pointer-get (convert-type a uint32-t*)) (pointer-get (convert-type b uint32-t*))
    (pointer-get (convert-type b uint32-t*)) c))

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
  (quicksort struct-less? struct-swapper (sizeof test-struct-t) struct-array test-element-count)
  (quicksort uint32-less? uint32-swapper 4 uint32-array test-element-count)
  (test-helper-assert "quicksort uint32"
    (and (= 1 (array-get uint32-array 0))
      (= (+ 1 (/ test-element-count 2)) (array-get uint32-array (/ test-element-count 2)))
      (= test-element-count (array-get uint32-array (- test-element-count 1)))))
  (test-helper-assert "quicksort struct"
    (and (= 1 struct-array:value)
      (= (+ 1 (/ test-element-count 2)) (: (+ struct-array (/ test-element-count 2)) value))
      (= test-element-count (: (+ struct-array (- test-element-count 1)) value))))
  #;(for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
    (printf "%lu " (array-get uint32-array i)))
  #;(for ((set i 0) (< i test-element-count) (set i (+ 1 i)))
    (printf "%lu " (struct-get (array-get struct-array i) value)))
  (label exit (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-quicksort)
  (label exit (test-helper-display-summary) (return status.id)))