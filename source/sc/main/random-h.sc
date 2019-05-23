(sc-comment "depends on types.c")

(pre-define (f64-from-u64 a)
  (begin
    "guarantees that all dyadic rationals of the form (k / 2**−53) will be equally likely. this conversion prefers the high bits of x.
     from http://xoshiro.di.unimi.it/"
    (* (bit-shift-right a 11) (/ 1.0 (bit-shift-left (UINT64_C 1) 53)))))

(declare
  random-state-t (type (struct (data (array u64 4))))
  (random-state-new seed) (random-state-t u64)
  (random state size out) (void random-state-t* u32 f64*))