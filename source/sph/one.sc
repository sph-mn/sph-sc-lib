(pre-include-once string-h "string.h"
  stdlib-h "stdlib.h"
  libgen-h "libgen.h"
  errno-h "errno.h" sys-stat-h "sys/stat.h" unistd-h "unistd.h" sys-types-h "sys/types.h")

(pre-define (file-exists? path) (not (equal? (access path F-OK) -1)))

(define (ensure-trailing-slash a result) (b8 b8* b8**)
  (define a-len b32 (strlen a))
  (if (or (not a-len) (equal? #\/ (deref (+ a (- a-len 1)))))
    (begin (set (deref result) a) (return 0))
    (begin (define new-a char* (malloc (+ 2 a-len))) (if (not new-a) (return 1))
      (memcpy new-a a a-len) (memcpy (+ new-a a-len) "/" 1)
      (set (deref new-a (+ 1 a-len)) 0) (set (deref result) new-a) (return 2))))

(define (string-clone a) (b8* b8*)
  (define a-size size-t (+ 1 (strlen a))) (define result b8* (malloc a-size))
  (if result (memcpy result a a-size)) (return result))

(define (string-append a b) (b8* b8* b8*)
  ;"always returns a new string"
  (define a-length size-t (strlen a)) (define b-length size-t (strlen b))
  (define result b8* (malloc (+ 1 a-length b-length)))
  (if result (begin (memcpy result a a-length) (memcpy (+ result a-length) b (+ 1 b-length))))
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
;more descriptive names for obscurely named c keywords
(pre-define (increment-one a) (set a (+ 1 a)))
(pre-define (decrement-one a) (set a (- a 1)))
;following are helpers for using the local-memory pattern. it creates an allocated-heap-memory registry in local variables with a more automated free so that
;different routine end points, like after error occurences, can easily free all memory up to point

(pre-define (local-memory-init max-address-count)
  (define sph-local-memory-addresses[max-address-count] b0*) (define sph-local-memory-index b8 0))

(pre-define (local-memory-add pointer)
  ;do not add more entries as given by max-address-count or it leads to a buffer overflow
  (set (deref sph-local-memory-addresses sph-local-memory-index) pointer
    sph-local-memory-index (+ 1 sph-local-memory-index)))

(pre-define local-memory-free
  (while sph-local-memory-index (decrement-one sph-local-memory-index)
    (free (deref (+ sph-local-memory-addresses sph-local-memory-index)))))
