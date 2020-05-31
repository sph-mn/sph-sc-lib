/* "array4" - struct {.current, .data, .size, .used} that combines pointer, length, used length and iteration index in one object.
   this type can be used similar to linked lists.
   most bindings are generic macros that will work on any array4 type. array4-add and array4-forward go from left to right.
   examples:
     array4_declare_type(my_type, int);
     my_type_t a;
     if(my_type_new(4, &a)) {
       // memory allocation error
     }
     array4_add(a, 1);
     array4_add(a, 2);
     while(array4_in_range(a)) {
       array4_get(a);
       array4_forward(a);
     }
     array4_free(a); */
#include <stdlib.h>
#define array4_declare_type(name, element_type) \
  typedef struct { \
    element_type* data; \
    size_t size; \
    size_t used; \
    size_t current; \
  } name##_t; \
  uint8_t name##_new_custom(size_t size, void* (*alloc)(size_t), name##_t* a) { \
    element_type* data; \
    data = alloc((size * sizeof(element_type))); \
    if (!data) { \
      return (1); \
    }; \
    a->data = data; \
    a->size = size; \
    a->used = 0; \
    a->current = 0; \
    return (0); \
  } \
\
  /** return 0 on success, 1 for memory allocation error */ \
  uint8_t name##_new(size_t size, name##_t* a) { return ((name##_new_custom(size, malloc, a))); } \
  uint8_t name##_resize_custom(name##_t* a, size_t new_size, void* (*realloc)(void*, size_t)) { \
    element_type* data = realloc((a->data), (new_size * sizeof(element_type))); \
    if (!data) { \
      return (1); \
    }; \
    a->data = data; \
    a->size = new_size; \
    a->used = ((new_size < a->used) ? new_size : a->used); \
    a->current = ((new_size < a->current) ? new_size : a->current); \
    return (0); \
  } \
\
  /** return 0 on success, 1 for realloc error */ \
  uint8_t name##_resize(name##_t* a, size_t new_size) { return ((name##_resize_custom(a, new_size, realloc))); }
#define array4_declare(a, type) type a = { 0, 0, 0, 0 }
#define array4_add(a, value) \
  (a.data)[a.used] = value; \
  a.used = (a.used + 1)
#define array4_set_null(a) \
  a.used = 0; \
  a.size = 0; \
  a.data = 0; \
  a.current = 0
#define array4_get_at(a, index) (a.data)[index]
#define array4_clear(a) a.used = 0
#define array4_remove(a) a.used -= 1
#define array4_size(a) a.used
#define array4_max_size(a) a.size
#define array4_free(a) free((a.data))
#define array4_full(a) (a.used == a.size)
#define array4_not_full(a) (a.used < a.size)
#define array4_take(a, data, size, used) \
  a->data = data; \
  a->size = size; \
  a->used = used
#define array4_in_range(a) (a.current < a.used)
#define array4_get(a) (a.data)[a.current]
#define array4_forward(a) a.current += 1
#define array4_rewind(a) a.current = 0
