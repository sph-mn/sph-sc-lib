;similar to bindings of "status.sc" but only uses a status integer and no special type. depends on "status.sc"
(pre-define status-ii-init (define status-ii status-i-t status-success))
(pre-define (status-ii-success? a) (= status-success a))
(pre-define (status-ii-failure? a) (not (status-ii-success? a)))
(pre-define (status-ii-require a cont) (if (not (= status-success a)) cont))

(pre-define (status-ii-require! expression cont) (set status-ii expression)
  (status-ii-require status-ii cont))

(pre-define (status-ii-require-goto a) (status-ii-require a (goto exit)))
(pre-define (status-ii-require-goto! expression) (status-ii-require! expression (goto exit)))
(pre-define (status-ii-require-return a) (status-ii-require a (return status)))
(pre-define (status-ii-require-return! expression) (status-ii-require! expression (return status)))
