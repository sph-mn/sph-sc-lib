(pre-include-guard-begin sph-memreg-h)

(sc-comment
  "memreg registers memory in a local variable, for example to free all memory allocated at point."
  "the variables memreg_register and memreg_index will also be available."
  "usage:
   memreg_init(2);
   memreg_add(&variable-1);
   memreg_add(&variable-2);
   memreg_free;")

(pre-define
  (memreg-init register-size)
  (begin
    (declare memreg-register (array void* (register-size)) memreg-index (unsigned int))
    (set memreg-index 0))
  (memreg-add address)
  (begin
    "add a pointer to the register. memreg_init must have been called
     with sufficient size for all pointers to be added"
    (set (array-get memreg-register memreg-index) address)
    (set+ memreg-index 1))
  memreg-free
  (while memreg-index
    (sc-comment "free all currently registered pointers")
    (set- memreg-index 1)
    (free (array-get memreg-register memreg-index))))

(sc-comment
  "the *_named variant of memreg supports multiple concurrent registers identified by name"
  "usage:
   memreg_init_named(testname, 4);
   memreg_add_named(testname, &variable);
   memreg_free_named(testname);")

(pre-define
  (memreg-init-named register-id register-size)
  (begin
    (declare
      (pre-concat memreg-register _ register-id) (array void* (register-size))
      (pre-concat memreg-index _ register-id) (unsigned int))
    (set (pre-concat memreg-index _ register-id) 0))
  (memreg-add-named register-id address)
  (begin
    "does not protect against buffer overflow"
    (set
      (array-get (pre-concat memreg-register _ register-id) (pre-concat memreg-index _ register-id))
      address)
    (set+ (pre-concat memreg-index _ register-id) 1))
  (memreg-free-named register-id)
  (while (pre-concat memreg-index _ register-id)
    (set- (pre-concat memreg-index _ register-id) 1)
    (free
      (array-get (pre-concat memreg-register _ register-id) (pre-concat memreg-index _ register-id)))))

(sc-comment
  "memreg2 is like memreg but additionally supports specifying a handler function {void* -> void} with each address that will be used to free the data")

(pre-define
  (memreg2-init-named register-id register-size)
  (begin
    (declare
      (pre-concat memreg2-register _ register-id) (array memreg2-t (register-size))
      (pre-concat memreg2-index _ register-id) (unsigned int))
    (set (pre-concat memreg2-index _ register-id) 0))
  (memreg2-add-named register-id _address _handler)
  (begin
    (struct-set
      (array-get (pre-concat memreg2-register _ register-id)
        (pre-concat memreg2-index _ register-id))
      address _address
      handler (convert-type _handler (function-pointer void void*)))
    (set+ (pre-concat memreg2-index _ register-id) 1))
  (memreg2-free-named register-id)
  (while (pre-concat memreg2-index _ register-id)
    (set- (pre-concat memreg2-index _ register-id) 1)
    ( (struct-get
        (array-get (pre-concat memreg2-register _ register-id)
          (pre-concat memreg2-index _ register-id))
        handler)
      (struct-get
        (array-get (pre-concat memreg2-register _ register-id)
          (pre-concat memreg2-index _ register-id))
        address))))

(declare memreg2-t (type (struct (address void*) (handler (function-pointer void void*)))))
(pre-include-guard-end)