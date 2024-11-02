(pre-include-guard-begin sph-ikv-h)

(sc-comment
  "string-array-hash - a file format and hashtable type for named arrays, possibly nested."
  "it uses getline which needs #define _GNU_SOURCE before including stdio.h.
   depends on status.h, hashtable.c, and murmu3.c")

(pre-include "inttypes.h")

(pre-define-if-not-defined
  ikv-integer-t uintmax-t
  ikv-float-t double
  ikv-string-t uint8-t
  ikv-key-t uint8-t
  ikv-type-t uint8-t
  ikv-nesting-t uint8-t
  ikv-max-keysize 128
  ikv-max-nesting 8)

(pre-define
  ikv-type-ikv 1
  ikv-type-integers 2
  ikv-type-floats 3
  ikv-type-strings 4
  ikv-s-group-ikv "ikv"
  ikv-s-id-file-open-failed 1
  ikv-s-id-memory 2
  ikv-s-id-full 3
  ikv-memory-error (status-set-goto ikv-s-group-ikv ikv-s-id-memory)
  (ikv-equal a b) (= 0 (strncmp a b ikv-max-keysize))
  (ikv-value-get-string a index) (array-get (convert-type a:data ikv-string-t**) index)
  (ikv-value-get-integer a index) (array-get (convert-type a:data ikv-integer-t*) index)
  (ikv-value-get-float a index) (array-get (convert-type a:data ikv-float-t*) index)
  (ikv-value-get-ikv a) (pointer-get (convert-type a:data ikv-t*)))

(define (ikv-hash-64 key size) (uint64-t ikv-key-t* size-t)
  (declare a (array uint64-t 2))
  (MurmurHash3_x64_128 key (strlen key) 0 a)
  (return (array-get a 0)))

(declare ikv-value-t (type (struct (type ikv-type-t) (size ikv-integer-t) (data void*))))
(sph-hashtable-declare-type ikv ikv-key-t* ikv-value-t ikv-hash-64 ikv-equal 2)

(declare
  ikv-read-value-t (type (function-pointer status-t char* size-t ikv-value-t*))
  (ikv-free-all a) (void ikv-t)
  (ikv-write-file-direct a file nesting) (void ikv-t FILE* ikv-nesting-t)
  (ikv-write-file a path) (void ikv-t ikv-string-t*)
  (ikv-floats-new size out) (status-t size-t ikv-float-t**)
  (ikv-integers-new size out) (status-t size-t ikv-integer-t**)
  (ikv-read-value line size value) (status-t char* size-t ikv-value-t*)
  (ikv-read-indent file read-value ikv) (status-t FILE* ikv-read-value-t ikv-t)
  (ikv-read-file path ikv) (status-t ikv-string-t* ikv-t))

(pre-include-guard-end)