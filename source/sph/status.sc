;return status code and error handling.
;two basic return types are supported:
;* integer: integer return code. 32 bit signed for compatibility reasons
;* object: a status-t struct that also includes information about to which module an integer return status code belongs to
;a status id of 0 means success, everything else means failure.
;error ids and module ids can be managed with enumerated types and error descriptions added when necessary by additional routines.
(define-type status-t (struct (id b32_s) (module b8)))
(define-type status-i-t b32_s)
(pre-define status-init (define status status-t (struct-literal 0 0)))
(pre-define status-ii-init (define status b32-t))

(pre-define (status-io-goto status-module status-id)
  ;integer integer -> object
  (set status.module status-module status.id status-id) (goto exit))

(pre-define (status-io-return module id)
  ;integer integer -> object
  (return (convert-type (struct-literal module module id id) status-t)))

(pre-define (status-require-check status cont)
  ;object -> object
  (if (not (status-success? status)) cont))

(pre-define (status-require-check-goto status)
  ;object -> object
  (status-require-check status (goto exit)))

(pre-define (status-require-check-return status)
  ;object -> object
  (status-require-check status (return status)))

(pre-define (status-require expression cont)
  ;object -> object
  (set status expression) (status-require-check status cont))

(pre-define (status-require-goto expression) (status-require expression (goto exit)))
(pre-define (status-require-return expression) (status-require expression (return status)))

(pre-define (status-io-require-check status-module status cont)
  ;integer -> object
  (if (not (status-success? status)) (begin (set status.module status-module) cont)))

(pre-define (status-io-require-check-goto module status)
  (status-io-require-check module status (goto exit)))

(pre-define (status-io-require-check-return module status)
  (status-io-require-check module status (return status)))

(pre-define (status-io-require module expression cont)
  ;integer -> object
  (set status.id expression) (status-io-require-check module status cont))

(pre-define (status-io-require-goto module expression)
  (status-io-require module expression (goto exit)))

(pre-define (status-io-require-return module expression)
  (status-io-require module expression (return status)))

(pre-define (status-ii-require-check status cont) (if (not (status-success? status)) cont))

(pre-define (status-ii-require expression cont)
  ;integer -> integer
  (set status expression) (status-ii-require-check status cont))

(pre-define (status-ii-require-goto expression) (status-ii-require expression (goto exit)))
(pre-define (status-ii-require-return expression) (status-ii-require expression (return status)))
(pre-define (status-success? a) (= 0 a.id))
;todo: should status? bindings use implied local status variable?
(pre-define (status-failure? a) (not (status-success? a)))
(pre-define status-success 0)
(pre-define (status-from-boolean a) (not a))
