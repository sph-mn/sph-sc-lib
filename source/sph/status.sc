; return status code and error handling. uses a local variable named "status" and a goto label named "exit".
; a status has an identifier and a group to discern between status identifiers of different libraries.
; status id 0 is success, everything else can be considered a failure or at least a special case.
; status ids are 32 bit signed integers for compatibility with error return codes from some other libraries.
; there are two types of bindings:
; * bindings with a ! suffix set the group and status id in the status object
; * bindings without a ! suffix set only the group in the status object
(define-type status-i-t b32_s)
(define-type status-t (struct (id status-i-t) (group b8)))

(pre-define status-id-success 0
  status-group-undefined 0
  status-init (define status status-t (struct-literal status-id-success status-group-undefined)))

(pre-define (status-success? a) (equal? status-id-success (struct-get a id)))
(pre-define (status-failure? a) (not (status-success? a)))
(pre-define (status-do group-id cont) (struct-set status group group-id) cont)
(pre-define (status-goto group-id id) (status-do group-id (goto exit)))
(pre-define (status-return group-id id) (status-do group-id (return status)))

(pre-define (status-require-do group-id status cont)
  (if (status-failure? status) (status-do group-id cont)))

(pre-define (status-require group-id status) (status-require-do group-id status (goto exit)))

(pre-define (status-require-return group-id status)
  (status-require-do group-id status (return status)))

(pre-define (status-do! group-id status-id cont) (struct-set status group group-id id status-id)
  cont)

(pre-define (status-goto! group-id id) (status-do! group-id id (goto exit)))
(pre-define (status-return! group-id id) (status-do! group-id id (return status)))

(pre-define (status-require-do! group-id expression cont) (set status expression)
  (status-require-do group-id status cont))

(pre-define (status-require! group-id expression)
  (status-require-do! group-id expression (goto exit)))

(pre-define (status-require-return! group-id expression)
  (status-require-do! group-id expression (return status)))
