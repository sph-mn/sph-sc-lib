(pre-include "stdio.h" "inttypes.h" "assert.h" "time.h")
(include-sc "../../sph/mi-list")
(pre-define test-element-count 100)

(define (insert-values a) (mi-list-t* mi-list-t*)
  (define counter size-t test-element-count)
  (while counter (set a (mi-list-add a counter)) (set counter (- counter 1))) (mi-list-add a counter))

(define (test-value-existence a) (uint8_t mi-list-t*)
  (define counter size-t 0)
  (while (<= counter test-element-count) (assert (= counter (mi-list-first a)))
    (set a (mi-list-rest a)) (set counter (- counter 1))))

(define (print-contents a) (void mi-list-t*)
  (printf "print-contents\n")
  (while a (printf "%lu\n" (mi-list-first a)) (set a (mi-list-rest a))))

(pre-define (get-time) (convert-type (time 0) uint64_t))
(pre-define (print-time a) (printf "%u\n" a))

(define (main) int
  (define a mi-list-t* (mi-list-create)) (set a (insert-values a))
  (test-value-existence a)
  (print-contents a)
  (mi-list-destroy a) (printf "success\n") (return 0))
