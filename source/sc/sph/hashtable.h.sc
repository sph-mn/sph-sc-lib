(pre-include-guard-begin sph-hashtable-h)
(pre-include "stdlib.h" "string.h" "inttypes.h")

(sc-comment
  "a macro that defines hash-table data types for arbitrary key/value types,"
  "with linear probing for collision resolve and customizable hash and equal functions."
  "prime numbers from https://planetmath.org/goodhashtableprimes")

(declare sph-hashtable-primes
  (array uint32-t ()
    53 97 193
    389 769 1543
    3079 6151 12289
    24593 49157 98317
    196613 393241 786433
    1572869 3145739 6291469
    12582917 25165843 50331653 100663319 201326611 402653189 805306457 1610612741))

(define sph-hashtable-primes-end uint32-t* (+ sph-hashtable-primes 25))

(pre-define
  (sph-hashtable-hash-integer key hashtable-size) (modulo key hashtable-size)
  (sph-hashtable-equal-integer key-a key-b) (= key-a key-b)
  (sph-hashtable-declare-type name key-type value-type hashtable-hash hashtable-equal size-factor)
  (begin
    (declare (pre-concat name _t)
      (type (struct (size size-t) (flags uint8-t*) (keys key-type*) (values value-type*))))
    (define ((pre-concat name _calculate-size) min-size) (size-t size-t)
      (set min-size (* size-factor min-size))
      (declare primes uint32-t*)
      (for ((set primes sph-hashtable-primes) (<= primes sph-hashtable-primes-end) (set+ primes 1))
        (if (<= min-size *primes) (return *primes)))
      (sc-comment "if no prime has been found, make size at least an odd number")
      (return (bit-or 1 min-size)))
    (define ((pre-concat name _new) min-size result) (uint8-t size-t (pre-concat name _t*))
      (declare flags uint8-t* keys key-type* values value-type*)
      (set min-size ((pre-concat name _calculate-size) min-size))
      (set flags (calloc min-size 1))
      (if (not flags) (return 1))
      (set keys (calloc min-size (sizeof key-type)))
      (if (not keys) (begin (free flags) (return 1)))
      (set values (malloc (* min-size (sizeof value-type))))
      (if (not values) (begin (free keys) (free flags) (return 1)))
      (struct-set *result flags flags keys keys values values size min-size)
      (return 0))
    (define ((pre-concat name _free) a) (void (pre-concat name _t))
      (begin (free a.values) (free a.keys) (free a.flags)))
    (define ((pre-concat name _get) a key) (value-type* (pre-concat name _t) key-type)
      "returns the address of the value in the hash table, 0 if it was not found"
      (declare i size-t hash-i size-t)
      (set hash-i (hashtable-hash key a.size) i hash-i)
      (while (< i a.size)
        (if (array-get a.flags i)
          (if (hashtable-equal key (array-get a.keys i)) (return (+ i a.values)))
          (return 0))
        (set+ i 1))
      (sc-comment "wraps over")
      (set i 0)
      (while (< i hash-i)
        (if (array-get a.flags i)
          (if (hashtable-equal key (array-get a.keys i)) (return (+ i a.values)))
          (return 0))
        (set+ i 1))
      (return 0))
    (define ((pre-concat name _set) a key value)
      (value-type* (pre-concat name _t) key-type value-type)
      "returns the address of the added or already included value, 0 if there is no space left in the hash table"
      (declare i size-t hash-i size-t)
      (set hash-i (hashtable-hash key a.size) i hash-i)
      (while (< i a.size)
        (if (array-get a.flags i)
          (if (hashtable-equal key (array-get a.keys i)) (return (+ i a.values)) (set+ i 1))
          (begin
            (set (array-get a.flags i) #t (array-get a.keys i) key (array-get a.values i) value)
            (return (+ i a.values)))))
      (set i 0)
      (while (< i hash-i)
        (if (array-get a.flags i)
          (if (hashtable-equal key (array-get a.keys i)) (return (+ i a.values)) (set+ i 1))
          (begin
            (set (array-get a.flags i) #t (array-get a.keys i) key (array-get a.values i) value)
            (return (+ i a.values)))))
      (return 0))
    (define ((pre-concat name _remove) a key) (uint8-t (pre-concat name _t) key-type)
      "returns 0 if the element was removed, 1 if it was not found.
       only needs to set flag to zero"
      (define value value-type* ((pre-concat name _get) a key))
      (if value (begin (set (array-get a.flags (- value a.values)) 0) (return 0)) (return 1)))
    (define ((pre-concat name _clear) a) (void (pre-concat name _t)) (memset a.flags 0 a.size))))

(pre-include-guard-end)