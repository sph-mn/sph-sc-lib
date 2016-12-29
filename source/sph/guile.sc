(pre-define scm-first SCM_CAR scm-tail SCM_CDR)
(pre-define scm-c-define-procedure-c-init (define scm-c-define-procedure-c-temp SCM))

(pre-define (scm-c-define-procedure-c name required optional rest c-function documentation)
  ;like scm-c-define-gsubr but also sets a documentation string.
  ;defines and registers a c routine as a scheme procedure
  (set scm-c-define-procedure-c-temp (scm-c-define-gsubr name required optional rest c-function))
  (scm-set-procedure-property! scm-c-define-procedure-c-temp
    (scm-from-locale-symbol "documentation") (scm-from-locale-string documentation)))

(pre-define (scm-c-list-each list e body)
  ;SCM SCM c-compound-expression ->
  (while (not (scm-is-null list)) (set e (scm-first list)) body (set list (scm-tail list))))

(pre-define (scm-is-undefined a) (= SCM-UNDEFINED a))
(pre-define (scm-false-if-undefined a) (if* (scm-is-undefined a) SCM-BOOL-F a))
(pre-define (null-if-undefined a) (if* (scm-is-undefined a) 0 a))
(pre-define (scm-if-undefined-expr a b c) (if* (scm-is-undefined a) b c))
(pre-define (scm-if-undefined a b c) (if (scm-is-undefined a) b c))

(pre-define (scm-is-list-false-or-undefined a)
  (or (scm-is-true (scm-list? a)) (= SCM-BOOL-F a) (= SCM-UNDEFINED a)))

(pre-define (scm-is-integer-false-or-undefined a)
  (or (scm-is-integer a) (= SCM-BOOL-F a) (= SCM-UNDEFINED a)))

(define (scm-c-bytevector-take size-octets a) (SCM size-t b8*)
  ;creates a new bytevector of size-octects from a given bytevector
  (define r SCM (scm-c-make-bytevector size-octets))
  (memcpy (SCM-BYTEVECTOR-CONTENTS r) a size-octets) (return r))

(pre-define (scm-c-error-create id group data)
  (scm-call-3 scm-error-create (scm-from-uint id)
    (if* group (scm-from-uint group) SCM-BOOL-F) (if* data data SCM-EOL)))

(define (scm-debug-log value) (b0 SCM)
  (scm-call-2 (scm-variable-ref (scm-c-lookup "write")) value (scm-current-output-port))
  (scm-newline (scm-current-output-port)))

(define scm-error-create SCM scm-error? SCM scm-error-group SCM scm-error-id SCM scm-error-data SCM)

(define (scm-error-init) b0
  ;the features defined in this file need run-time initialisation. call this once in an application before using the features here
  (define m SCM (scm-c-resolve-module "sph error"))
  (set scm-error-create (scm-variable-ref (scm-c-module-lookup m "error-create-p")))
  (set scm-error-group (scm-variable-ref (scm-c-module-lookup m "error-group")))
  (set scm-error-id (scm-variable-ref (scm-c-module-lookup m "error-id")))
  (set scm-error-data (scm-variable-ref (scm-c-module-lookup m "error-data")))
  (set scm-error? (scm-variable-ref (scm-c-module-lookup m "error?"))))

(pre-define scm-c-local-error-init
  (define local-error-origin SCM local-error-name SCM local-error-data SCM))

(pre-define (scm-c-local-error i d)
  (set local-error-origin (scm-from-locale-symbol __func__)
    local-error-name (scm-from-locale-symbol i) local-error-data d)
  (goto error))

(pre-define scm-c-local-error-create
  (scm-call-3 scm-error-create local-error-origin
    local-error-name (if* local-error-data local-error-data SCM-BOOL-F)))

(pre-define (scm-c-local-define-malloc variable-name type)
  (define variable-name type* (malloc (sizeof type)))
  (if (not variable-name) (scm-c-local-error "memory" 0)))

(pre-define (scm-c-local-define-malloc+size variable-name type size)
  (define variable-name type* (malloc size)) (if (not variable-name) (scm-c-local-error "memory" 0)))

(pre-define scm-c-local-error-return (return scm-c-local-error-create))

(pre-if local-error-assert-enable
  (pre-define (scm-c-local-error-assert name expr) (if (not expr) (scm-c-local-error name 0)))
  (pre-define (scm-c-local-error-assert name expr) null))

(pre-define (scm-c-local-error-glibc error-number)
  (scm-c-local-error "glibc" (scm-from-locale-string (strerror error-number))))

(pre-define scm-c-local-error-system
  (scm-c-local-error "system" (scm-from-locale-string (strerror errno))))

(pre-define (scm-c-require-success-glibc a) (set s a) (if (< s 0) (scm-c-local-error-glibc s)))
(pre-define (scm-c-require-success-system a) (if (< a 0) scm-c-local-error-system))
(enum (sph-guile-status-id-wrong-argument-type))
(pre-define sph-guile-status-group-sph-guile 4)

(define (sph-guile-status-text status) (b8* status-t)
  (return
    (case* = (struct-get status group)
      (sph-guile-status-group-sph-guile
        (string-append "sph-guile: "
          (convert-type
            (case* = status.id
              (sph-guile-status-id-wrong-argument-type "wrong type for argument")
              (else "error without description"))
            b8*)))
      (else (convert-type "" b8*)))))

(pre-if scm-enable-typechecks?
  (pre-define (scm-typecheck expr)
    (if (not expr)
      (begin
        (set status.id sph-guile-status-id-wrong-argument-type
          (struct-get status group) sph-guile-status-group-sph-guile)
        (goto exit))))
  (pre-define (scm-typecheck expr) null))

(pre-define (scm-error-return-1 a) (if (scm-is-true (scm-call-1 scm-error? a)) (return a)))
(pre-define (scm-error-return-2 a b) (scm-error-return-1 a) (scm-error-return-1 b))
(pre-define (scm-error-return-3 a b c) (scm-error-return-2 a b) (scm-error-return-1 c))
(pre-define (scm-error-return-4 a b c d) (scm-error-return-2 a b) (scm-error-return-2 c d))
