/* depends on sph/i_array.c.
similar to sph/memreg.c but uses a specialised heap allocated array for the memory register
  that can be passed between functions
usage:
     memreg_register_t allocations;
     if(!memreg_heap_allocate(4, &allocations)) { return(1); }
     memreg_heap_add(allocations, &variable-1);
     memreg_heap_add(allocations, &variable-2);
     memreg_heap_free(allocations); */
i_array_declare_type(memreg_register_t, void*);
#define memreg_heap_add i_array_add
#define memreg_heap_free_register i_array_free
/** makes sure that values are null and free succeeds even if not allocated yet */
#define memreg_heap_declare(variable_name) i_array_declare(variable_name, memreg_register_t)
/** true on success, false on failure (failed memory allocation) */
#define memreg_heap_allocate(register_size, register_address) i_array_allocate_memreg_register_t(register_size, register_address)
/** free only the registered memory */
#define memreg_heap_free_pointers(reg) \
  while (i_array_in_range(reg)) { \
    free((i_array_get(reg))); \
    i_array_forward(reg); \
  }
/** memreg-register-t -> unspecified
    free all currently registered pointers and the register array */
#define memreg_heap_free(reg) \
  memreg_heap_free_pointers(reg); \
  memreg_heap_free_register(reg)
