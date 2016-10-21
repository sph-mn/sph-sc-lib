(pre-if stability-typechecks
  (pre-define (if-typecheck expr action)
    (if (not expr)
      (begin
        (debug-log "type check failed %s"
          (if* (< (string-length (pre-stringify expr)) 24) (pre-stringify expr) ""))
        action)))
  (pre-define (if-typecheck expr action) null))

(pre-define (octet-write-string-binary target a)
  (sprintf target "%d%d%d%d%d%d%d%d"
    (if* (bit-and a 128) 1 0) (if* (bit-and a 64) 1 0)
    (if* (bit-and a 32) 1 0) (if* (bit-and a 16) 1 0)
    (if* (bit-and a 8) 1 0) (if* (bit-and a 4) 1 0) (if* (bit-and a 2) 1 0) (if* (bit-and a 1) 1 0)))

(enum (sph-error-number-memory sph-error-number-input))

(define (error-description n) (char* b32-s)
  (return
    (cond* ((= sph-error-number-memory n) "memory") ((= sph-error-number-input n) "input")
      (else "unknown"))))

(pre-define (require-goto a label) (if (not a) (goto label)))

(pre-define (array-contains? array-start array-end search-value index-temp result)
  (set index-temp array-start) (set result #f)
  (while (<= index-temp array-end)
    (if (= (deref index-temp) search-value) (begin (set result #t) break)) (increment-one index-temp)))
