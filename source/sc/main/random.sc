(sc-comment "depends on random-h.c")
(pre-define (rotl x k) (bit-or (bit-shift-left x k) (bit-shift-right x (- 64 k))))

(define (random-state-new seed) (random-state-t u64)
  "use the given u64 as a seed and set state with splitmix64 results.
   the same seed will lead to the same series of random numbers from sp-random"
  (declare i u8 z u64 result random-state-t)
  (for ((set i 0) (< i 4) (set i (+ 1 i)))
    (set
      seed (+ seed (UINT64_C 11400714819323198485))
      z seed
      z (* (bit-xor z (bit-shift-right z 30)) (UINT64_C 13787848793156543929))
      z (* (bit-xor z (bit-shift-right z 27)) (UINT64_C 10723151780598845931))
      (array-get result.data i) (bit-xor z (bit-shift-right z 31))))
  (return result))

(pre-define (define-random name size-type data-type transfer)
  (define (name state size out) (void random-state-t* size-type data-type*)
    "return uniformly distributed random real numbers in the range -1 to 1.
     implements xoshiro256plus from http://xoshiro.di.unimi.it/
     referenced by https://nullprogram.com/blog/2017/09/21/"
    (declare result-plus u64 i size-type t u64 s random-state-t)
    (set s *state)
    (for ((set i 0) (< i size) (set i (+ 1 i)))
      (set
        result-plus (+ (array-get s.data 0) (array-get s.data 3))
        t (bit-shift-left (array-get s.data 1) 17)
        (array-get s.data 2) (bit-xor (array-get s.data 2) (array-get s.data 0))
        (array-get s.data 3) (bit-xor (array-get s.data 3) (array-get s.data 1))
        (array-get s.data 1) (bit-xor (array-get s.data 1) (array-get s.data 2))
        (array-get s.data 0) (bit-xor (array-get s.data 0) (array-get s.data 3))
        (array-get s.data 2) (bit-xor (array-get s.data 2) t)
        (array-get s.data 3) (rotl (array-get s.data 3) 45)
        (array-get out i) transfer))
    (set *state s)))

(define-random random u32 f64 (f64-from-u64 result-plus))