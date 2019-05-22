(sc-comment "a generic quicksort implementation that works with any array type")

(define (quicksort less? swap array left right)
  (void (function-pointer uint8-t void* size-t size-t) (function-pointer void void* size-t size-t)
    void* size-t size-t)
  "less should return true if the first argument is < than the second.
   swap should exchange the values of the two arguments it receives"
  (if (<= right left) return)
  (define pivot size-t (+ left (/ (- right left) 2)))
  (define l size-t left)
  (define r size-t right)
  (while #t
    (while (less? array l pivot) (set l (+ 1 l)))
    (while (less? array pivot r) (set r (- r 1)))
    (if (> l r) break)
    (cond ((= pivot l) (set pivot r)) ((= pivot r) (set pivot l)))
    (swap array l r)
    (set l (+ 1 l) r (- r 1)))
  (quicksort less? swap array left r)
  (quicksort less? swap array l right))