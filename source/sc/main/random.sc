(sc-comment "depends on types.c. sph_ prefix is used because libc uses random")

(pre-define
  (rotl x k) (bit-or (bit-shift-left x k) (bit-shift-right x (- 64 k)))
  (f64-from-u64 a)
  (begin
    "guarantees that all dyadic rationals of the form (k / 2**−53) will be equally likely. this conversion prefers the high bits of x.
     from http://xoshiro.di.unimi.it/"
    (* (bit-shift-right a 11) (/ 1.0 (bit-shift-left (UINT64_C 1) 53))))
  (define-sph-random-u64 name data-type transfer)
  (define (name state size out) (void sph-random-state-t* size-t data-type*)
    "write uniformly distributed 64 bit unsigned integers into out.
     implements xoshiro256** from http://xoshiro.di.unimi.it/
     referenced by https://nullprogram.com/blog/2017/09/21/.
     most output numbers will be large because small numbers
     require a lot of consecutive zero bits which is unlikely"
    (declare a u64 i size-t t u64 s u64*)
    (set s state:data)
    (for ((set i 0) (< i size) (set+ i 1))
      (set
        a (* 9 (rotl (* 5 (array-get s 1)) 7))
        t (bit-shift-left (array-get s 1) 17)
        (array-get s 2) (bit-xor (array-get s 2) (array-get s 0))
        (array-get s 3) (bit-xor (array-get s 3) (array-get s 1))
        (array-get s 1) (bit-xor (array-get s 1) (array-get s 2))
        (array-get s 0) (bit-xor (array-get s 0) (array-get s 3))
        (array-get s 2) (bit-xor (array-get s 2) t)
        (array-get s 3) (rotl (array-get s 3) 45)
        (array-get out i) transfer)))
  (define-sph-random-f64 name data-type transfer)
  (define (name state size out) (void sph-random-state-t* size-t data-type*)
    "write uniformly distributed 64 bit floating point numbers into out.
     implements xoshiro256+ from http://xoshiro.di.unimi.it/"
    (declare a u64 i size-t t u64 s u64*)
    (set s state:data)
    (for ((set i 0) (< i size) (set+ i 1))
      (set
        a (+ (array-get s 0) (array-get s 3))
        t (bit-shift-left (array-get s 1) 17)
        (array-get s 2) (bit-xor (array-get s 2) (array-get s 0))
        (array-get s 3) (bit-xor (array-get s 3) (array-get s 1))
        (array-get s 1) (bit-xor (array-get s 1) (array-get s 2))
        (array-get s 0) (bit-xor (array-get s 0) (array-get s 3))
        (array-get s 2) (bit-xor (array-get s 2) t)
        (array-get s 3) (rotl (array-get s 3) 45)
        (array-get out i) transfer))))

(declare sph-random-state-t (type (struct (data (array u64 4)))))

(define (sph-random-state-new seed) (sph-random-state-t u64)
  "use the given u64 as a seed and set state with splitmix64 results.
   the same seed will lead to the same series of pseudo random numbers"
  (declare i u8 z u64 result sph-random-state-t)
  (for ((set i 0) (< i 4) (set i (+ 1 i)))
    (set
      seed (+ seed (UINT64_C 11400714819323198485))
      z seed
      z (* (bit-xor z (bit-shift-right z 30)) (UINT64_C 13787848793156543929))
      z (* (bit-xor z (bit-shift-right z 27)) (UINT64_C 10723151780598845931))
      (array-get result.data i) (bit-xor z (bit-shift-right z 31))))
  (return result))

(define-sph-random-u64 sph-random-u64 u64 a)
(define-sph-random-f64 sph-random-f64 f64 (f64-from-u64 a))