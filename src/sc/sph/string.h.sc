(pre-include "string.h" "stdlib.h" "stdio.h")

(define (ensure-trailing-slash a result) (uint8-t char* char**)
  "set result to a new string with a trailing slash added, or the given string if it already has a trailing slash.
   returns 0 if result is the given string, 1 if new memory could not be allocated, 2 if result is a new string"
  (define a-len uint32-t (strlen a))
  (if (or (not a-len) (= #\/ (pointer-get (+ a (- a-len 1))))) (begin (set *result a) (return 0))
    (begin
      (define new-a char* (malloc (+ 2 a-len)))
      (if (not new-a) (return 1))
      (memcpy new-a a a-len)
      (set (array-get new-a a-len) #\/ (array-get new-a (+ a-len 1)) 0 *result new-a)
      (return 2))))

(define (string-append a b) (char* char* char*)
  "always returns a new string"
  (define a-length size-t (strlen a))
  (define b-length size-t (strlen b))
  (define result char* (malloc (+ 1 a-length b-length)))
  (if (not result) (return 0))
  (memcpy result a a-length)
  (memcpy (+ result a-length) b (+ 1 b-length))
  (set (array-get result (+ a-length b-length)) 0)
  (return result))

(define (string-clone a) (char* char*)
  "return a new string with the same contents as the given string. return 0 if the memory allocation failed"
  (define a-size size-t (+ 1 (strlen a)))
  (define result char* (malloc a-size))
  (if result (memcpy result a a-size))
  (return result))

(define (string-join strings strings-len delimiter result-len) (char* char** size-t char* size-t*)
  "join strings into one string with each input string separated by delimiter.
   zero if strings-len is zero or memory could not be allocated"
  (declare result char* cursor char* total-size size-t part-size size-t delimiter-len size-t)
  (set
    delimiter-len (strlen (convert-type delimiter char*))
    total-size (+ 1 (* delimiter-len (- strings-len 1))))
  (for-each-index i size-t
    strings-len (set total-size (+ total-size (strlen (convert-type (array-get strings i) char*)))))
  (set result (malloc total-size))
  (if (not result) (return 0))
  (set cursor result part-size (strlen (convert-type (array-get strings 0) char*)))
  (memcpy cursor (array-get strings 0) part-size)
  (set cursor (+ cursor part-size))
  (for-each-index-from 1 i
    size-t strings-len (memcpy cursor delimiter delimiter-len)
    (set
      cursor (+ cursor delimiter-len)
      part-size (strlen (convert-type (array-get strings i) char*)))
    (memcpy cursor (array-get strings i) part-size) (set cursor (+ cursor part-size)))
  (set (array-get result (- total-size 1)) 0 *result-len (- total-size 1))
  (return result))

(define (sph-display-bits-u8 a) (void uint8-t)
  (printf "%u" (bit-and 1 a))
  (for-each-index-from 1 i
    uint8-t 8 (printf "%u" (if* (!= (bit-and (bit-shift-left (convert-type 1 uint8-t) i) a) 0) 1 0))))

(define (sph-display-bits a size) (void void* size-t)
  (for-each-index i size-t size (sph-display-bits-u8 (array-get (convert-type a char*) i)))
  (printf "\n"))

(define (sph-helper-uint-to-string a result-len) (char* uintmax-t size-t*)
  (declare t uintmax-t digits size-t result char* p char*)
  (set t a digits 1)
  (while (>= t 10) (set t (/ t 10) digits (+ digits 1)))
  (set result (convert-type (malloc (+ digits 1)) char*))
  (if (not result) (return 0))
  (set p (+ (convert-type result char*) digits) *p 0)
  (do-while (> a 0)
    (set p (- p 1) (pointer-get p) (convert-type (+ 48 (modulo a 10)) char) a (/ a 10)))
  (set *result-len digits)
  (return result))
