/* "iteration array" - a fixed size array with variable length content that makes iteration easier to code. it is used similar to a linked list.
  most bindings are generic macros that will work on all i-array types. i-array-add and i-array-forward go from left to right.
  examples:
    i-array-declare-type(my-type, int);
    i-array-allocate-my-type(a, 4);
    i-array-add(a, 1);
    i-array-add(a, 2);
    while(i-array-in-range(a)) { i-array-get(a); }
    i-array-free(a); */
#include <stdlib.h>
/** .current: to avoid having to write for-loops. it is what would be the index variable in loops
     .unused: to have variable length content in a fixed length array. points outside the memory area after the last element has been added
     .end: a boundary for iterations
     .start: the beginning of the allocated array and used for rewind and free */
#define i_array_declare_type(name, element_type) \
  typedef struct { \
    element_type* current; \
    element_type* unused; \
    element_type* end; \
    element_type* start; \
  } name; \
  uint8_t i_array_allocate_##name(name* a, size_t length) { \
    element_type* start; \
    start = malloc((length * sizeof(element_type))); \
    if (!start) { \
      return (0); \
    }; \
    a->start = start; \
    a->current = start; \
    a->unused = start; \
    a->end = (length + start); \
    return (1); \
  }
/** define so that in-range is false, length is zero and free doesnt fail */
#define i_array_declare(a, type) type a = { 0, 0, 0, 0 }
#define i_array_add(a, value) \
  *(a.unused) = value; \
  a.unused = (1 + a.unused)
/** set so that in-range is false, length is zero and free doesnt fail */
#define i_array_set_null(a) \
  a.start = 0; \
  a.unused = 0
#define i_array_in_range(a) (a.current < a.unused)
#define i_array_get_at(a, index) (a.start)[index]
#define i_array_get(a) *(a.current)
#define i_array_forward(a) a.current = (1 + a.current)
#define i_array_rewind(a) a.current = a.start
#define i_array_clear(a) a.unused = a.start
#define i_array_remove(a) a.unused = (a.unused - 1)
#define i_array_length(a) (a.unused - a.start)
#define i_array_max_length(a) (a.end - a.start)
#define i_array_free(a) free((a.start));