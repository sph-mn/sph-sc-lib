(sc-comment
  "a linked list with custom data type. each element is a heap allocated struct
   examples:
     mi_list_declare_type(list_64, uint64_t);
     list_64_t* a;
     a = list_64_add(a, 112);
     mi_list_first(a);
     mi_list_first_address(a);
     mi_list_rest(a);
     list_64_length(a);
     list_64_destroy(a);")

(pre-include "stdlib.h" "inttypes.h")

(pre-define
  (mi-list-declare-type name element-type)
  (begin
    (declare (pre-concat name _t)
      (type
        (struct
          (pre-concat name _struct)
          (link (struct (pre-concat name _struct*)))
          (data element-type))))
    (define ((pre-concat name _drop) a) ((pre-concat name _t*) (pre-concat name _t*))
      "removes and deallocates the first element"
      (define a-next (pre-concat name _t*) (mi-list-rest a))
      (free a)
      (return a-next))
    (define ((pre-concat name _destroy) a) (void (pre-concat name _t*))
      "it would be nice to set the pointer to zero, but that would require more indirection with a pointer-pointer"
      (define a-next (pre-concat name _t*) 0)
      (while a (set a-next a:link) (free a) (set a a-next)))
    (define ((pre-concat name _add) a value)
      ((pre-concat name _t*) (pre-concat name _t*) element-type)
      (define element (pre-concat name _t*) (calloc 1 (sizeof (pre-concat name _t))))
      (if (not element) (return 0))
      (set element:data value element:link a)
      (return element))
    (define ((pre-concat name _length) a) (size-t (pre-concat name _t*))
      (define result size-t 0)
      (while a (set result (+ 1 result) a (mi-list-rest a)))
      (return result)))
  (mi-list-first a) a:data
  (mi-list-first-address a) &a:data
  (mi-list-rest a) a:link)