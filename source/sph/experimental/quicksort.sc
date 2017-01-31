;based on the optimised c implementation by darel rex finley from http://alienryderflex.com/quicksort/
;the following code depends on sph.sc because of the types used

(pre-define (define-quicksort name type-array type-index max-levels)
  (define (name a element-count) (b8 type-array type-index)
    ;element-count limits a range that is used for sorting in the given array
    (define i type-index 0)
    (define pivot type-index
      start[max-levels] type-index end[max-levels] type-index left type-index right type-index)
    (set (deref start 0) 0) (set (deref end 0) element-count)
    (while (>= i 0) (set left (deref start i))
      (set right (- (deref end i) 1))
      (if (< left right)
        (begin (set pivot (deref a left)) (if (= i (- max-levels 1)) (return 1))
          (while (< left right)
            (while (and (>= (deref a right) pivot) (< left right)) (set right (- right 1))
              (if (< left right) (begin (set left (+ left 1)) (set (deref a left) (deref a right)))))
            (while (and (<= (deref a left) pivot) (< left right)) (set left (+ left 1))
              (if (< left right)
                (begin (set right (- right 1)) (set (deref a right) (deref a left))))))
          (set (deref a left) pivot) (set (deref start (+ i 1)) (+ left 1))
          (set (deref end (+ i 1)) (deref end i)) (set i (+ i 1)) (set (deref end i) left))
        (set i (- i 1))))
    (return 0)))

(define-quicksort quicksort b32* b32 1000)
