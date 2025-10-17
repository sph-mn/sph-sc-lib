(pre-include-guard-begin sph-memory-h)
(pre-include "inttypes.h" "sph/status.h" "sph/memreg.h")

(pre-define
  sph-status-id-memory 1
  sph-status-group (convert-type "sph" uint8-t*)
  sph-memory-error (status-set-goto sph-status-group sph-status-id-memory)
  (sph-malloc size result) (begin (sph-primitive-malloc size (convert-type result void**)))
  (sph-malloc-string size result) (sph-primitive-malloc-string size (convert-type result uint8-t**))
  (sph-calloc size result) (sph-primitive-calloc size (convert-type result void**))
  (sph-realloc size result) (sph-primitive-realloc size (convert-type result void**))
  sph-memory-growth-factor 2
  sph-memory-initial-size 4
  (sph-memory-add-directly a address handler)
  (begin (struct-set (array3-first-unused a) address address handler handler) (set+ a:used 1)))

(array3-declare-type sph-memory memreg2-t)

(declare
  (sph-status-description a) (uint8-t* status-t)
  (sph-status-name a) (uint8-t* status-t)
  (sph-primitive-malloc size result) (status-t size-t void**)
  (sph-primitive-malloc-string length result) (status-t size-t uint8-t**)
  (sph-primitive-calloc size result) (status-t size-t void**)
  (sph-primitive-realloc size memory) (status-t size-t void**)
  (sph-memory-add-with-handler a address handler)
  (status-t sph-memory-t* void* (function-pointer void void*))
  (sph-memory-free a) (void sph-memory-t*))

(pre-include-guard-end)
