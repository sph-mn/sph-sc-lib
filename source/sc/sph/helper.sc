(pre-include "stdlib.h" "inttypes.h" "./status.c")

(pre-define
  status-group-sph "sph"
  (sph-helper-malloc size result)
  (begin
    "add explicit type cast to prevent compiler warning"
    (sph-helper-primitive-malloc size (convert-type result void**)))
  (sph-helper-malloc-string size result)
  (sph-helper-primitive-malloc-string size (convert-type result uint8-t**))
  (sph-helper-calloc size result) (sph-helper-primitive-calloc size (convert-type result void**))
  (sph-helper-realloc size result) (sph-helper-primitive-realloc size (convert-type result void**)))

(enum (sph-status-id-memory))

(declare
  (sph-helper-primitive-malloc size result) (status-t size-t void**)
  (sph-helper-primitive-malloc-string length result) (status-t size-t uint8-t**)
  (sph-helper-primitive-calloc size result) (status-t size-t void**)
  (sph-helper-primitive-realloc size block) (status-t size-t void**)
  (sph-helper-uint->string a result-len) (uint8-t* uintmax-t size-t*)
  (sph-helper-display-bits-u8 a) (void uint8-t)
  (sph-helper-display-bits a size) (void void* size-t))