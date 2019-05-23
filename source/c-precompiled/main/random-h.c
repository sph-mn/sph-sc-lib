/* depends on types.c */
/** guarantees that all dyadic rationals of the form (k / 2**−53) will be equally likely. this conversion prefers the high bits of x.
     from http://xoshiro.di.unimi.it/ */
#define f64_from_u64(a) ((a >> 11) * (1.0 / (UINT64_C(1) << 53)))
typedef struct {
  u64 data[4];
} random_state_t;
random_state_t random_state_new(u64 seed);
void random(random_state_t* state, u32 size, f64* out);