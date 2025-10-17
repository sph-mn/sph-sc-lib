(pre-include-guard-begin sph-memory)
(pre-include "stdlib.h" "stdio.h" "sph/memory.h")

(define (sph-status-description a) (uint8-t* status-t)
  (declare b uint8-t*)
  (case = a.id
    (sph-status-id-memory (set b "not enough memory or other memory allocation error"))
    (else (set b ""))))

(define (sph-status-name a) (uint8-t* status-t)
  (declare b uint8-t*)
  (case = a.id (sph-status-id-memory (set b "memory")) (else (set b "unknown"))))

(define (sph-primitive-malloc size result) (status-t size-t void**)
  "allocation helpers use status-t and have the same interface"
  status-declare
  (declare a void*)
  (set a (malloc size))
  (if a (set *result a) sph-memory-error)
  (label exit status-return))

(define (sph-primitive-malloc-string length result) (status-t size-t uint8-t**)
  "like sph-malloc but allocates one extra byte that is set to zero"
  status-declare
  (declare a uint8-t*)
  (status-require (sph-malloc (+ 1 length) &a))
  (set (array-get a length) 0 *result a)
  (label exit status-return))

(define (sph-primitive-calloc size result) (status-t size-t void**)
  status-declare
  (declare a void*)
  (set a (calloc size 1))
  (if a (set *result a) sph-memory-error)
  (label exit status-return))

(define (sph-primitive-realloc size memory) (status-t size-t void**)
  status-declare
  (declare a void*)
  (set a (realloc *memory size))
  (if a (set *memory a) sph-memory-error)
  status-return)

(define (sph-memory-add-with-handler a address handler)
  (status-t sph-memory-t* void* (function-pointer void void*))
  "event memory addition with automatic array expansion"
  status-declare
  (status-require (sph-memory-ensure a sph-memory-initial-size))
  (sph-memory-add-directly a address handler)
  (label exit status-return))

(define (sph-memory-free a) (void sph-memory-t*)
  "free all registered memory and unitialize the event-memory register"
  (if (not a:data) return)
  (declare m memreg2-t)
  (for-each-index i size-t a:size (set m (array3-get *a i)) (m.handler m.address))
  (array3-free a)
  (set a:data 0))

(pre-include-guard-end)
