/* depends on array4.c.
similar to memreg.c but uses a heap allocated array as the memory register
   that can be passed between functions
usage:
   memreg_register_t allocations;
   if(!memreg_heap_new(2, &allocations)) { return(1); }
   memreg_heap_add(allocations, &variable-1);
   memreg_heap_add(allocations, &variable-2);
   memreg_heap_free(allocations); */
array4_declare_type(memreg_register, void*);
#define memreg_heap_add array4_add
#define memreg_heap_free_register array4_free
/** makes sure that values are null and free succeeds even if not allocated yet */
#define memreg_heap_declare(variable_name) array4_declare(variable_name, memreg_register_t)
/** true on success, false on failure (failed memory allocation) */
#define memreg_heap_new(register_size, register_address) memreg_register_new(register_size, register_address)
/** free only the registered memory */
#define memreg_heap_free_pointers(reg) \
  while (array4_in_range(reg)) { \
    free((array4_get(reg))); \
    array4_forward(reg); \
  }
/** memreg-register-t -> unspecified
     free all currently registered pointers and the register array */
#define memreg_heap_free(reg) \
  memreg_heap_free_pointers(reg); \
  memreg_heap_free_register(reg)
