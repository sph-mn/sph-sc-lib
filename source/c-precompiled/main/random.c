/* depends on <inttypes.h>. sph_ prefix is used because libc uses random */
#define rotl(x, k) ((x << k) | (x >> (64 - k)))
/** guarantees that all dyadic rationals of the form (k / 2**−53) will be equally likely.
     this conversion prefers the high bits of x as is recommended for xoshiro.
     from http://xoshiro.di.unimi.it/ */
#define sph_random_f64_from_u64(a, range) ((a >> 11) * (range / (UINT64_C(1) << 53)))
typedef struct {
  uint64_t data[4];
} sph_random_state_t;
/** use the given u64 as a seed and set state with splitmix64 results.
   the same seed will lead to the same series of pseudo random numbers */
sph_random_state_t sph_random_state_new(uint64_t seed) {
  uint8_t i;
  uint64_t z;
  sph_random_state_t result;
  for (i = 0; (i < 4); i += 1) {
    seed = (seed + UINT64_C(11400714819323198485));
    z = seed;
    z = ((z ^ (z >> 30)) * UINT64_C(13787848793156543929));
    z = ((z ^ (z >> 27)) * UINT64_C(10723151780598845931));
    (result.data)[i] = (z ^ (z >> 31));
  };
  return (result);
}
/** generate uniformly distributed 64 bit unsigned integers.
   implements xoshiro256** from http://xoshiro.di.unimi.it/
   referenced by https://nullprogram.com/blog/2017/09/21/.
   note: most output numbers will be large because small numbers
   require a lot of consecutive zero bits which is unlikely */
uint64_t sph_random_u64(sph_random_state_t* state) {
  uint64_t a;
  size_t i;
  uint64_t t;
  uint64_t* s;
  s = state->data;
  a = (9 * rotl((5 * s[1]), 7));
  t = (s[1] << 17);
  s[2] = (s[2] ^ s[0]);
  s[3] = (s[3] ^ s[1]);
  s[1] = (s[1] ^ s[2]);
  s[0] = (s[0] ^ s[3]);
  s[2] = (s[2] ^ t);
  s[3] = rotl((s[3]), 45);
  return (a);
}
/** generate uniformly distributed unsigned 64 bit integers in range 0..range.
   debiased integer multiplication by lemire, https://arxiv.org/abs/1805.10941
   with enchancement by o'neill, https://www.pcg-random.org/posts/bounded-rands.html */
uint64_t sph_random_u64_bounded(sph_random_state_t* state, uint64_t range) {
  uint64_t x;
  __uint128_t m;
  uint64_t l;
  uint64_t t;
  x = sph_random_u64(state);
  m = (((__uint128_t)(x)) * ((__uint128_t)(range)));
  l = ((uint64_t)(m));
  if (l < range) {
    t = (range);
    if (t >= range) {
      t -= range;
      if (t >= range) {
        t %= range;
      };
    };
    while ((l < t)) {
      x = sph_random_u64(state);
      m = (((__uint128_t)(x)) * ((__uint128_t)(range)));
      l = ((uint64_t)(m));
    };
  };
  return ((m >> 64));
}
/** generate uniformly distributed 64 bit floating point numbers.
   implements xoshiro256+ from http://xoshiro.di.unimi.it/ */
double sph_random_f64_bounded(sph_random_state_t* state, double range) {
  uint64_t a;
  size_t i;
  uint64_t t;
  uint64_t* s;
  s = state->data;
  a = (s[0] + s[3]);
  t = (s[1] << 17);
  s[2] = (s[2] ^ s[0]);
  s[3] = (s[3] ^ s[1]);
  s[1] = (s[1] ^ s[2]);
  s[0] = (s[0] ^ s[3]);
  s[2] = (s[2] ^ t);
  s[3] = rotl((s[3]), 45);
  return ((sph_random_f64_from_u64(a, range)));
}
double sph_random_f64(sph_random_state_t* state) { return ((sph_random_f64_bounded(state, (1.0)))); }
void sph_random_u64_array(sph_random_state_t* state, size_t size, uint64_t* out) {
  size_t i;
  for (i = 0; (i < size); i += 1) {
    out[i] = sph_random_u64(state);
  };
}
void sph_random_u64_bounded_array(sph_random_state_t* state, uint64_t range, size_t size, uint64_t* out) {
  size_t i;
  for (i = 0; (i < size); i += 1) {
    out[i] = sph_random_u64_bounded(state, range);
  };
}
void sph_random_f64_array(sph_random_state_t* state, size_t size, double* out) {
  size_t i;
  for (i = 0; (i < size); i += 1) {
    out[i] = sph_random_f64(state);
  };
}
void sph_random_f64_bounded_array(sph_random_state_t* state, double range, size_t size, double* out) {
  size_t i;
  for (i = 0; (i < size); i += 1) {
    out[i] = sph_random_f64_bounded(state, range);
  };
}
uint32_t sph_random_u32(sph_random_state_t* state) { return ((sph_random_u64(state) >> 32)); }
uint32_t sph_random_u32_bounded(sph_random_state_t* state, uint64_t range) { return ((sph_random_u64_bounded(state, range))); }
void sph_random_u32_array(sph_random_state_t* state, size_t size, uint32_t* out) {
  size_t i;
  for (i = 0; (i < size); i += 1) {
    out[i] = (sph_random_u64(state) >> 32);
  };
}
void sph_random_u32_bounded_array(sph_random_state_t* state, uint64_t range, size_t size, uint32_t* out) {
  size_t i;
  for (i = 0; (i < size); i += 1) {
    out[i] = sph_random_u64_bounded(state, range);
  };
}
