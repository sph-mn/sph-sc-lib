(sc-comment
  "return status code and error handling. uses a local variable named \"status\" and a goto label named \"exit\".
   a status has an identifier and a group to discern between status identifiers of different libraries.
   status id 0 is success, everything else can be considered a failure or special case.
   status ids are 32 bit signed integers for compatibility with error return codes from many other existing libraries.
   bindings with a ! suffix update the status from an expression")

(pre-define (status-init-group group)
  (begin
    "like status init but sets a default group"
    (define status status-t (struct-literal status-id-success group)))
  status-id-success
  0
  status-group-undefined
  0
  status-init
  (define status status-t (struct-literal status-id-success status-group-undefined))
  status-reset
  (status-set-both status-group-undefined status-id-success)
  status-success?
  (equal? status-id-success (struct-get status id))
  status-failure?
  (not status-success?)
  status-goto
  (goto exit)
  status-require
  (if status-failure? status-goto)
  (status-set-group group-id)
  (struct-set status group group-id)
  (status-set-id status-id)
  (struct-set status id status-id)
  (status-set-both group-id status-id)
  (begin
    (status-set-group group-id)
    (status-set-id status-id))
  (status-require! expression)
  (begin
    "update status with the result of expression, check for failure and goto error if so"
    (set status expression)
    (if status-failure? status-goto))
  (status-set-id-goto status-id)
  (begin
    "set the status id and goto error"
    (status-set-id status-id)
    status-goto)
  (status-set-group-goto group-id)
  (begin
    (status-set-group group-id)
    status-goto)
  (status-set-both-goto group-id status-id)
  (begin
    (status-set-both group-id status-id)
    status-goto)
  (status-id-is? status-id)
  (equal? status-id (struct-get status id))
  (status-i-require! expression)
  (begin
    "update status with the result of expression, check for failure and goto error if so"
    (set status.id expression)
    (if status-failure? status-goto)))

(declare
  status-i-t (type b32_s) status-t
  (type
    (struct
      (id status-i-t)
      (group b8))))
