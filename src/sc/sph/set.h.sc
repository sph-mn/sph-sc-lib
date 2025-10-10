(pre-include-guard-begin sph-set-h)

(sc-comment
  "a macro that defines set data types and related functions for arbitrary value types.
   * compared to hashtable.c, this uses less than half the space and operations are faster (about 20% in first tests)
   * linear probing for collision resolve
   * sph-set-declare-type allows the null value (used for unset elements) to be part of the set
     * except for the null value, values are in field .values starting from index 1
     * notnull is used at index 0 to check if the empty-value is included
   * sph-set-declare-type-nonull does not support the null value to be part of the set and should be a bit faster
     * values are in .values, starting from index 0
   * null and notnull arguments are user provided so that they have the same data type as other set elements
   * primes from https://planetmath.org/goodhashtableprimes
   * automatic resizing is not implemented. resizing can be done by re-inserting each value into a larger set")

(pre-include "stdlib.h" "string.h" "inttypes.h")

(pre-if (> SIZE_MAX 0xffffffffu)
  (pre-define (sph-set-calculate-size-extra n) (set n (bit-or n (bit-shift-right n 32))))
  (pre-define (sph-set-calculate-size-extra n)))

(pre-define
  (sph-set-hash-integer value hashtable-size) (modulo value hashtable-size)
  (sph-set-equal-integer value-a value-b) (= value-a value-b)
  (sph-set-declare-type name value-type set-hash set-equal null size-factor)
  (begin
    (declare (pre-concat name _t)
      (type
        (struct
          (size size-t)
          (mask size-t)
          (values value-type*)
          (occupied uint8-t*)
          (nullable uint8-t))))
    (define ((pre-concat name _occupied-get) bitmap index) (uint8-t (const uint8-t*) size-t)
      (declare byte-index size-t bit-index size-t byte-value uint8-t)
      (set
        byte-index (bit-shift-right index 3)
        bit-index (bit-and index 7)
        byte-value (array-get bitmap byte-index))
      (return (bit-and (bit-shift-right byte-value bit-index) 1)))
    (define ((pre-concat name _occupied-set) bitmap index) (void uint8-t* size-t)
      (declare byte-index size-t bit-index size-t)
      (set
        byte-index (bit-shift-right index 3)
        bit-index (bit-and index 7)
        (array-get bitmap byte-index)
        (bit-or (array-get bitmap byte-index) (convert-type (bit-shift-left 1u bit-index) uint8-t))))
    (define ((pre-concat name _occupied-clear) bitmap index) (void uint8-t* size-t)
      (declare byte-index size-t bit-index size-t)
      (set
        byte-index (bit-shift-right index 3)
        bit-index (bit-and index 7)
        (array-get bitmap byte-index)
        (bit-and (convert-type (array-get bitmap byte-index) uint8-t)
          (convert-type (bit-not (bit-shift-left 1u bit-index)) uint8-t))))
    (define ((pre-concat name _calculate-size) n) (size-t size-t)
      (set n (* size-factor n))
      (if (< n 2) (set n 2))
      (set
        n (- n 1)
        n (bit-or n (bit-shift-right n 1))
        n (bit-or n (bit-shift-right n 2))
        n (bit-or n (bit-shift-right n 4))
        n (bit-or n (bit-shift-right n 8))
        n (bit-or n (bit-shift-right n 16)))
      (sph-set-calculate-size-extra n)
      (return (+ 1 n)))
    (define ((pre-concat name _clear) a) (void (pre-concat name _t*))
      (declare bytes size-t)
      (set bytes (bit-shift-right (+ 7 a:size) 3) a:nullable 0)
      (memset a:occupied 0 bytes))
    (define ((pre-concat name _free) a) (void (pre-concat name _t))
      (free a.occupied)
      (free a.values))
    (define ((pre-concat name _new) min-size out) (uint8-t size-t (pre-concat name _t*))
      (declare a (pre-concat name _t) bytes size-t)
      (set
        a.size ((pre-concat name _calculate-size) min-size)
        a.values (malloc (* a.size (sizeof value-type))))
      (if (not a.values) (return 1))
      (set bytes (bit-shift-right (+ a.size 7) 3) a.occupied (calloc bytes 1))
      (if (not a.occupied) (begin (free a.values) (return 1)))
      (set a.nullable 0 a.mask (- a.size 1) *out a)
      (return 0))
    (define ((pre-concat name _get) a value) (value-type* (pre-concat name _t) value-type)
      (declare i size-t j size-t)
      (if (set-equal value null) (return (if* a.nullable a.values 0)))
      (set i (set-hash value a.size) j 0)
      (while (< j a.size)
        (if ((pre-concat name _occupied-get) a.occupied i)
          (if (set-equal (array-get a.values i) value) (return (+ a.values i)))
          (return 0))
        (set i (bit-and (+ i 1) a.mask))
        (set+ j 1))
      (return 0))
    (define ((pre-concat name _add) a value) (value-type* (pre-concat name _t*) value-type)
      (if (set-equal value null) (begin (set a:nullable 1) (return a:values)))
      (define i size_t (set-hash value a:size) j size_t 0 occupied uint8_t 0)
      (while (< j a:size)
        (set occupied ((pre-concat name _occupied-get) a:occupied i))
        (if (and occupied (set-equal (array-get a:values i) value))
          (return (address-of (array-get a:values i))))
        (if (not occupied)
          (begin
            (set (array-get a:values i) value)
            ((pre-concat name _occupied-set) a:occupied i)
            (return (address-of (array-get a:values i)))))
        (set i (bit-and (+ i 1) a:mask))
        (set+ j 1))
      (return 0))
    (define ((pre-concat name _remove) a value) (uint8_t (pre-concat name _t*) value-type)
      (if (set-equal value null)
        (begin (if (not a:nullable) (return 1)) (set a:nullable 0) (return 0)))
      (define
        i size-t (set-hash value a:size)
        j size-t 0
        h size-t 0
        dj size-t 0
        di size-t 0
        found size-t 0)
      (while (not found)
        (if (not ((pre-concat name _occupied-get) a:occupied i)) (return 1))
        (if (set-equal (array-get a:values i) value) (set j i found 1)
          (set i (bit-and (+ i 1) a:mask))))
      (while 1
        (set i (bit-and (+ i 1) a:mask))
        (if (not ((pre-concat name _occupied-get) a:occupied i))
          (begin ((pre-concat name _occupied-clear) a:occupied j) (return 0)))
        (set
          h (set-hash (array-get a:values i) a:size)
          dj (bit-and (- j h) a:mask)
          di (bit-and (- i h) a:mask))
        (if (<= dj di) (begin (set (array-get a:values j) (array-get a:values i)) (set j i))))
      (return 1))))

(pre-include-guard-end)
