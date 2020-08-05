(pre-define _GNU_SOURCE)

(pre-include "stdio.h" "inttypes.h"
  "./test.c" "../foreign/murmur3.c" "../main/hashtable.c" "../main/sah.c")

(pre-define (sah-value-get-string a index) (array-get (convert-type a:data uint8-t**) index))

(define (test-sah) status-t
  (declare a sah-t b sah-t value sah-value-t*)
  status-declare
  (status-i-require (sah-new 100 &a))
  (status-require (sah-read-file "/home/nonroot/testdata" a))
  (sc-comment (sah-write-file a "/tmp/sah-test"))
  (set value (sah-get a "key3c"))
  (sc-comment (printf "%s\n" (sah-value-get-string value 0)))
  (set value (sah-get a "key3y"))
  (sc-comment (printf "%s\n" (sah-value-get-string value 0)))
  (set
    value (sah-get a "nest1")
    b (pointer-get (convert-type value:data sah-t*))
    value (sah-get b "nest11")
    value (sah-get (pointer-get (convert-type value:data sah-t*)) "nest111"))
  (sc-comment (printf "%s\n" (sah-value-get-string value 0)))
  (set value (sah-get b "nest13"))
  (sc-comment (printf "%s %lu\n" (sah-value-get-string value 0) value:size))
  (sah-free-all a)
  (label exit (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-sah)
  (label exit (test-helper-display-summary) (return status.id)))