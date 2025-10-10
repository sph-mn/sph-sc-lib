(pre-define
  (array2-declare-type name element-type)
  (array2-declare-type-custom name element-type malloc realloc)
  (array2-declare-type-custom name element-type malloc realloc)
  (begin
    (declare (pre-concat name _t) (type (struct (data element-type*) (size size-t))))
    (define ((pre-concat name _new) size a) (uint8-t size-t (pre-concat name _t*))
      "return 0 on success, 1 for memory allocation error"
      (declare data element-type*)
      (set data (malloc (* size (sizeof element-type))))
      (if (not data) (return 1))
      (set a:data data a:size size)
      (return 0))
    (define ((pre-concat name _resize) a new-size) (uint8-t (pre-concat name _t*) size-t)
      "return 0 on success, 1 for realloc error"
      (define data element-type* (realloc a:data (* new-size (sizeof element-type))))
      (if (not data) (return 1))
      (set a:data data a:size new-size)
      (return 0)))
  (array2-declare a type) (define a type (struct-literal 0))
  (array2-set-null a) (set a.size 0 a.data 0)
  (array2-get a index) (array-get a.data index)
  (array2-size a) a.size
  (array2-free a) (free a.data)
  (array2-take a data size) (set a:data data a:size size)
  (array2-data-last a) (array-get a.data (- a.size 1))
  (array2-declare-stack name array-size type-t value-t)
  (begin
    (declare (pre-concat name _data) (array value-t array-size) name type-t)
    (set name.data (pre-concat name _data) name.size array-size)))
