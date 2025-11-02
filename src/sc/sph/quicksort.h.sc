(pre-include-guard-begin sph-quicksort-h-included)
(pre-include "sys/types.h")

(define (quicksort less? swap array left right)
  (void (function-pointer uint8-t void* ssize-t ssize-t) (function-pointer void void* ssize-t ssize-t) void* ssize-t ssize-t)
  "a generic quicksort implementation that works with any array type.
   less should return true if the first argument is < than the second.
   swap should exchange the values of the two arguments it receives.
   quicksort(less, swap, array, 0, array-size - 1).
   uses the hoare-partition quicksort algorithm"
  (if (<= right left) return)
  (define pivot ssize-t (+ left (/ (- right left) 2)) l ssize-t left r ssize-t right)
  (while (<= l r)
    (while (less? array l pivot) (set+ l 1))
    (while (less? array pivot r) (set- r 1))
    (if (<= l r)
      (begin
        (cond ((= pivot l) (set pivot r)) ((= pivot r) (set pivot l)))
        (swap array l r)
        (set+ l 1)
        (set- r 1))))
  (if (< left r) (quicksort less? swap array left r))
  (if (< l right) (quicksort less? swap array l right)))

(pre-include-guard-end)
