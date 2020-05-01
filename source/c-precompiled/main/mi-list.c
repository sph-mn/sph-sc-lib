/* a linked list with custom data type. each element is a heap allocated struct
   examples:
     mi_list_declare_type(list_64, uint64_t);
     list_64_t* a;
     a = list_64_add(a, 112);
     mi_list_first(a);
     mi_list_first_address(a);
     mi_list_rest(a);
     list_64_length(a);
     list_64_destroy(a); */
#include <stdlib.h>
#include <inttypes.h>
#define mi_list_declare_type(name, element_type) \
  typedef struct name##_struct { \
    struct name##_struct* link; \
    element_type data; \
  } name##_t; \
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
  name##_t* name##_add(name##_t* a, element_type value) { \
    name##_t* element = calloc(1, (sizeof(name##_t))); \
    if (!element) { \
      return (0); \
    }; \
    element->data = value; \
    element->link = a; \
    return (element); \
  } \
  size_t name##_length(name##_t* a) { \
    size_t result = 0; \
    while (a) { \
      result = (1 + result); \
      a = mi_list_rest(a); \
    }; \
    return (result); \
  }
#define mi_list_first(a) a->data
#define mi_list_first_address(a) &(a->data)
#define mi_list_rest(a) a->link
