;an array extension for iterating over an array with a state and storing the array size.
;usage: call "array-it-define-type" to define an array-it type and define variables of that type with the data field set to an array.

(pre-define (array-it-define-type name size-t data-t)
  (define-type name (struct (size size-t) (index size-t) (data data-t))))

(pre-define (array-it-next a) (struct-set a index (+ 1 (struct-ref a index))))
(pre-define (array-it-next? a) (< (+ 1 (struct-ref a index)) (struct-ref a size)))
(pre-define (array-it-prev a) (struct-set a index (- (struct-ref a index) 1)))
(pre-define (array-it-prev? a) (<= 0 (- (struct-ref a index) 1)))
(pre-define (array-it-reset a) (struct-set a index 0))
(pre-define (array-it-data a) (struct-ref a data))
(pre-define (array-it-get-address a) (+ (struct-ref a data) (struct-ref a index)))
(pre-define (array-it-get a) (deref (array-it-get-address a)))
