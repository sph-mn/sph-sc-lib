(pre-include-guard-begin sph-memory-c-included)
(pre-include "stdlib.h" "stdio.h" "sph/memory.h")

(define (sph-memory-status-description a) (char* status-t)
  (case = a.id
    (sph-memory-status-id-memory (return "not enough memory or other memory allocation error"))
    (else (return ""))))

(define (sph-memory-status-name a) (char* status-t)
  (case = a.id (sph-memory-status-id-memory (return "memory")) (else (return "unknown"))))

(define (sph-memory-malloc size result) (status-t size-t void**)
  status-declare
  (declare a void*)
  (set a (malloc size))
  (if a (set *result a) sph-memory-error)
  (label exit status-return))

(define (sph-memory-malloc-string length result) (status-t size-t char**)
  "like sph_malloc but allocates one extra byte that is set to zero"
  status-declare
  (declare a char*)
  (status-require (sph-malloc (+ 1 length) &a))
  (set (array-get a length) 0 *result a)
  (label exit status-return))

(define (sph-memory-calloc size result) (status-t size-t void**)
  status-declare
  (declare a void*)
  (set a (calloc size 1))
  (if a (set *result a) sph-memory-error)
  (label exit status-return))

(define (sph-memory-realloc size memory) (status-t size-t void**)
  status-declare
  (define a void* (realloc *memory size))
  (if a (set *memory a) sph-memory-error)
  (label exit status-return))

(define (sph-memory-add-with-handler a address handler)
  (status-t sph-memory-t* void* (function-pointer void void*))
  "event memory addition with automatic array expansion"
  status-declare
  (status-require (sph-memory-ensure a 4))
  (sph-memory-add-directly a address handler)
  (label exit status-return))

(define (sph-memory-destroy a) (void sph-memory-t*)
  "free all registered memory and unitialize the event-memory register"
  (if (not a:data) return)
  (declare m memreg2-t)
  (for-each-index i size-t a:used (set m (sph-array-get *a i)) (m.handler m.address))
  (sph-memory-free a)
  (set a:data 0))

(pre-include-guard-end)
