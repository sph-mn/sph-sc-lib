(sc-comment
  "depends on array4.c."
  "similar to memreg.c but uses a heap allocated array as the memory register
   that can be passed between functions"
  "usage:
   memreg_register_t allocations;
   if(!memreg_heap_new(2, &allocations)) { return(1); }
   memreg_heap_add(allocations, &variable-1);
   memreg_heap_add(allocations, &variable-2);
   memreg_heap_free(allocations);")

(array4-declare-type memreg-register void*)

(pre-define
  memreg-heap-add array4-add
  memreg-heap-free-register array4-free
  (memreg-heap-declare variable-name)
  (begin
    "makes sure that values are null and free succeeds even if not allocated yet"
    (array4-declare variable-name memreg-register-t))
  (memreg-heap-new register-size register-address)
  (begin
    "true on success, false on failure (failed memory allocation)"
    (memreg-register-new register-size register-address))
  (memreg-heap-free-pointers reg)
  (begin
    "free only the registered memory"
    (while (array4-in-range reg) (free (array4-get reg)) (array4-forward reg)))
  (memreg-heap-free reg)
  (begin
    "memreg-register-t -> unspecified
     free all currently registered pointers and the register array"
    (memreg-heap-free-pointers reg)
    (memreg-heap-free-register reg)))