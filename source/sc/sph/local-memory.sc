(pre-define
  (local-memory-init register-size)
  (begin
    "register memory in a local variable to free all memory allocated at point"
    (declare
      sph-local-memory-register (array void* (register-size))
      sph-local-memory-index ui8)
    (set sph-local-memory-index 0))
  (local-memory-add address)
  (begin
    "do not try to add more entries than specified by register-size or a buffer overflow occurs"
    (set
      (array-get sph-local-memory-register sph-local-memory-index) address
      sph-local-memory-index (+ 1 sph-local-memory-index)))
  local-memory-free
  (while sph-local-memory-index
    (set sph-local-memory-index (- sph-local-memory-index 1))
    (free (pointer-get (+ sph-local-memory-register sph-local-memory-index)))))