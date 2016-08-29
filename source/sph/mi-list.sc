;a minimal linked list with custom element types.
;this file can be included multiple times to create different type versions,
;depending the value of the preprocessor variables "mi-list-name-infix" and "mi-list-element-t" before inclusion
(pre-include-once stdlib-h "stdlib.h" inttypes-h "inttypes.h")
(pre-if-not-defined mi-list-name-infix (pre-define mi-list-name-infix 64))
(pre-if-not-defined mi-list-element-t (pre-define mi-list-element-t uint64_t))

(pre-if-not-defined mi-list-name-concat
  ;identifier concatenation is not more straightfoward in c in this case
  (begin (pre-define (mi-list-name-concat a b) (pre-concat mi-list- a _ b))
    (pre-define (mi-list-name-concatenator a b) (mi-list-name-concat a b))
    (pre-define (mi-list-name name) (mi-list-name-concatenator mi-list-name-infix name))))

(pre-define mi-list-struct-name (mi-list-name struct))
(pre-define mi-list-t (mi-list-name t))

(define-type mi-list-t
  (struct mi-list-struct-name (link (struct mi-list-struct-name*)) (data mi-list-element-t)))

(pre-if-not-defined mi-list-first
  (begin (pre-define (mi-list-first a) (struct-deref a data))
    (pre-define (mi-list-first-address a) (address-of (struct-deref a data)))
    (pre-define (mi-list-rest a) (struct-deref a link))))

(define ((mi-list-name drop) a) (mi-list-t* mi-list-t*)
  (define a-next mi-list-t* (mi-list-rest a)) (free a) (return a-next))

(define ((mi-list-name destroy) a) (void mi-list-t*)
  (define a-next mi-list-t* a)
  (while a-next (set a-next (struct-deref a link)) (free a) (set a a-next)))

(define ((mi-list-name add) a value) (mi-list-t* mi-list-t* mi-list-element-t)
  (define element mi-list-t* (calloc 1 (sizeof mi-list-t))) (if (not element) (return 0))
  (set (struct-deref element data) value) (if a (set (struct-deref element link) a)) (return element))

(define ((mi-list-name length) a) (size-t mi-list-t*)
  (define result size-t 0) (while a (set result (+ 1 result)) (set a (mi-list-rest a)))
  (return result))

(pre-undefine mi-list-name-infix mi-list-element-t mi-list-struct-name mi-list-t)
