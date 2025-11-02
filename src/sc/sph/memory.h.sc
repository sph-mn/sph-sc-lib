(pre-include-guard-begin sph-memory-h-included)
(pre-include "inttypes.h" "sph/array.h" "sph/memreg.h")

(pre-define
  sph-memory-status-id-memory 1
  sph-memory-status-group "sph"
  sph-memory-error (status-set-goto sph-memory-status-group sph-memory-status-id-memory)
  sph-memory-growth-factor 2
  (sph-memory-init a) (set a.data 0)
  (sph-memory-add-directly a address handler)
  (begin (struct-set (sph-array-first-unused *a) address address handler handler) (set+ a:used 1))
  (sph-malloc size result) (begin (sph-memory-malloc size (convert-type result void**)))
  (sph-malloc-string size result) (sph-memory-malloc-string size result)
  (sph-calloc size result) (sph-memory-calloc size (convert-type result void**))
  (sph-realloc size result) (sph-memory-realloc size (convert-type result void**)))

(sc-no-semicolon (sph-array-declare-type sph-memory memreg2-t))

(declare
  (sph-memory-status-description a) (char* status-t)
  (sph-memory-status-name a) (char* status-t)
  (sph-memory-malloc size result) (status-t size-t void**)
  (sph-memory-malloc-string length result) (status-t size-t char**)
  (sph-memory-calloc size result) (status-t size-t void**)
  (sph-memory-realloc size memory) (status-t size-t void**)
  (sph-memory-add-with-handler a address handler)
  (status-t sph-memory-t* void* (function-pointer void void*))
  (sph-memory-destroy a) (void sph-memory-t*))

(pre-include-guard-end)
