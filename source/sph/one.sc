;this library contains various more experimental bindings

(pre-include-once stdlib-h "stdlib.h"
  errno-h "errno.h"
  sys-stat-h "sys/stat.h" libgen-h "libgen.h" string-h "string.h" unistd-h "unistd.h")

(pre-define string-length strlen
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

(pre-define (file-exists? path) (not (equal? (access path F-OK) -1)))

(define (ensure-trailing-slash a) (char* char*)
  (define a-len b8 (string-length a))
  (if (or (not a-len) (equal? #\/ (deref (+ a (- a-len 1))))) (return a)
    (begin (define new-a char* (malloc (+ 2 a-len))) (if (not new-a) (return 0))
      (memory-copy new-a a a-len) (memory-copy (+ new-a a-len) "/" 1)
      (set (deref new-a (+ 1 a-len)) 0) (return new-a))))

(define (string-clone a) (b8* b8*)
  (define a-size size-t (+ 1 (string-length a))) (define result b8* (malloc a-size))
  (if result (memory-copy result a a-size)) (return result))

(define (string-append a b) (b8* b8* b8*)
  ;"always returns a new string"
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
      (free path-dirname) (return (and status (or (= EEXIST errno) (= 0 (mkdir path mkdir-mode))))))))

(pre-define (local-define-malloc variable-name type on-error)
  (define variable-name type* (malloc (sizeof type))) (if (not variable-name) on-error))

(pre-define (free+null a) (free a) (set a 0))
(pre-define (pointer-equal? a b) (= (convert-type a b0*) (convert-type b b0*)))
