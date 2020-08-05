(sc-comment
  "string-array-hash - a file format and hashtable type for named arrays, possibly nested."
  "depends on stdio.h, inttypes.h, murmur3.c, sph/status.c and sph/hashtable.c."
  "it uses getline which needs #define _GNU_SOURCE before including stdio.h")

(pre-define-if-not-defined
  sah-integer-t uintmax-t
  sah-float-t double
  sah-nesting-t uint8-t
  sah-max-keysize 128
  sah-max-nesting 8)

(pre-define
  sah-type-sah 1
  sah-type-integers 2
  sah-type-floats 3
  sah-type-strings 4
  (sah-equal a b) (= 0 (strncmp a b sah-max-keysize))
  sah-s-group-sah "sah"
  sah-s-id-file-open-failed 1
  sah-s-id-memory 2
  sah-s-id-full 3
  sah-memory-error (status-set-goto sah-s-group-sah sah-s-id-memory))

(define (sah-hash-64 key size) (uint64-t uint8-t* size-t)
  (declare a (array uint64-t 2))
  (MurmurHash3_x64_128 key (strlen key) 0 a)
  (return (array-get a 0)))

(declare sah-value-t (type (struct (type uint8-t) (size sah-integer-t) (data void*))))
(hashtable-declare-type sah uint8-t* sah-value-t sah-hash-64 sah-equal 2)

(define (sah-free-all a) (void sah-t)
  (sc-comment "hash including all sub hashes and data")
  (declare i size-t j size-t b sah-value-t)
  (for ((set i 0) (< i a.size) (set+ i 1))
    (if (not a.flags) continue)
    (set b (array-get a.values i))
    (case = b.type
      ((sah-type-integers sah-type-floats) (free b.data))
      (sah-type-strings
        (for ((set j 0) (< j b.size) (set+ j 1))
          (free (array-get (convert-type b.data uint8-t**) j)))
        (free b.data))
      (sah-type-sah (sah-free-all (pointer-get (convert-type b.data sah-t*))))))
  (sah-free a))

(define (sah-write-file-direct a file nesting) (void sah-t FILE* sah-nesting-t)
  (declare i size-t j size-t b sah-value-t)
  (for ((set i 0) (< i a.size) (set+ i 1))
    (if (not (array-get a.flags i)) continue)
    (for ((set j 0) (< j nesting) (set+ j 1)) (fprintf file "  "))
    (fprintf file "%s" (array-get (convert-type a.keys uint8-t**) i))
    (set b (array-get a.values i))
    (case = b.type
      (sah-type-integers
        (for ((set j 0) (< j b.size) (set+ j 1))
          (fprintf file " %u" (array-get (convert-type b.data sah-integer-t*) j)))
        (fprintf file "\n"))
      (sah-type-floats
        (for ((set j 0) (< j b.size) (set+ j 1))
          (fprintf file " %f" (array-get (convert-type b.data sah-integer-t*) j)))
        (fprintf file "\n"))
      (sah-type-strings
        (for ((set j 0) (< j b.size) (set+ j 1))
          (fprintf file " %s" (array-get (convert-type b.data uint8-t**) j)))
        (fprintf file "\n"))
      (sah-type-sah (fprintf file "\n")
        (sah-write-file-direct (pointer-get (convert-type b.data sah-t*)) file (+ nesting 1))))))

(define (sah-write-file a path) (void sah-t uint8-t*)
  (declare file FILE*)
  (set file (fopen path "w"))
  (sah-write-file-direct a file 0))

(declare sah-read-value-t (type (function-pointer status-t char* size-t sah-value-t*)))

(define (sah-floats-new size out) (status-t size-t sah-float-t**)
  status-declare
  (declare a void*)
  (set a (calloc size (sizeof sah-float-t)))
  (if a (set *out a) sah-memory-error)
  (label exit status-return))

(define (sah-integers-new size out) (status-t size-t sah-integer-t**)
  status-declare
  (declare a void*)
  (set a (calloc size (sizeof sah-integer-t)))
  (if a (set *out a) sah-memory-error)
  (label exit status-return))

