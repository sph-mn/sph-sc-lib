(pre-include-guard-begin sph-array-h)

(sc-comment
  "depends on stdlib.h (malloc/realloc/free) and string.h (memset) for the default allocators")

(pre-include "sph/status.h")

(pre-define
  sph-array-status-id-memory 1
  sph-array-status-group (convert-type "sph" uint8-t*)
  sph-array-memory-error (status-set-goto sph-array-status-group sph-array-status-id-memory)
  sph-array-growth-factor 2
  (sph-array-default-alloc s es) (malloc (* s es))
  (sph-array-default-realloc d s u n es) (realloc d (* n es))
  (sph-array-default-alloc-zero s) (calloc 1 s)
  (sph-array-declare-type-custom name element-type sph-array-alloc sph-array-realloc sph-array-free)
  (begin
    (declare (pre-concat name _t) (type (struct (size size-t) (used size-t) (data element-type*))))
    (define ((pre-concat name _new) size a) (status-t size-t (pre-concat name _t*))
      status-declare
      (define data element-type* (sph-array-alloc size (sizeof element-type)))
      (if (not data) sph-array-memory-error)
      (set a:data data a:size size a:used 0)
      (label exit status-return))
    (define ((pre-concat name _resize) a new-size) (status-t (pre-concat name _t*) size-t)
      status-declare
      (define
        data element-type* (sph-array-realloc a:data a:size a:used new-size (sizeof element-type)))
      (if (not data) sph-array-memory-error)
      (set a:data data a:size new-size a:used (if* (< new-size a:used) new-size a:used))
      (label exit status-return))
    (define ((pre-concat name _free) a) (void (pre-concat name _t*)) (sph-array-free a:data))
    (define ((pre-concat name _ensure) a needed) (status-t (pre-concat name _t*) size-t)
      status-declare
      (return
        (if* a:data
          (if* (< (- a:size a:used) needed)
            ((pre-concat name _resize) a (* sph-array-growth-factor a:size))
            status)
          ((pre-concat name _new) needed a)))))
  (sph-array-declare-type name element-type)
  (sph-array-declare-type-custom name element-type
    sph-array-default-alloc sph-array-default-realloc free)
  (sph-array-declare-type-zeroed name element-type)
  (sph-array-declare-type-custom name element-type
    sph-array-default-alloc-zero sph-array-default-realloc-zero free)
  (sph-array-declare a type) (define a type (struct-literal 0 0 0))
  (sph-array-add a value) (begin (set (array-get a.data a.used) value) (set+ a.used 1))
  (sph-array-set-null a) (set a.used 0 a.size 0 a.data 0)
  (sph-array-get a index) (array-get a.data index)
  (sph-array-get-pointer a index) (+ a.data index)
  (sph-array-clear a) (set a.used 0)
  (sph-array-remove a) (set- a.used 1)
  (sph-array-unused-size a) (- a.size a.used)
  (sph-array-full a) (= a.used a.size)
  (sph-array-not-full a) (< a.used a.size)
  (sph-array-take a data size used) (set a:data data a:size size a:used used)
  (sph-array-last a) (array-get a.data (- a.used 1))
  (sph-array-first-unused a) (array-get a.data a.used)
  (sph-array-declare-stack name array-size type-t value-t)
  (begin
    (declare (pre-concat name _data) (array value-t array-size) name type-t)
    (set name.data (pre-concat name _data) name.size array-size name.used 0)))

(define (sph-array-default-realloc-zero d s u n es) (void* void* size-t size-t size-t size-t)
  (define nd void* (realloc d (* n es)))
  (if (not nd) (return 0))
  (if (> n s) (memset (+ (convert-type nd uint8-t*) (* s es)) 0 (* es (- n s))))
  (return nd))

(pre-include-guard-end)
