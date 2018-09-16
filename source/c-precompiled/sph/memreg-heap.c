/* depends on sph/i_array.c.
similar to sph/memreg.c but uses a specialised heap allocated array for the memory register
  that can be passed between functions
usage:
     memreg_register_t allocations;
     if(!memreg_heap_allocate(4, allocations)) { return(1); }
     memreg_heap_add(allocations, &variable-1);
     memreg_heap_add(allocations, &variable-2);
     memreg_heap_free(allocations); */
i_array_declare_type(memreg_register_t, void*);
#define memreg_heap_add i_array_add
#define memreg_heap_free_register i_array_free
/** true on success, false on failure (failed memory allocation) */
#define memreg_heap_allocate(register_size, register_address) i_array_allocate_memreg_register_t(register_size, register_address)
/** free only the registered memory */
#define memreg_heap_free_pointers(register) \
  while (i_array_in_range(register)) { \
    free((i_array_get(memreg_register))); \
  }
/** free all currently registered pointers and the register array */
#define memreg_heap_free(register) \
  memreg_heap_free_pointers(register); \
  memreg_heap_free_register(register)
