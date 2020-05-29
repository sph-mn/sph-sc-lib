/* depends on types.c. sph_ prefix is used because libc uses random */
#define rotl(x, k) ((x << k) | (x >> (64 - k)))
/** guarantees that all dyadic rationals of the form (k / 2**−53) will be equally likely. this conversion prefers the high bits of x.
     from http://xoshiro.di.unimi.it/ */
#define sph_random_f64_from_u64(a) ((a >> 11) * (1.0 / (UINT64_C(1) << 53)))
#define sph_random_define_x256ss(name, data_type, transfer) \
  /** write uniformly distributed 64 bit unsigned integers into out. \
       implements xoshiro256** from http://xoshiro.di.unimi.it/ \
       referenced by https://nullprogram.com/blog/2017/09/21/. \
       most output numbers will be large because small numbers \
       require a lot of consecutive zero bits which is unlikely */ \
  void name(sph_random_state_t* state, size_t size, data_type* out) { \
    u64 a; \
    size_t i; \
    u64 t; \
    u64* s; \
    s = state->data; \
    for (i = 0; (i < size); i += 1) { \
      a = (9 * rotl((5 * s[1]), 7)); \
      t = (s[1] << 17); \
      s[2] = (s[2] ^ s[0]); \
      s[3] = (s[3] ^ s[1]); \
      s[1] = (s[1] ^ s[2]); \
      s[0] = (s[0] ^ s[3]); \
      s[2] = (s[2] ^ t); \
      s[3] = rotl((s[3]), 45); \
      out[i] = transfer; \
    }; \
  }
#define sph_random_define_x256p(name, data_type, transfer) \
  /** write uniformly distributed 64 bit floating point numbers into out. \
       implements xoshiro256+ from http://xoshiro.di.unimi.it/ */ \
  void name(sph_random_state_t* state, size_t size, data_type* out) { \
    u64 a; \
    size_t i; \
    u64 t; \
    u64* s; \
    s = state->data; \
    for (i = 0; (i < size); i += 1) { \
      a = (s[0] + s[3]); \
      t = (s[1] << 17); \
      s[2] = (s[2] ^ s[0]); \
      s[3] = (s[3] ^ s[1]); \
      s[1] = (s[1] ^ s[2]); \
      s[0] = (s[0] ^ s[3]); \
      s[2] = (s[2] ^ t); \
      s[3] = rotl((s[3]), 45); \
      out[i] = transfer; \
    }; \
  }
typedef struct {
  u64 data[4];
} sph_random_state_t;
/** use the given u64 as a seed and set state with splitmix64 results.
   the same seed will lead to the same series of pseudo random numbers */
sph_random_state_t sph_random_state_new(u64 seed) {
  u8 i;
  u64 z;
  sph_random_state_t result;
  for (i = 0; (i < 4); i = (1 + i)) {
    seed = (seed + UINT64_C(11400714819323198485));
    z = seed;
    z = ((z ^ (z >> 30)) * UINT64_C(13787848793156543929));
    z = ((z ^ (z >> 27)) * UINT64_C(10723151780598845931));
    (result.data)[i] = (z ^ (z >> 31));
  };
  return (result);
}
sph_random_define_x256ss(sph_random_u64, u64, a)
  sph_random_define_x256p(sph_random_f64, f64, (sph_random_f64_from_u64(a)))
