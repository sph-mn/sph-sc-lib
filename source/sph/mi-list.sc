;a minimal integer linked list
(pre-include "stdlib.h" "inttypes.h")
(pre-if-not-defined mi-list-element-t (define-type mi-list-element-t uint64_t))

(define-type mi-list-t
  (struct mi-list-struct (link (struct mi-list-struct*)) (data mi-list-element-t)))

(define-macro (mi-list-create) (convert-type 0 mi-list-t*))
(define-macro (mi-list-first a) (if* (= 0 a) 0 (struct-deref a data)))
(define-macro (mi-list-rest a) (if* (= 0 a) 0 (struct-deref a link)))

(define (mi-list-drop a) (mi-list-t* mi-list-t*)
  (define a-next mi-list-t* (mi-list-rest a)) (free a) (return a-next))

(define (mi-list-destroy a) (void mi-list-t*)
  (define a-next mi-list-t* a)
  (while a-next (set a-next (struct-deref a link)) (free a) (set a a-next)))

(define (mi-list-add a value) (mi-list-t* mi-list-t* mi-list-element-t)
  (define element mi-list-t* (malloc (sizeof mi-list-t))) (if (not element) (return 0))
  (set (struct-deref element data) value) (if a (set (struct-deref element link) a)) (return element))
