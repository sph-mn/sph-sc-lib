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
  (set status.module module status.id id) (goto exit))

(define-macro (status-return module id)
  ;integer integer -> object
  (return (convert-type (struct-literal module module id id) status-t)))

(define-macro (status-require-check status cont)
  ;object -> object
  (if (not (status-success? status.id)) cont))

(define-macro (status-require-check-goto status)
  ;object -> object
  (status-require-check status (goto exit)))

(define-macro (status-require-check-return status)
  ;object -> object
  (status-require-check status (return status)))

(define-macro (status-require expression cont)
  ;object -> object
  (set status expression) (status-require-check expression cont))

(define-macro (status-require-goto expression) (status-require expression (goto status)))
(define-macro (status-require-return expression) (status-require expression (return status)))

(define-macro (status-io-require-check module status cont)
  ;integer -> object
  (if (not (status-success? status.id)) (begin (set status.module module) cont)))

(define-macro (status-io-require-check-goto module status)
  (status-io-require-check module status (goto exit)))

(define-macro (status-io-require-check-return module status)
  (status-io-require-check module status (return status)))

(define-macro (status-io-require module expression cont)
  ;integer -> object
  (set status.id expression) (status-io-require-check module status cont))

(define-macro (status-io-require-goto module expression)
  (status-io-require module expression (goto exit)))

(define-macro (status-io-require-return module expression)
  (status-io-require module expression (return status)))

(define-macro (status-ii-require-check status cont) (if (not (status-success? status)) cont))

(define-macro (status-ii-require expression cont)
  ;integer -> integer
  (set status expression) (status-ii-require-check status cont))

(define-macro (status-ii-require-goto expression) (status-ii-require expression (goto exit)))
(define-macro (status-ii-require-return expression) (status-ii-require expression (return status)))
(define-macro (status-success? a) (= 0 a.id))
(define-macro status-success 0)
