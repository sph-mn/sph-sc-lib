(sc-comment
  "memreg registers memory in a local variable to free all memory allocated at point"
  "usage:
     memreg_init(4);
     memreg_add(&variable-1);
     memreg_add(&variable-2);
     memreg_free();")

(pre-define
  (memreg-init register-size)
  (begin
    (declare
      sph-memreg-register (array void* (register-size))
      sph-memreg-index (unsigned int))
    (set sph-memreg-index 0))
  (memreg-add address)
  (begin
    "does not protect against buffer overflow"
    (set
      (array-get sph-memreg-register sph-memreg-index) address
      sph-memreg-index (+ 1 sph-memreg-index)))
  memreg-free
  (while sph-memreg-index
    (set sph-memreg-index (- sph-memreg-index 1))
    (free (pointer-get (+ sph-memreg-register sph-memreg-index)))))

(sc-comment
  "the *-named variant of memreg supports multiple concurrent registers identified by name"
  "usage:
     memreg_init_named(testname, 4);
     memreg_add_named(testname, &variable);
     memreg_free_named(testname);")

(pre-define
  (memreg-init-named register-id register-size)
  (begin
    (declare
      (pre-concat sph-memreg-register _ register-id) (array void* (register-size))
      (pre-concat sph-memreg-index _ register-id) (unsigned int))
    (set (pre-concat sph-memreg-index _ register-id) 0))
  (memreg-add-named register-id address)
  (begin
    "does not protect against buffer overflow"
    (set
      (array-get
        (pre-concat sph-memreg-register _ register-id) (pre-concat sph-memreg-index _ register-id))
      address (pre-concat sph-memreg-index _ register-id)
      (+ 1 (pre-concat sph-memreg-index _ register-id))))
  (memreg-free-named register-id)
  (while (pre-concat sph-memreg-index _ register-id)
    (set (pre-concat sph-memreg-index _ register-id)
      (- (pre-concat sph-memreg-index _ register-id) 1))
    (free
      (pointer-get
        (+
          (pre-concat sph-memreg-register _ register-id) (pre-concat sph-memreg-index _ register-id))))))