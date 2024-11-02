(define (ikv-free-all a) (void ikv-t)
  (sc-comment "hash including all sub hashes and data")
  (declare i size-t j size-t b ikv-value-t)
  (for ((set i 0) (< i a.size) (set+ i 1))
    (if (not a.flags) continue)
    (set b (array-get a.values i))
    (case = b.type
      ((ikv-type-integers ikv-type-floats) (free b.data))
      (ikv-type-strings
        (for ((set j 0) (< j b.size) (set+ j 1))
          (free (array-get (convert-type b.data ikv-string-t**) j)))
        (free b.data))
      (ikv-type-ikv (ikv-free-all (pointer-get (convert-type b.data ikv-t*))))))
  (ikv-free a))

(define (ikv-write-file-direct a file nesting) (void ikv-t FILE* ikv-nesting-t)
  (declare i size-t j size-t b ikv-value-t)
  (for ((set i 0) (< i a.size) (set+ i 1))
    (if (not (array-get a.flags i)) continue)
    (for ((set j 0) (< j nesting) (set+ j 1)) (fprintf file "  "))
    (fprintf file "%s" (array-get (convert-type a.keys ikv-key-t**) i))
    (set b (array-get a.values i))
    (case = b.type
      (ikv-type-integers
        (for ((set j 0) (< j b.size) (set+ j 1)) (fprintf file " %u" (ikv-value-get-integer &b j)))
        (fprintf file "\n"))
      (ikv-type-floats
        (for ((set j 0) (< j b.size) (set+ j 1)) (fprintf file " %f" (ikv-value-get-float &b j)))
        (fprintf file "\n"))
      (ikv-type-strings
        (for ((set j 0) (< j b.size) (set+ j 1)) (fprintf file " %s" (ikv-value-get-string &b j)))
        (fprintf file "\n"))
      (ikv-type-ikv (fprintf file "\n")
        (ikv-write-file-direct (ikv-value-get-ikv &b) file (+ nesting 1))))))

(define (ikv-write-file a path) (void ikv-t ikv-string-t*)
  (declare file FILE*)
  (set file (fopen path "w"))
  (ikv-write-file-direct a file 0)
  (fclose file))

(define (ikv-floats-new size out) (status-t size-t ikv-float-t**)
  status-declare
  (declare a void*)
  (set a (calloc size (sizeof ikv-float-t)))
  (if a (set *out a) ikv-memory-error)
  (label exit status-return))

(define (ikv-integers-new size out) (status-t size-t ikv-integer-t**)
  status-declare
  (declare a void*)
  (set a (calloc size (sizeof ikv-integer-t)))
  (if a (set *out a) ikv-memory-error)
  (label exit status-return))

(define (ikv-read-value line size value) (status-t char* size-t ikv-value-t*)
  "value of i is the index of the space after key"
  (declare
    a void*
    count size-t
    i size-t
    i-start size-t
    line-rest char*
    string ikv-string-t*
    value-i size-t)
  status-declare
  (sc-comment "detect format")
  (set i-start 0 i 1 value:type ikv-type-integers)
  (case = (array-get line i)
    ( (#\0 #\1 #\2 #\3 #\4 #\5 #\6 #\7 #\8 #\9) (set+ i 1)
      (while (< i size)
        (case = (array-get line i)
          (#\. (set value:type ikv-type-floats) break)
          (#\space break)
          (else (set+ i 1)))))
    (else (set value:type ikv-type-strings)))
  (sc-comment "value of i still before the space. count array elements")
  (set i i-start count 0)
  (while (< i size) (if (= #\space (array-get line i)) (set+ count 1)) (set+ i 1))
  (set i i-start)
  (if (= ikv-type-strings value:type)
    (begin
      (sc-comment "is string")
      (set a (malloc (* count (sizeof ikv-string-t*))))
      (if (not a) ikv-memory-error)
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
                  (set a (malloc (* (+ (- i i-start) 1) (sizeof ikv-string-t*))))
                  (if (not a) ikv-memory-error)
                  (set string a)
                  (memcpy string (+ line i-start) (- i i-start))
                  (set
                    (array-get string (- i-start i)) 0
                    (array-get (convert-type value:data ikv-string-t**) value-i) string)
                  (set+ value-i 1)
                  break))
              (set+ i 1))))
        (set+ i-start 1)))
    (begin
      (sc-comment "is number")
      (status-require
        (if* (= ikv-type-integers value:type)
          (ikv-integers-new count (convert-type &value:data ikv-integer-t**))
          (ikv-floats-new count (convert-type &value:data ikv-float-t**))))
      (set value:size count value-i 0)
      (sc-comment "matches space prefixes, -1 to leave room for the last number")
      (while (< i (- size 1))
        (if (= #\space (array-get line i))
          (begin
            (if (= ikv-type-integers value:type)
              (set (array-get (convert-type value:data ikv-integer-t*) value-i)
                (strtol (+ line i 1) &line-rest 10))
              (set (array-get (convert-type value:data ikv-float-t*) value-i)
                (strtod (+ line i 1) &line-rest)))
            (set i (- line-rest line))
            (set+ value-i 1))
          (set+ i 1)))))
  (label exit status-return))

(define (ikv-read-indent file read-value ikv) (status-t FILE* ikv-read-value-t ikv-t)
  "reads keys and indentation, creating nested hashtables for keys as required and
   calls read-value to get the value from the rest of a line.
   generic indent key-value reader - custom read-value functions can read custom value formats"
  (declare
    a void*
    i size-t
    key ikv-key-t*
    line char*
    size size-t
    line-alloc-size size-t
    value ikv-value-t
    nested-ikv ikv-t*
    nesting ikv-nesting-t
    nested-keys (array ikv-key-t* ikv-max-nesting)
    nested-ikvs (array ikv-t ikv-max-nesting))
  status-declare
  (set line 0 (array-get nested-ikvs 0) ikv)
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
          (if (not a) ikv-memory-error)
          (set key a)
          (memcpy key (+ (* 2 nesting) line) (- i (* 2 nesting)))
          (set (array-get key i) 0 (array-get nested-keys nesting) key)
          (sc-comment "keep i be before the first space")
          break)
        (set+ i 1)))
    (sc-comment "keys without value start nesting")
    (if (= i size)
      (begin
        (set a (malloc (sizeof ikv-t)))
        (if (not a) ikv-memory-error)
        (set nested-ikv a)
        (status-i-require (ikv-new 100 nested-ikv))
        (set
          (array-get nested-ikvs (+ nesting 1)) *nested-ikv
          value.type ikv-type-ikv
          value.size 100
          value.data nested-ikv)
        (if (not (ikv-set (array-get nested-ikvs nesting) key value))
          (begin (free nested-ikv) (status-set-goto ikv-s-group-ikv ikv-s-id-full))))
      (begin
        (status-require (read-value (+ line i) (- size i) &value))
        (sc-comment "insert key/value")
        (if (not (ikv-set (array-get nested-ikvs nesting) (array-get nested-keys nesting) value))
          (status-set-goto ikv-s-group-ikv ikv-s-id-full)))))
  (label exit (if line (free line)) status-return))

(define (ikv-read-file path ikv) (status-t ikv-string-t* ikv-t)
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
  (if (not file) (status-set-goto ikv-s-group-ikv ikv-s-id-file-open-failed))
  (status-require (ikv-read-indent file ikv-read-value ikv))
  (label exit (if file (fclose file)) status-return))