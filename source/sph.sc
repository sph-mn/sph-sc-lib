(pre-include "inttypes.h")

;short fixed length type names. as described on http://sph.mn/content/2a63
(define-macro pointer uintptr-t
  b0 void
  b8 uint8_t
  b16 uint16_t
  b32 uint32_t
  b64 uint64_t b8_s int8_t b16_s int16_t b32_s int32_t b64_s int64_t f32-s float f64-s double)

(pre-if debug-log?
  ;prints arguments with pattern like printf, prepends current function name and line number and automatically adds a newline.
  ;example: (debug-log "%d" 1)
  ;all occurences of "debug-log" can be disabled by setting the preprocessor variable "debug-log?" to 0 before including this file
  (define-macro (debug-log format ...)
    (fprintf stderr (pre-string-concat "%s:%d " format "\n") __func__ __LINE__ __VA_ARGS__))
  (define-macro (debug-log format ...) null))

;typical definition of null as seen in other libraries
(define-macro null (convert-type 0 b0))
;giving meaning to obscurely named c keywords
(define-macro _readonly const)
(define-macro _noalias restrict)
(define-macro (increment-one a) (set a (+ 1 a)))
(define-macro (decrement-one a) (set a (- a 1)))
;following are helpers for using the local-memory pattern. it creates an allocated-heap-memory registry in local variables with a more automated free so that
;different routine end points, like after error occurences, can easily free all memory up to point

(define-macro (local-memory-init max-address-count)
  (define sph-local-memory-addresses[max-address-count] b0*) (define sph-local-memory-index b8 0))

(define-macro (local-memory-add pointer)
  ;do not add more entries as given by max-address-count or it leads to a buffer overflow
  (set (deref sph-local-memory-addresses sph-local-memory-index) pointer
    sph-local-memory-index (+ 1 sph-local-memory-index)))

(define-macro local-memory-free
  (while sph-local-memory-index (decrement-one sph-local-memory-index)
    (free (deref (+ sph-local-memory-addresses sph-local-memory-index)))))

;local-error is a way to save error information at places and go to a single cleanup goto label, "error", to process the error information.
;it is basically for using one goto label for errors instead of multiple goto labels
(define-macro local-error-init (define local-error-number b32-s local-error-module b8*))

(define-macro (local-error module-identifier error-identifier)
  ;saves error information and jumps to the label "error".
  ;local-error-module, is used to identify the package/library/module that the error-identifier/error-code belongs to.
  ;for example "glibc", or any other custom name. it can be set to zero
  (set local-error-module module-identifier) (set local-error-number error-identifier) (goto error))
