(pre-include-guard-begin sph-filesystem-h-included)
(pre-include "unistd.h" "sys/stat.h" "sys/types.h" "libgen.h" "errno.h" "sph/string.h")
(pre-define (file-exists path) (not (= (access path F-OK) -1)))

(define (dirname-2 a) (char* char*)
  "like posix dirname, but never modifies its argument and always returns a new string"
  (define path-copy char* (string-clone a))
  (return (dirname path-copy)))

(define (ensure-directory-structure path mkdir-mode) (uint8-t char* mode-t)
  "return 1 if the path exists or has been successfully created"
  (if (file-exists path) (return #t)
    (begin
      (define path-dirname char* (dirname-2 path))
      (define status uint8-t (ensure-directory-structure path-dirname mkdir-mode))
      (free path-dirname)
      (return (and status (or (= EEXIST errno) (= 0 (mkdir path mkdir-mode))))))))

(pre-include-guard-end)
