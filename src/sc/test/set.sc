(pre-include "stdio.h" "inttypes.h" "sph/test.h" "sph/set.h")
(pre-define test-element-count 10000)

(sc-no-semicolon
  (sph-set-declare-type set64 uint64-t sph-set-hash-integer sph-set-equal-integer 0 2))

(define (print-contents a) (void set64-t)
  (define i size-t 0)
  (printf "------\n")
  (while (< i a.size) (printf "%lu " (array-get a.values i)) (set+ i 1)))

(define (test-sph-set) status-t
  status-declare
  (declare a set64-t i uint64-t value uint64-t*)
  (test-helper-assert "allocation" (not (set64-new test-element-count &a)))
  (sc-comment "insert values")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (test-helper-assert "insert" (set64-add &a i)))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set64-get a i))
    (test-helper-assert "insert check" (if* (= 0 i) (not (= 0 value)) (and value (= i *value)))))
  (sc-comment "remove values")
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (test-helper-assert "remove" (not (set64-remove &a i))))
  (for ((set i 0) (< i test-element-count) (set+ i 1))
    (set value (set64-get a i))
    (test-helper-assert "remove check" (not value)))
  (set64-free a)
  (label exit status-return))

(define (test-sph-set-null) (status-t)
  status-declare
  (declare a set64-t p uint64-t*)
  (test-helper-assert "alloc" (not (set64-new 1024 &a)))
  (set p (set64-get a 0))
  (test-helper-assert "null absent initially" (= p 0))
  (set p (set64-add &a 0))
  (test-helper-assert "add null returns ptr" (!= p 0))
  (set p (set64-get a 0))
  (test-helper-assert "get null after add" (!= p 0))
  (test-helper-assert "remove null ok" (not (set64-remove &a 0)))
  (set p (set64-get a 0))
  (test-helper-assert "null absent after remove" (= p 0))
  (test-helper-assert "remove null again not found" (= (set64-remove &a 0) 1))
  (for-each-index-from 1 i size-t 1000 (test-helper-assert "add nonnull" (!= (set64-add &a i) 0)))
  (set p (set64-add &a 0))
  (test-helper-assert "add null with load" (!= p 0))
  (test-helper-assert "remove null with load" (not (set64-remove &a 0)))
  (for-each-index-from 1 i
    size-t 1000
    (test-helper-assert "nonnull still present"
      (and (set64-get a i) (= (pointer-get (set64-get a i)) i))))
  (set64-free a)
  (label exit status-return))

(define (main) int
  status-declare
  (test-helper-test-one test-sph-set)
  (test-helper-test-one test-sph-set-null)
  (label exit test-helper-display-summary (return status.id)))
