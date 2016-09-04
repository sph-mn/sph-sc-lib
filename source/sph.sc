(pre-include-once inttypes-h "inttypes.h" stdio-h "stdio.h")
;short fixed length type names. as described on http://sph.mn/content/2a63

(pre-define boolean b8
  pointer-t uintptr_t
  b0 void
  b8 uint8_t
  b16 uint16_t
  b32 uint32_t
  b64 uint64_t b8_s int8_t b16_s int16_t b32_s int32_t b64_s int64_t f32_s float f64_s double)

(pre-if debug-log?
  ;prints arguments with pattern like printf, prepends current function name and line number and automatically adds a newline.
  ;example: (debug-log "%d" 1)
  ;all occurences of "debug-log" can be disabled by setting the preprocessor variable "debug-log?" to 0 before including this file
  (pre-define (debug-log format ...)
    (fprintf stderr (pre-string-concat "%s:%d " format "\n") __func__ __LINE__ __VA_ARGS__))
  (pre-define (debug-log format ...) null))

;typical definition of null as seen in other libraries
(pre-define null (convert-type 0 b0))
;giving meaning to obscurely named c keywords
(pre-define _readonly const)
(pre-define _noalias restrict)
(pre-define (increment-one a) (set a (+ 1 a)))
(pre-define (decrement-one a) (set a (- a 1)))
(pre-define (zero? a) (= 0 a))
;following are helpers for using the local-memory pattern. it creates an allocated-heap-memory registry in local variables with a more automated free so that
;different routine end points, like after error occurences, can easily free all memory up to point

(pre-define (local-memory-init max-address-count)
  (define sph-local-memory-addresses[max-address-count] b0*) (define sph-local-memory-index b8 0))

(pre-define (local-memory-add pointer)
  ;do not add more entries as given by max-address-count or it leads to a buffer overflow
  (set (deref sph-local-memory-addresses sph-local-memory-index) pointer
    sph-local-memory-index (+ 1 sph-local-memory-index)))

(pre-define local-memory-free
  (while sph-local-memory-index (decrement-one sph-local-memory-index)
    (free (deref (+ sph-local-memory-addresses sph-local-memory-index)))))
