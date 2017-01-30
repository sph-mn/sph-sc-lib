;following are helpers for using the local-memory pattern. it creates an allocated-heap-memory registry in local variables with a more automated free so that
;different routine end points, like after error occurences, can easily free all memory up to point

(pre-define (local-define-malloc variable-name type on-error)
  (define variable-name type* (malloc (sizeof type))) (if (not variable-name) on-error))

(pre-define (local-memory-init max-address-count)
  (define sph-local-memory-addresses[max-address-count] b0*) (define sph-local-memory-index b8 0))

(pre-define (local-memory-add pointer)
  ;do not add more entries as given by max-address-count or it leads to a buffer overflow
  (set (deref sph-local-memory-addresses sph-local-memory-index) pointer
    sph-local-memory-index (+ 1 sph-local-memory-index)))

(pre-define local-memory-free
  (while sph-local-memory-index (decrement-one sph-local-memory-index)
    (free (deref (+ sph-local-memory-addresses sph-local-memory-index)))))
