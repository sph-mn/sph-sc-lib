; return status code and error handling. uses a local variable named "status" and a goto label named "exit".
; a status has an identifier and a group to discern between status identifiers of different libraries.
; status id 0 is success, everything else can be considered a failure or at least a special case.
; status ids are 32 bit signed integers for compatibility with error return codes from some other libraries.
; there are two types of bindings:
; * bindings with a ! suffix set the group and status id in the status object
; * bindings without a ! suffix set only the group in the status object
; status id is set -> status id is checked -> on failure, group id is set
(define-type status-i-t b32_s)
(define-type status-t (struct (id status-i-t) (group b8)))

(pre-define status-id-success 0
  status-group-undefined 0
  status-init (define status status-t (struct-literal status-id-success status-group-undefined)))

(pre-define status-success? (equal? status-id-success (struct-get status id)))
(pre-define status-failure? (not status-success?))
(pre-define status-goto (goto exit))
(pre-define (status-set-group group-id) (struct-set status group group-id))
(pre-define (status-set-id status-id) (struct-set status id status-id))

(pre-define (status-set-both group-id status-id) (status-set-group group-id)
  (status-set-id status-id))

(pre-define status-require (if status-failure? status-goto))

(pre-define (status-require! expression) (set status expression)
  (if status-failure? status-goto))

(pre-define (status-set-id-goto status-id) (status-set-id status-id) status-goto)
(pre-define (status-set-group-goto group-id) (status-set-group group-id) status-goto)

(pre-define (status-set-both-goto group-id status-id) (status-set-both group-id status-id)
  status-goto)

(pre-define (status-id-is? status-id)
  (equal? status-id (struct-get status id)))
