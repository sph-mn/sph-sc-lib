(sc-comment
  "a linked list with custom data type. each element is a heap allocated struct.
   examples:
     mi_list_declare_type(list_64, uint64_t);
     list_64_t* a;
     a = list_64_add(a, 112);
     mi_list_first(a);
     mi_list_first_address(a);
     mi_list_rest(a);
     list_64_length(a);
     list_64_destroy(a);
   to use a custom node structure, declare the struct and use mi_list_declare_functions
   example:
     typedef struct list_64_struct {
       struct list_64_struct* link;
       element_type data;
       int custom_field;
     } list_64_t;
     mi_list_declare_functions(list_64, uint64_t);
   the struct must contain the fields link and data, everything else can be customised.
   mi-list-first and mi-list-first-address will only return .data.
   access custom fields from the list pointer, for example list->custom_field")

(pre-include "stdlib.h" "inttypes.h")

(pre-define
  (mi-list-declare-struct-type name element-type)
  (declare (pre-concat name _t)
    (type
      (struct
        (pre-concat name _struct)
        (link (struct (pre-concat name _struct*)))
        (data element-type))))
  (mi-list-declare-functions name element-type)
  (begin
    (define ((pre-concat name _drop) a) ((pre-concat name _t*) (pre-concat name _t*))
      "removes and deallocates the first element"
      (define a-next (pre-concat name _t*) (mi-list-rest a))
      (free a)
      (return a-next))
    (define ((pre-concat name _destroy) a) (void (pre-concat name _t*))
      "it would be nice to set the pointer to zero, but that would require more indirection with a pointer-pointer"
      (define a-next (pre-concat name _t*) 0)
      (while a (set a-next a:link) (free a) (set a a-next)))
    (define ((pre-concat name _length) a) (size-t (pre-concat name _t*))
      (define result size-t 0)
      (while a (set result (+ 1 result) a (mi-list-rest a)))
      (return result))
    (define ((pre-concat name _add) a value)
      ((pre-concat name _t*) (pre-concat name _t*) element-type)
      (define element (pre-concat name _t*) (calloc 1 (sizeof (pre-concat name _t))))
      (if (not element) (return 0))
      (set element:data value element:link a)
      (return element)))
  (mi-list-declare-type name element-type)
  (begin
    (mi-list-declare-struct-type name element-type)
    (mi-list-declare-functions name element-type))
  (mi-list-append a b) (set a:link b)
  (mi-list-first a) a:data
  (mi-list-first-address a) &a:data
  (mi-list-rest a) a:link)