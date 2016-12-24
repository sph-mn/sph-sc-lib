;return status code and error handling with a status type.
; a status conists of a status id and a status group id to discern between ids from different libraries.
; status id 0 is success, everything else can be considered a failure.
; status ids are 32 bit signed integers for compatibility with common error return codes
(define-type status-i-t b32_s)
(define-type status-t (struct (id status-i-t) (group b8)))

(pre-define status-id-success 0
  status-init (define status status-t (struct-literal status-id-success status-id-success)))

(pre-define (status-success? a) (= status-id-success (struct-get a id)))
(pre-define (status-failure? a) (not (status-success? a)))
(pre-define (status-from-boolean a) (not a))

(pre-define (status-do! group-id status-id cont) (struct-set status group group-id id status-id)
  cont)

(pre-define (status-goto! group id) (status-do! group id (goto exit)))
(pre-define (status-return! group id) (status-do! group id (return status)))

(pre-define (status-require-do group-id status cont)
  (if (not (status-success? status)) (begin (struct-set status group group-id) cont)))

(pre-define (status-require group status) (status-require-do group status (goto exit)))
(pre-define (status-require-return group status) (status-require-do group status (return status)))

(pre-define (status-require-do! group expression cont) (set status expression)
  (status-require-do group status cont))

(pre-define (status-require! group expression) (status-require-do! group expression (goto exit)))

(pre-define (status-require-return! group expression)
  (status-require-do! group expression (return status)))
