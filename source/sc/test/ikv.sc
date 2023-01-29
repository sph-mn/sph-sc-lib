(pre-define _GNU_SOURCE)

(pre-include "stdio.h" "inttypes.h"
  "./test.c" "../foreign/murmur3.c" "sph/hashtable.c" "sph/ikv.h" "sph/ikv.c")

(define (test-ikv) status-t
  (declare a ikv-t b ikv-t value ikv-value-t*)
  status-declare
  (status-i-require (ikv-new 100 &a))
  (sc-comment "read/write")
  (status-require (ikv-read-file "other/ikv-test-data" a))
  (ikv-write-file a "temp/ikv-test")
  (ikv-free-all a)
  (status-i-require (ikv-new 100 &a))
  (status-require (ikv-read-file "temp/ikv-test" a))
  (sc-comment "top level")
  (set value (ikv-get a "key4"))
  (test-helper-assert "key4 string" (= 0 (strcmp "string7" (ikv-value-get-string value 0))))
  (set value (ikv-get a "key3"))
  (test-helper-assert "key3 string array" (= 0 (strcmp "string3" (ikv-value-get-string value 2))))
  (sc-comment "nested")
  (set
    value (ikv-get a "nest1")
    b (ikv-value-get-ikv value)
    value (ikv-get b "nest11")
    value (ikv-get (ikv-value-get-ikv value) "nest111"))
  (test-helper-assert "nest111 string" (= 0 (strcmp "string4" (ikv-value-get-string value 0))))
  (set value (ikv-get b "nest12"))
  (test-helper-assert "nest12 integer" (= 9 (ikv-value-get-integer value 0)))
  (ikv-free-all a)
  (label exit (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-ikv)
  (label exit (test-helper-display-summary) (return status.id)))