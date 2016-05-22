;return status code and error handling.
;two basic return types are supported:
;* integer: integer return code. 32 bit signed for compatibility reasons
;* object: a status-t struct that also includes information about to which module an integer return status code belongs to
;a status id of 0 means success, everything else means failure.
;error ids and module ids can be managed with enumerated types and error descriptions added when necessary by additional routines.
(define-type status-t (struct (id b32_s) (module b8)))
(define-type status-i-t b32_s)
(define-macro status-init (define status status-t))
(define-macro status-ii-init (define status b32-t))

(define-macro (status-goto module id)
  ;integer integer -> object
  (set status.module module status.id id) (goto error))

(define-macro (status-return module id)
  ;integer integer -> object
  (return (convert-type (struct-literal module module id id) status-t)))

(define-macro (status-require expression cont)
  ;object -> object
  (set status expression) (if (not (zero? status.id)) cont))

(define-macro (status-require-goto expression) (status-require expression (goto status)))
(define-macro (status-require-return expression) (status-require expression (return status)))

(define-macro (status-io-require module expression cont) (set status.id expression)
  ;integer -> object
  (if (not (zero? status.id)) cont))

(define-macro (status-io-require-goto module expression)
  (status-io-require module expression (goto error)))

(define-macro (status-io-require-return module expression)
  (status-io-require module expression (return status)))

(define-macro (status-ii-require module expression cont)
  ;integer -> integer
  (set status expression) (if (not (zero? status)) cont))

(define-macro (status-ii-require-goto module expression)
  (status-ii-require module expression (goto error)))

(define-macro (status-ii-require-return module expression)
  (status-ii-require module expression (return status)))

(define-macro (status-success? a)
  (= 0 a.id))

(define-macro status-success 0)
