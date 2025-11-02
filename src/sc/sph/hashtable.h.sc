(pre-include-guard-begin sph-hashtable-h-included)
(pre-include "stdlib.h" "string.h" "inttypes.h")

(pre-if (> SIZE_MAX 0xffffffffu)
  (pre-define (sph-hashtable-calculate-size-extra n) (set n (bit-or n (bit-shift-right n 32))))
  (pre-define (sph-hashtable-calculate-size-extra n)))

(pre-define
  sph-hashtable-empty 0
  sph-hashtable-full 1
  (sph-hashtable-hash-integer key hashtable-size) (modulo key hashtable-size)
  (sph-hashtable-equal-integer key-a key-b) (= key-a key-b)
  (sph-hashtable-declare-type name key-type value-type hashtable-hash hashtable-equal size-factor)
  (begin
    (declare (pre-concat name _t)
      (type
        (struct (size size-t) (mask size-t) (flags uint8-t*) (keys key-type*) (values value-type*))))
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
      (sph-hashtable-calculate-size-extra n)
      (return (+ 1 n)))
    (define ((pre-concat name _new) minimum-size out) (uint8-t size-t (pre-concat name _t*))
      (declare a (pre-concat name _t) n size-t)
      (set
        a.flags 0
        a.keys 0
        a.values 0
        n ((pre-concat name _calculate-size) minimum-size)
        a.flags (calloc n 1))
      (if (not a.flags) (return 1))
      (set a.keys (calloc n (sizeof key-type)))
      (if (not a.keys) (begin (free a.flags) (return 1)))
      (set a.values (malloc (* n (sizeof value-type))))
      (if (not a.values) (begin (free a.flags) (free a.keys) (return 1)))
      (set a.size n a.mask (- n 1) *out a)
      (return 0))
    (define ((pre-concat name _clear) a) (void (pre-concat name _t)) (memset a.flags 0 a.size))
    (define ((pre-concat name _free) a) (void (pre-concat name _t))
      (free a.values)
      (free a.keys)
      (free a.flags))
    (define ((pre-concat name _get) a key) (value-type* (pre-concat name _t) key-type)
      (define
        table-size size_t a.size
        mask size_t a.mask
        index size_t (hashtable-hash key table-size)
        steps size_t 0)
      (while (< steps table-size)
        (if (= (array-get a.flags index) sph-hashtable-empty) (return 0))
        (if
          (and (= (array-get a.flags index) sph-hashtable-full)
            (hashtable-equal (array-get a.keys index) key))
          (return (address-of (array-get a.values index))))
        (set index (bit-and (+ index 1) mask))
        (set+ steps 1))
      (return 0))
    (define ((pre-concat name _set) a key value)
      (value-type* (pre-concat name _t) key-type value-type)
      (define
        table-size size_t a.size
        mask size_t a.mask
        index size_t (hashtable-hash key table-size)
        steps size_t 0)
      (while (< steps table-size)
        (if
          (and (= (array-get a.flags index) sph-hashtable-full)
            (hashtable-equal (array-get a.keys index) key))
          (return (+ a.values index)))
        (if (not (= (array-get a.flags index) sph-hashtable-full))
          (begin
            (set
              (array-get a.flags index) sph-hashtable-full
              (array-get a.keys index) key
              (array-get a.values index) value)
            (return (+ a.values index))))
        (set index (bit-and (+ index 1) mask))
        (set+ steps 1))
      (return 0))
    (define ((pre-concat name _remove) a key) (uint8_t (pre-concat name _t) key-type)
      (define
        table-size size-t a.size
        mask size-t a.mask
        index size-t (hashtable-hash key table-size))
      (while 1
        (if (not (= (array-get a.flags index) sph-hashtable-full)) (return 1))
        (if (hashtable-equal (array-get a.keys index) key) break)
        (set index (bit-and (+ index 1) mask)))
      (define hole-index size-t index)
      (declare home-index size-t distance-hole size-t distance-index size-t)
      (while 1
        (set index (bit-and (+ index 1) mask))
        (if (not (= (array-get a.flags index) sph-hashtable-full))
          (begin (set (array-get a.flags hole-index) sph-hashtable-empty) (return 0)))
        (set
          home-index (hashtable-hash (array-get a.keys index) table-size)
          distance-hole (bit-and (- hole-index home-index) mask)
          distance-index (bit-and (- index home-index) mask))
        (if (<= distance-hole distance-index)
          (begin
            (set
              (array-get a.keys hole-index) (array-get a.keys index)
              (array-get a.values hole-index) (array-get a.values index))
            (set hole-index index))))
      (return 1))))

(pre-include-guard-end)
