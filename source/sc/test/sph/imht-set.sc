(pre-include "stdio.h" "inttypes.h" "assert.h" "time.h")
(sc-include "../../sph/imht-set")

(pre-define
  test-element-count 10000000
  (get-time) (convert-type (time 0) uint64-t)
  (print-time a) (printf "%u\n" a))

(define (test-zero set) (uint8-t imht-set-t*)
  (assert (= 0 (imht-set-find set 0)))
  (imht-set-add set 0)
  (assert (not (= 0 (imht-set-find set 0))))
  (imht-set-remove set 0)
  (assert (= 0 (imht-set-find set 0))))

(define (insert-values set) (uint8-t imht-set-t*)
  (define counter size-t test-element-count)
  (while counter
    (imht-set-add set counter)
    (set counter (- counter 1))))

(define (test-value-existence set) (uint8-t imht-set-t*)
  (define counter size-t test-element-count)
  (while counter
    (assert (not (= 0 (imht-set-find set counter))))
    (set counter (- counter 1))))

(define (print-contents set) (void imht-set-t*)
  (define index size-t (- set:size 1))
  (while index
    (printf "%lu\n" (array-get set:content index))
    (set index (- index 1))))

(define (main) int
  (declare set imht-set-t*)
  (imht-set-create test-element-count (address-of set))
  (test-zero set)
  (insert-values set)
  (test-value-existence set)
  (imht-set-destroy set)
  (printf "success\n")
  (return 0))