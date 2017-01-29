(pre-define (scm-c-error-create id group data)
  (scm-call-3 scm-error-create (scm-from-uint id)
    (if* group (scm-from-uint group) SCM-BOOL-F) (if* data data SCM-EOL)))

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

(pre-define (scm-error-return-1 a) (if (scm-is-true (scm-call-1 scm-error? a)) (return a)))
(pre-define (scm-error-return-2 a b) (scm-error-return-1 a) (scm-error-return-1 b))
(pre-define (scm-error-return-3 a b c) (scm-error-return-2 a b) (scm-error-return-1 c))
(pre-define (scm-error-return-4 a b c d) (scm-error-return-2 a b) (scm-error-return-2 c d))
