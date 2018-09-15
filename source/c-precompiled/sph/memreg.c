/* memreg registers memory in a local variable to free all memory allocated at point
usage:
     memreg_init(4);
     memreg_add(&variable-1);
     memreg_add(&variable-2);
     memreg_free(); */
#define memreg_init(register_size) \
  void* sph_memreg_register[register_size]; \
  unsigned int sph_memreg_index; \
  sph_memreg_index = 0
/** does not protect against buffer overflow */
#define memreg_add(address) \
  sph_memreg_register[sph_memreg_index] = address; \
  sph_memreg_index = (1 + sph_memreg_index)
#define memreg_free \
  while (sph_memreg_index) { \
    sph_memreg_index = (sph_memreg_index - 1); \
    free((*(sph_memreg_register + sph_memreg_index))); \
  }
/* the *-named variant of memreg supports multiple concurrent registers identified by name
usage:
     memreg_init_named(testname, 4);
     memreg_add_named(testname, &variable);
     memreg_free_named(testname); */
#define memreg_init_named(register_id, register_size) \
  void* sph_memreg_register##_##register_id[register_size]; \
  unsigned int sph_memreg_index##_##register_id; \
  sph_memreg_index##_##register_id = 0
/** does not protect against buffer overflow */
#define memreg_add_named(register_id, address) \
  sph_memreg_register##_##register_id[sph_memreg_index##_##register_id] = address; \
  sph_memreg_index##_##register_id = (1 + sph_memreg_index##_##register_id)
#define memreg_free_named(register_id) \
  while (sph_memreg_index##_##register_id) { \
    sph_memreg_index##_##register_id = (sph_memreg_index##_##register_id - 1); \
    free((*(sph_memreg_register##_##register_id + sph_memreg_index##_##register_id))); \
  }
