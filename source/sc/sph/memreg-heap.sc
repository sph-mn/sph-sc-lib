(sc-comment
  "depends on sph/i_array.c."
  "similar to sph/memreg.c but uses a specialised heap allocated array for the memory register
  that can be passed between functions"
  "usage:
     memreg_register_t allocations;
     if(!memreg_heap_allocate(4, allocations)) { return(1); }
     memreg_heap_add(allocations, &variable-1);
     memreg_heap_add(allocations, &variable-2);
     memreg_heap_free(allocations);")

(i-array-declare-type memreg-register-t void*)

(pre-define
  memreg-heap-add i-array-add
  memreg-heap-free-register i-array-free
  (memreg-heap-declare variable-name)
  (begin
    "makes sure that values are null and free succeeds even if not allocated yet"
    (i-array-declare variable-name memreg-register-t))
  (memreg-heap-allocate register-size register-address)
  (begin
    "true on success, false on failure (failed memory allocation)"
    (i-array-allocate-memreg-register-t register-size register-address))
  (memreg-heap-free-pointers register)
  (begin
    "free only the registered memory"
    (while (i-array-in-range register)
      (free (i-array-get register))))
  (memreg-heap-free register)
  (begin
    "free all currently registered pointers and the register array"
    (memreg-heap-free-pointers register)
    (memreg-heap-free-register register)))