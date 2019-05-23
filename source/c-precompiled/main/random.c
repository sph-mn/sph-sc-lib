/* depends on random-h.c */
#define rotl(x, k) ((x << k) | (x >> (64 - k)))
/** use the given u64 as a seed and set state with splitmix64 results.
   the same seed will lead to the same series of random numbers from sp-random */
random_state_t random_state_new(u64 seed) {
  u8 i;
  u64 z;
  random_state_t result;
  for (i = 0; (i < 4); i = (1 + i)) {
    seed = (seed + UINT64_C(11400714819323198485));
    z = seed;
    z = ((z ^ (z >> 30)) * UINT64_C(13787848793156543929));
    z = ((z ^ (z >> 27)) * UINT64_C(10723151780598845931));
    (result.data)[i] = (z ^ (z >> 31));
  };
  return (result);
};
#define define_random(name, size_type, data_type, transfer) \
  /** return uniformly distributed random real numbers in the range -1 to 1. \
       implements xoshiro256plus from http://xoshiro.di.unimi.it/ \
       referenced by https://nullprogram.com/blog/2017/09/21/ */ \
  void name(random_state_t* state, size_type size, data_type* out) { \
    u64 result_plus; \
    size_type i; \
    u64 t; \
    random_state_t s; \
    s = *state; \
    for (i = 0; (i < size); i = (1 + i)) { \
      result_plus = ((s.data)[0] + (s.data)[3]); \
      t = ((s.data)[1] << 17); \
      (s.data)[2] = ((s.data)[2] ^ (s.data)[0]); \
      (s.data)[3] = ((s.data)[3] ^ (s.data)[1]); \
      (s.data)[1] = ((s.data)[1] ^ (s.data)[2]); \
      (s.data)[0] = ((s.data)[0] ^ (s.data)[3]); \
      (s.data)[2] = ((s.data)[2] ^ t); \
      (s.data)[3] = rotl(((s.data)[3]), 45); \
      out[i] = transfer; \
    }; \
    *state = s; \
  }
define_random(random, u32, f64, (f64_from_u64(result_plus)));