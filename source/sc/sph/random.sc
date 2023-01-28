(pre-include "sph/random.h")

(pre-define
  (sph-rotl x k) (bit-or (bit-shift-left x k) (bit-shift-right x (- 64 k)))
  (sph-random-f64-from-u64 a)
  (begin
    "guarantees that all dyadic rationals of the form (k / 2**−53) will be equally likely.
     from http://xoshiro.di.unimi.it/
     0x1.0p-53 is a binary floating point constant for 2**-53"
    (* (bit-shift-right a 11) (sc-insert "0x1.0p-53"))))

(define (sph-random-state-new seed) (sph-random-state-t uint64-t)
  "use the given u64 as a seed and set state with splitmix64 results.
   the same seed will lead to the same series of pseudo random numbers"
  (declare z uint64-t result sph-random-state-t)
  (for-each-index i size-t
    4
    (set
      seed (+ seed (UINT64_C 11400714819323198485))
      z seed
      z (* (bit-xor z (bit-shift-right z 30)) (UINT64_C 13787848793156543929))
      z (* (bit-xor z (bit-shift-right z 27)) (UINT64_C 10723151780598845931))
      (array-get result.data i) (bit-xor z (bit-shift-right z 31))))
  (return result))

(define (sph-random-u64 state) (uint64-t sph-random-state-t*)
  "generate uniformly distributed 64 bit unsigned integers.
   implements xoshiro256** from http://xoshiro.di.unimi.it/
   referenced by https://nullprogram.com/blog/2017/09/21/.
   note: most output numbers will be large because small numbers
   require a lot of consecutive zero bits which is unlikely"
  (declare a uint64-t t uint64-t s uint64-t*)
  (set
    s state:data
    a (* 9 (sph-rotl (* 5 (array-get s 1)) 7))
    t (bit-shift-left (array-get s 1) 17)
    (array-get s 2) (bit-xor (array-get s 2) (array-get s 0))
    (array-get s 3) (bit-xor (array-get s 3) (array-get s 1))
    (array-get s 1) (bit-xor (array-get s 1) (array-get s 2))
    (array-get s 0) (bit-xor (array-get s 0) (array-get s 3))
    (array-get s 2) (bit-xor (array-get s 2) t)
    (array-get s 3) (sph-rotl (array-get s 3) 45))
  (return a))

(define (sph-random-u64-bounded state range) (uint64-t sph-random-state-t* uint64-t)
  "generate uniformly distributed unsigned 64 bit integers in range 0..range.
   debiased integer multiplication by lemire, https://arxiv.org/abs/1805.10941
   with enhancement by o'neill, https://www.pcg-random.org/posts/bounded-rands.html"
  (declare x uint64-t m __uint128-t l uint64-t t uint64-t)
  (set
    x (sph-random-u64 state)
    m (* (convert-type x __uint128-t) (convert-type range __uint128-t))
    l (convert-type m uint64-t))
  (if (< l range)
    (begin
      (set t (- range))
      (if (>= t range) (begin (set- t range) (if (>= t range) (set% t range))))
      (while (< l t)
        (set
          x (sph-random-u64 state)
          m (* (convert-type x __uint128-t) (convert-type range __uint128-t))
          l (convert-type m uint64-t)))))
  (return (bit-shift-right m 64)))

(define (sph-random-f64-bounded state range) (double sph-random-state-t* double)
  "generate uniformly distributed 64 bit floating point numbers.
   implements xoshiro256+ from http://xoshiro.di.unimi.it/"
  (declare a uint64-t t uint64-t s uint64-t*)
  (set
    s state:data
    a (+ (array-get s 0) (array-get s 3))
    t (bit-shift-left (array-get s 1) 17)
    (array-get s 2) (bit-xor (array-get s 2) (array-get s 0))
    (array-get s 3) (bit-xor (array-get s 3) (array-get s 1))
    (array-get s 1) (bit-xor (array-get s 1) (array-get s 2))
    (array-get s 0) (bit-xor (array-get s 0) (array-get s 3))
    (array-get s 2) (bit-xor (array-get s 2) t)
    (array-get s 3) (sph-rotl (array-get s 3) 45))
  (return (* range (sph-random-f64-from-u64 a))))

(define (sph-random-f64 state) (double sph-random-state-t*)
  (return (sph-random-f64-from-u64 (sph-random-u64 state))))

(define (sph-random-u64-array state size out) (void sph-random-state-t* size-t uint64-t*)
  (for-each-index i size-t size (set (array-get out i) (sph-random-u64 state))))

(define (sph-random-u64-bounded-array state range size out)
  (void sph-random-state-t* uint64-t size-t uint64-t*)
  (for-each-index i size-t size (set (array-get out i) (sph-random-u64-bounded state range))))

(define (sph-random-f64-array state size out) (void sph-random-state-t* size-t double*)
  (for-each-index i size-t size (set (array-get out i) (sph-random-f64 state))))

(define (sph-random-f64-bounded-array state range size out)
  (void sph-random-state-t* double size-t double*)
  (for-each-index i size-t size (set (array-get out i) (sph-random-f64-bounded state range))))

(define (sph-random-u32 state) (uint32-t sph-random-state-t*)
  (return (bit-shift-right (sph-random-u64 state) 32)))

(define (sph-random-u32-bounded state range) (uint32-t sph-random-state-t* uint32-t)
  (return (sph-random-u64-bounded state range)))

(define (sph-random-u32-array state size out) (void sph-random-state-t* size-t uint32-t*)
  (for-each-index i size-t size (set (array-get out i) (bit-shift-right (sph-random-u64 state) 32))))

(define (sph-random-u32-bounded-array state range size out)
  (void sph-random-state-t* uint32-t size-t uint32-t*)
  (for-each-index i size-t size (set (array-get out i) (sph-random-u64-bounded state range))))

(define (sph-random-f64-1to1 state) (double sph-random-state-t*)
  "return a random floating point number in the range -1 to 1"
  (return (- (sph-random-f64-bounded state 2.0) 1)))

(define (sph-random-f64-array-1to1 state size out) (void sph-random-state-t* size-t double*)
  (for-each-index i size-t size (set (array-get out i) (sph-random-f64-1to1 state))))

(define (sph-random-f64-0to1 state) (double sph-random-state-t*)
  "return a random floating point number in the range -1 to 1"
  (return (sph-random-f64-bounded state 1.0)))

(define (sph-random-f64-array-0to1 state size out) (void sph-random-state-t* size-t double*)
  (for-each-index i size-t size (set (array-get out i) (sph-random-f64-0to1 state))))