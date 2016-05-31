;a minimal integer linked list
(pre-include-once stdlib-h "stdlib.h" inttypes-h "inttypes.h")
(pre-if-not-defined mi-list-element-type-name (define-macro mi-list-element-type-name 64))
(pre-if-not-defined mi-list-element-t (define-type mi-list-element-t uint64_t))

(define-type mi-list-t
  (struct mi-list-struct (link (struct mi-list-struct*)) (data mi-list-element-t)))

(define-macro (mi-list-create) 0)
(define-macro (mi-list-first a) (if* (= 0 a) 0 (struct-deref a data)))
(define-macro (mi-list-first-address a) (if* (= 0 a) 0 (address-of (struct-deref a data))))
(define-macro (mi-list-rest a) (if* (= 0 a) 0 (struct-deref a link)))

(define (mi-list-drop a) (mi-list-t* mi-list-t*)
  (define a-next mi-list-t* (mi-list-rest a)) (free a) (return a-next))

(define (mi-list-destroy a) (void mi-list-t*)
  (define a-next mi-list-t* a)
  (while a-next (set a-next (struct-deref a link)) (free a) (set a a-next)))

(define (mi-list-add a value) (mi-list-t* mi-list-t* mi-list-element-t)
  (define element mi-list-t* (calloc 1 (sizeof mi-list-t)))
  (if (not element) (return 0))
  (set (struct-deref element data) value) (if a (set (struct-deref element link) a)) (return element))

(define (mi-list-length a) (size-t mi-list-t*)
  (define result size-t 0)
  (while a (set result (+ 1 result)) (set a (mi-list-rest a))) (return result))