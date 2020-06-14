(sc-comment
  "\"array3\" - struct {.data, .size, .used} that combines memory pointer, length and used length in one object.
   the \"used\" property is to support variable length data in a fixed size memory area.
   depends on stdlib.h for malloc. custom allocators can be used.
   examples:
     array3_declare_type(my_type, int);
     my_type_t a;
     if(my_type_new(4, &a)) {
       // memory allocation error
     }
     array3_add(a, 1);
     array3_add(a, 2);
     size_t i = 0;
     for(i = 0; i < a.size; i += 1) {
       array3_get(a, i);
     }
     array3_free(a);")

(pre-define
  (array3-declare-type name element-type)
  (array3-declare-type-custom name element-type malloc realloc)
  (array3-declare-type-custom name element-type malloc realloc)
  (begin
    (declare (pre-concat name _t) (type (struct (data element-type*) (size size-t) (used size-t))))
    (define ((pre-concat name _new) size a) (uint8-t size-t (pre-concat name _t*))
      "return 0 on success, 1 for memory allocation error"
      (declare data element-type*)
      (set data (malloc (* size (sizeof element-type))))
      (if (not data) (return 1))
      (set a:data data a:size size a:used 0)
      (return 0))
    (define ((pre-concat name _resize) a new-size) (uint8-t (pre-concat name _t*) size-t)
      "return 0 on success, 1 for realloc error"
      (define data element-type* (realloc a:data (* new-size (sizeof element-type))))
      (if (not data) (return 1))
      (set a:data data a:size new-size a:used (if* (< new-size a:used) new-size a:used))
      (return 0)))
  (array3-declare a type) (define a type (struct-literal 0 0 0))
  (array3-add a value) (set (array-get a.data a.used) value a.used (+ a.used 1))
  (array3-set-null a) (set a.used 0 a.size 0 a.data 0)
  (array3-get a index) (array-get a.data index)
  (array3-clear a) (set a.used 0)
  (array3-remove a) (set- a.used 1)
  (array3-size a) a.used
  (array3-max-size a) a.size
  (array3-free a) (free a.data)
  (array3-full a) (= a.used a.size)
  (array3-not-full a) (< a.used a.size)
  (array3-take a data size used) (set a:data data a:size size a:used used)
  (array3-data-last a) (array-get a.data (- a.size 1))
  (array3-declare-stack name array-size type-t value-t)
  (begin
    (declare (pre-concat name _data) (array value-t array-size) name type-t)
    (set name.data (pre-concat name _data) name.size array-size)))