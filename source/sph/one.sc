;this library contains various more experimental bindings

(pre-include-once stdlib-h "stdlib.h"
  errno-h "errno.h" sys-stat-h "sys/stat.h" libgen-h "libgen.h" string-h "string.h" unistd-h "unistd.h")

(define-macro string-length strlen
  string-length-n strnlen
  string-copy strcpy
  string-copy-n strncpy
  string-concat strcat
  string-concat-n strncat
  string-index strchr
  string-index-right strrchr
  string-index-string strstr
  string-duplicate strdup
  string-duplicate-n strndup
  string-index-ci strcasestr
  string-span strcspn
  string-break strpbrk string-compare strcmp memory-copy memcpy memory-compare memcmp)

(define-macro (file-exists? path) (not (equal? (access path F-OK) -1)))

(define (ensure-trailing-slash str) (char* char*)
  (define str-len b8 (string-length str))
  (if (or (not str-len) (equal? #\/ (deref (+ str (- str-len 1))))) (return str)
    (begin (define new-str char* (malloc (+ 2 str-len))) (if (not new-str) (return 0))
      (memory-copy new-str str str-len) (memory-copy (+ new-str str-len) "/" 1)
      (set (deref new-str (+ 1 str-len)) 0) (return new-str))))

(define (string-clone a) (b8* b8*)
  (define a-size size-t (+ 1 (string-length a))) (define result b8* (malloc a-size))
  (if result (memory-copy result a a-size)) (return result))

(define (string-append a b) (b8* b8* b8*)
  (define a-length size-t (string-length a)) (define b-length size-t (string-length b))
  (define result b8* (malloc (+ 1 a-length b-length)))
  (if result
    (begin (memory-copy result a a-length) (memory-copy (+ result a-length) b (+ 1 b-length))))
  (return result))

(define (dirname-2 a) (b8* b8*)
  ;the posix version of dirname may modify its argument
  (define path-copy b8* (string-clone a)) (return (dirname path-copy)))

(define (ensure-directory-structure path mkdir-mode) (boolean b8* mode-t)
  (if (file-exists? path) (return #t)
    (begin (define path-dirname b8* (dirname-2 path))
      (define status boolean (ensure-directory-structure path-dirname mkdir-mode))
      (free path-dirname) (and status (or (= 0 (mkdir path mkdir-mode)) (= EEXIST errno))))))

(define-macro (array-contains-s array-start array-end search-value index-temp result)
  (set index-temp array-start) (set result #f)
  (while (<= index-temp array-end)
    (if (= (deref index-temp) search-value) (begin (set result #t) break)) (increment-one index-temp)))

(define-macro (require-goto a label) (if (not a) (goto label)))

(pre-if stability-typechecks
  (define-macro (if-typecheck expr action)
    (if (not expr)
      (begin
        (debug-log "type check failed %s"
          (if* (< (string-length (pre-stringify expr)) 24) (pre-stringify expr) ""))
        action)))
  (define-macro (if-typecheck expr action) null))

(define-macro (octet-write-string-binary target a)
  (sprintf target "%d%d%d%d%d%d%d%d"
    (if* (bit-and a 128) 1 0) (if* (bit-and a 64) 1 0)
    (if* (bit-and a 32) 1 0) (if* (bit-and a 16) 1 0)
    (if* (bit-and a 8) 1 0) (if* (bit-and a 4) 1 0) (if* (bit-and a 2) 1 0) (if* (bit-and a 1) 1 0)))

(enum (sph-error-number-memory sph-error-number-input))

(define (error-description n) (char* b32-s)
  (return
    (cond* ((= sph-error-number-memory n) "memory") ((= sph-error-number-input n) "input")
      (else "unknown"))))

(define-macro (local-define-malloc variable-name type on-error)
  (define variable-name type* (malloc (sizeof type))) (if (not variable-name) on-error))

(define-macro (free+null a) (free a) (set a 0))
(define-macro (pointer-equal? a b) (= (convert-type a b0*) (convert-type b b0*)))
