(pre-define _GNU_SOURCE)

(pre-include "stdio.h" "inttypes.h"
  "sph/test.h" "../foreign/murmur3.c" "sph/hashtable.h" "sph/ikv.h" "sph/ikv.c")

(define (test-ikv) status-t
  (declare a ikv-t b ikv-t value ikv-value-t*)
  status-declare
  (status-i-require (ikv-new 100 &a))
  (sc-comment "read/write")
  (status-require (ikv-read-file (convert-type "other/ikv-test-data" ikv-string-t*) a))
  (ikv-write-file a (convert-type "tmp/ikv-test" ikv-string-t*))
  (ikv-free-all a)
  (status-i-require (ikv-new 100 &a))
  (status-require (ikv-read-file (convert-type "tmp/ikv-test" ikv-string-t*) a))
  (sc-comment "top level")
  (set value (ikv-get a (convert-type "key4" ikv-string-t*)))
  (test-helper-assert "key4 string"
    (= 0 (strcmp "string7" (convert-type (ikv-value-get-string value 0) char*))))
  (set value (ikv-get a (convert-type "key3" ikv-string-t*)))
  (test-helper-assert "key3 string array"
    (= 0 (strcmp "string3" (convert-type (ikv-value-get-string value 2) char*))))
  (sc-comment "nested")
  (set
    value (ikv-get a (convert-type "nest1" ikv-string-t*))
    b (ikv-value-get-ikv value)
    value (ikv-get b (convert-type "nest11" ikv-string-t*))
    value (ikv-get (ikv-value-get-ikv value) (convert-type "nest111" ikv-string-t*)))
  (test-helper-assert "nest111 string"
    (= 0 (strcmp "string4" (convert-type (ikv-value-get-string value 0) char*))))
  (set value (ikv-get b (convert-type "nest12" ikv-string-t*)))
  (test-helper-assert "nest12 integer" (= 9 (ikv-value-get-integer value 0)))
  (ikv-free-all a)
  (label exit (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-ikv)
  (label exit test-helper-display-summary (return status.id)))
