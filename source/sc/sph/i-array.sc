(sc-comment
  "\"iteration array\" - an array type that is used more like a linked list and makes iteration simpler."
  "most bindings are generic macros that will work on all i_array types."
  "i_array_add and i_array_forward go from left to right."
  "examples:
     i_array_declare_type(my_type, int);
     i_array_allocate_my_type(a, 4);
     i_array_add(a, 1);
     i_array_add(a, 2);
     while(i_array_in_range(a)) { i_array_get(a); }
     i_array_free(a);")

(pre-include "stdlib.h")

(pre-define
  (i-array-declare-type name element-type)
  (begin
    (sc-comment
      "* current: to avoid having to write for-loops. it is what would be the index variable in loops
      * used: to have variable length content in a fixed length array
      * end: a boundary for iterations
      * start: the beginning of the allocated array and used to rewind and free memory")
    (declare name
      (type
        (struct
          (current element-type*)
          (unused element-type*)
          (end element-type*)
          (start element-type*))))
    (define ((pre-concat i-array-allocate- name) a length) (boolean name* size-t)
      (declare temp element-type*)
      (set temp (malloc (* length (sizeof element-type))))
      (if (not temp) (return 0))
      (set
        a:start temp
        a:current temp
        a:unused temp
        a:end (+ length temp))
      (return 1)))
  (i-array-declare a type)
  (begin
    "define so that in-range is false, length is zero and free doesnt fail"
    (define a type (struct-literal 0 0 0 0)))
  (i-array-add a value)
  (set
    *a.unused value
    a.unused (+ 1 a.unused))
  (i-array-set-null a)
  (begin
    "set so that in-range is false, length is zero and free doesnt fail"
    (set
      a.start 0
      a.unused 0))
  (i-array-in-range a) (< a.current a.unused)
  (i-array-get-at a index) (array-get a.start index)
  (i-array-get a) *a.current
  (i-array-forward a) (set a.current (+ 1 a.current))
  (i-array-rewind a) (set a.current a.start)
  (i-array-clear a) (set a.unused a.start)
  (i-array-remove a) (set a.unused (- a.unused 1))
  (i-array-length a) (- a.unused a.start)
  (i-array-max-length a) (- a.end a.start)
  (i-array-free a) (free a.start))