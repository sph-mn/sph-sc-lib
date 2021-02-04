
#define array2_declare_type(name, element_type) array2_declare_type_custom(name, element_type, malloc, realloc)
#define array2_declare_type_custom(name, element_type, malloc, realloc) \
  typedef struct { \
    element_type* data; \
    size_t size; \
  } name##_t; \
  /** return 0 on success, 1 for memory allocation error */ \
  uint8_t name##_new(size_t size, name##_t* a) { \
    element_type* data; \
    data = malloc((size * sizeof(element_type))); \
    if (!data) { \
      return (1); \
    }; \
    a->data = data; \
    a->size = size; \
    return (0); \
  } \
\
  /** return 0 on success, 1 for realloc error */ \
  uint8_t name##_resize(name##_t* a, size_t new_size) { \
    element_type* data = realloc((a->data), (new_size * sizeof(element_type))); \
    if (!data) { \
      return (1); \
    }; \
    a->data = data; \
    a->size = new_size; \
    return (0); \
  }
#define array2_declare(a, type) type a = { 0, 0, 0 }
#define array2_set_null(a) \
  a.size = 0; \
  a.data = 0
#define array2_get(a, index) (a.data)[index]
#define array2_size(a) a.size
#define array2_free(a) free((a.data))
#define array2_take(a, data, size) \
  a->data = data; \
  a->size = size
#define array2_data_last(a) (a.data)[(a.size - 1)]
#define array2_declare_stack(name, array_size, type_t, value_t) \
  value_t name##_data[array_size]; \
  type_t name; \
  name.data = name##_data; \
  name.size = array_size