(define (sah-read-value line size value) (status-t char* size-t sah-value-t*)
  "value of i is the index of the space after key"
  (declare
    a void*
    count size-t
    i size-t
    i-start size-t
    line-rest char*
    string uint8-t*
    value-i size-t)
  status-declare
  (sc-comment "detect format")
  (set i-start 0 i 1 value:type sah-type-integers)
  (case = (array-get line i)
    ( (#\0 #\1 #\2 #\3 #\4 #\5 #\6 #\7 #\8 #\9) (set+ i 1)
      (while (< i size)
        (case = (array-get line i)
          (#\. (set value:type sah-type-floats) break)
          (#\space break)
          (else (set+ i 1)))))
    (else (set value:type sah-type-strings)))
  (sc-comment "value of i still before the space. count array elements")
  (set i i-start count 0)
  (while (< i size) (if (= #\space (array-get line i)) (set+ count 1)) (set+ i 1))
  (set i i-start)
  (if (= sah-type-strings value:type)
    (begin
      (sc-comment "is string")
      (set a (malloc (* count (sizeof uint8-t*))))
      (if (not a) sah-memory-error)
      (set value:data a value:size count value-i 0)
      (while (< i-start (- size 1))
        (if (= #\space (array-get line i-start))
          (begin
            (sc-comment "read until next space or eos")
            (set+ i-start 1)
            (set i i-start)
            (while (< i size)
              (if (or (= #\space (array-get line i)) (= i (- size 1)))
                (begin
                  (if (= i (- size 1)) (set+ i 1))
                  (set a (malloc (* (+ (- i i-start) 1) (sizeof uint8-t*))))
                  (if (not a) sah-memory-error)
                  (set string a)
                  (memcpy string (+ line i-start) (- i i-start))
                  (set
                    (array-get string (- i-start i)) 0
                    (array-get (convert-type value:data uint8-t**) value-i) string)
                  (set+ value-i 1)
                  break))
              (set+ i 1))))
        (set+ i-start 1)))
    (begin
      (sc-comment "is number")
      (status-require
        (if* (= sah-type-integers value:type)
          (sah-integers-new count (convert-type &value:data sah-integer-t**))
          (sah-floats-new count (convert-type &value:data sah-float-t**))))
      (set value:size count value-i 0)
      (sc-comment "matches space prefixes, -1 to leave room for the last number")
      (while (< i (- size 1))
        (if (= #\space (array-get line i))
          (begin
            (if (= sah-type-integers value:type)
              (set (array-get (convert-type value:data sah-integer-t*) value-i)
                (strtol (+ line i 1) &line-rest 10))
              (set (array-get (convert-type value:data sah-float-t*) value-i)
                (strtod (+ line i 1) &line-rest)))
            (set i (- line-rest line))
            (set+ value-i 1))
          (set+ i 1)))))
  (label exit status-return))

(define (sah-read-indent file read-value sah) (status-t FILE* sah-read-value-t sah-t)
  "reads keys and indentation, creating nested hashtables for keys as required and
   calls read-value to get the value from the rest of a line.
   generic indent key-value reader - custom read-value functions can read custom value formats"
  (declare
    a void*
    i size-t
    value-type uint8-t
    i-start size-t
    key uint8-t*
    line char*
    size size-t
    line-alloc-size size-t
    value sah-value-t
    nested-sah sah-t*
    nesting uint8-t
    nested-keys (array uint8-t* sah-max-nesting)
    nested-sahs (array sah-t sah-max-nesting))
  status-declare
  (set line 0 (array-get nested-sahs 0) sah)
  (while (not (= -1 (getline &line &line-alloc-size file)))
    (set i 0 size (strlen line))
    (if (= 0 size) continue)
    (sc-comment "remove newline")
    (if (= #\newline (array-get line (- size 1))) (set size (- size 1) (array-get line size) 0))
    (sc-comment "skip indent")
    (while (and (< i size) (= #\space (array-get line i))) (set+ i 1))
    (set nesting (if* i (/ i 2) i))
    (sc-comment "get key")
    (while (< i size)
      (if (or (= #\space (array-get line i)) (= i (- size 1)))
        (begin
          (sc-comment "up to space or rest of line")
          (if (= i (- size 1)) (set+ i 1))
          (set a (malloc (+ (- i (* 2 nesting)) 1)))
          (if (not a) sah-memory-error)
          (set key a)
          (memcpy key (+ (* 2 nesting) line) (- i (* 2 nesting)))
          (set (array-get key i) 0 (array-get nested-keys nesting) key)
          (sc-comment "keep i be before the first space")
          break)
        (set+ i 1)))
    (sc-comment "keys without value start nesting")
    (if (= i size)
      (begin
        (set a (malloc (sizeof sah-t)))
        (if (not a) sah-memory-error)
        (set nested-sah a)
        (status-i-require (sah-new 100 nested-sah))
        (set
          (array-get nested-sahs (+ nesting 1)) *nested-sah
          value.type sah-type-sah
          value.size 100
          value.data nested-sah)
        (if (not (sah-set (array-get nested-sahs nesting) key value))
          (begin (free nested-sah) (status-set-goto sah-s-group-sah sah-s-id-full))))
      (begin
        (status-require (read-value (+ line i) (- size i) &value))
        (sc-comment "insert key/value")
        (if (not (sah-set (array-get nested-sahs nesting) (array-get nested-keys nesting) value))
          (status-set-goto sah-s-group-sah sah-s-id-full)))))
  (label exit (if line (free line)) status-return))

(define (sah-read-file path sah) (status-t uint8-t* sah-t)
  "read key/value associations from file and add to hashtable.
   values can be integer/float/string arrays.
   file contains one key-value association per line.
   by using two space indentation in lines subsequent to keys, associations can be nested.
     key int ...
     key decimal int/decimal ...
     key
       key int ...
   key s string ..."
  status-declare
  (declare file FILE*)
  (set file (fopen path "r"))
  (if (not file) (status-set-goto sah-s-group-sah sah-s-id-file-open-failed))
  (status-require (sah-read-indent file sah-read-value sah))
  (label exit (if file (fclose file)) status-return))