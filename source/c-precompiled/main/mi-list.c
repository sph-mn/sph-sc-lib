
/* a linked list with custom data type. each element is a heap allocated struct.
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
   access custom fields from the list pointer, for example list->custom_field */
#include <stdlib.h>
#include <inttypes.h>

#define mi_list_declare_struct_type(name, element_type) \
  typedef struct name##_struct { \
    struct name##_struct* link; \
    element_type data; \
  } name##_t
#define mi_list_declare_functions(name, element_type) \
  /** removes and deallocates the first element */ \
  name##_t* name##_drop(name##_t* a) { \
    name##_t* a_next = mi_list_rest(a); \
    free(a); \
    return (a_next); \
  } \
\
  /** it would be nice to set the pointer to zero, but that would require more indirection with a pointer-pointer */ \
  void name##_destroy(name##_t* a) { \
    name##_t* a_next = 0; \
    while (a) { \
      a_next = a->link; \
      free(a); \
      a = a_next; \
    }; \
  } \
  size_t name##_length(name##_t* a) { \
    size_t result = 0; \
    while (a) { \
      result = (1 + result); \
      a = mi_list_rest(a); \
    }; \
    return (result); \
  } \
  name##_t* name##_add(name##_t* a, element_type value) { \
    name##_t* element = calloc(1, (sizeof(name##_t))); \
    if (!element) { \
      return (0); \
    }; \
    element->data = value; \
    element->link = a; \
    return (element); \
  }
#define mi_list_declare_type(name, element_type) \
  mi_list_declare_struct_type(name, element_type); \
  mi_list_declare_functions(name, element_type)
#define mi_list_append(a, b) a->link = b
#define mi_list_first(a) a->data
#define mi_list_first_address(a) &(a->data)
#define mi_list_rest(a) a->link
