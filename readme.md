# sph-sc-lib

various small standalone c utility libraries.
c code is in src/c-precompiled. [sc](https://github.com/sph-mn/sph-sc) versions are in src/sc. the libraries are developed in sc and then translated to normal, readable c, formatted with clang-format

# included libraries
* [array](#array): structs for arrays that include size or a count of used content
* [futures](#futures): fine-grained parallelism with objects that can be waited on for results
* [hashtable](#hashtable): hash-tables for any key/value type
* [list](#list): slist, a minimal singly-linked stack allocator-bound list and dlist, an intrusive doubly-linked list
* [memreg](#memreg): track heap memory allocations in function scope
* [memory](#memory): allocate or register memory using status_t
* [queue](#queue): a queue for any data type
* [quicksort](#quicksort): a generic implementation for arrays of any type
* [random](#random): pseudo random number generation
* [set](#set): sets for any key/value type
* [spline-path](#spline-path): interpolated 2d paths between given points
* [status](#status): return-status and error handling using a tiny object with status/error id and source library id
* [test](#test): minimalistic test runner macros
* [thread-pool](#thread-pool): task queue with pthread threads and wait conditions for pausing inactive threads

## experimental
* [ikv](#ikv): file format for nested named arrays, for example for text configuration
* guile: a few helpers for working with guile

# license
code is under gpl3+, documentation under cc-by-nc.

# compilation
* include "sph/library_name.h" (if exists) if the .c file is included in a shared library being used
* include "sph/library_name.c" for the rest of the code

# array
macro that defines a generic dynamic array type for arbitrary element types.

## characteristics
* structure layout: `{ .data, .size, .used }`
* `.data` points to allocated memory
* `.size` is the allocated element capacity
* `.used` tracks how many elements are currently stored
* supports variable-length data within a fixed-size memory block
* uses `malloc` and `free` by default; custom allocators may be substituted
* fixed capacity unless explicitly resized
* minimal code size and suitable for inclusion-based builds

## usage examples
```
#include "sph/array.h"

sph_array_declare_type(my_type, int);

int main(void) {
  status_declare;
  my_type_t a;
  status_require(my_type_new(4, &a));
  sph_array_add(a, 1);
  sph_array_add(a, 2);
  for (size_t i = 0; i < a.used; i += 1) {
    sph_array_get(a, i);
  }
  my_type_free(a);
exit:
  status_return;
}
```

`sph_array_declare_type` defines:

```c
// returns 0 on success, 1 if allocation failed
uint8_t name##_new(size_t initial_size, name##_t *result);

// increases capacity, preserving data
uint8_t name##_resize(name##_t *a, size_t new_size);

// adds a value; may resize internally if supported
void sph_array_add(name##_t a, element_type value);

// retrieves element at index
element_type sph_array_get(name##_t a, size_t index);

// releases allocated memory
void name##_free(name##_t a);
```

## layout and sizing

* each declared array type is named `name##_t`
* the array may be resized or reused without reallocating a new object
* `.used` can be reset manually for reuse
* memory allocation is linear in element size and capacity

# futures
provides task objects with functions that are executed in parallel with a thread-pool.
calling touch on an object waits for its completion and returns its result.
depends on thread-pool.c

```c
#include <inttypes.h>
#include "queue.h"
#include "thread-pool.h"
#include "thread-pool.c"
#include "futures.c"

void* future_work(void* data) {
  // return value is a void pointer.
  // just for example this returns a new object. the data could be modified instead
  uint8_t* a;
  a = malloc(sizeof(uint8_t));
  *a = 2 + *(uint8_t*)(data);
  return a;
};

int main() {
  future_t* future;
  uint8_t data;
  uint8_t* result;
  data = 8;
  // this must be called at least once somewhere before futures can be used
  future_init(10);

  // create a new future and get the result later with touch
  future = future_new(future_work, &data);
  if(!future) return 1;
  result = (uint8_t*)(touch(future));

  // result is 10
  free(result);
  // this frees the thread-pool in case futures are not needed anymore in the process or before exit
  future_deinit();
  return 0;
}
```

```
gcc "$c/test/thread-pool.c" -o tmp/test-thread-pool -lpthread -D _DEFAULT_SOURCE
```

# hashtable
macro that defines open-addressing hash-table data structures for arbitrary key/value types.

## implementation
* linear probing for collision resolution
* separate arrays for flags, keys, and values
* flags encode slot state, so no sentinel key or null value is needed
* lookups and insertions operate in constant expected time
* minimal implementation (<150 loc)
* tables are fixed-size after creation; reallocation requires constructing a new table and reinserting entries

## usage examples
```
#include "sph/hashtable.h"

// name, key_type, value_type, hash_function, equal_function, size_factor
sph_hashtable_declare_type(mytype, uint64_t, uint32_t,
  sph_hashtable_hash_integer, sph_hashtable_equal_integer, 2);

int main(void) {
  mytype_t ht;
  if (mytype_new(200, &ht)) return 1;
  mytype_set(ht, 44, 5);
  mytype_get(ht, 44);
  mytype_remove(ht, 44);
  mytype_free(ht);
  return 0;
}
```

`sph_hashtable_declare_type` generates these functions
(`##` denotes token concatenation; e.g., `name##_new` → `mytype_new`):

```c
// returns 0 on success, 1 on allocation failure
uint8_t name##_new(size_t min_size, name##_t *result);

// returns pointer to inserted or existing value, 0 if table full
value_type *name##_set(name##_t a, key_type key, value_type value);

// returns pointer to stored value, 0 if not found
value_type *name##_get(name##_t a, key_type key);

// returns 0 if removed, 1 if not found
uint8_t name##_remove(name##_t a, key_type key);

void name##_free(name##_t a);
```

example hash and equality macros:

```c
#define sph_hashtable_hash_integer(key, size) ((key) % (size))
#define sph_hashtable_equal_integer(a, b) ((a) == (b))
```

# list
macros for generic singly and doubly linked lists with inline utility functions.

## singly list
`sph_slist_declare_type(name, element_type)` defines:
* `typedef struct name##_node { struct name##_node *next; element_type value; } name##_t;`
* `name##_t *name##_add_front(name##_t *head, element_type value);`
  * allocate node, prepend, return new head
* `name##_t *name##_remove_front(name##_t *head);`
  * free head, return next
* `void name##_destroy(name##_t *head);`
  * free full chain
* `size_t name##_count(name##_t *head);`
  * count nodes
* `void name##_append(name##_t *head, name##_t *tail);`
  * set `head->next = tail`

## doubly list
`sph_dlist_declare_type(name, element_type)` defines:
* `typedef struct name##_node { struct name##_node *previous; struct name##_node *next; element_type value; } name##_t;`
* `void name##_reverse(name##_t **head);`
  * reverse links in place
* `void name##_validate(name##_t *head);`
  * print link errors with index
* `void name##_unlink(name##_t **head, name##_t *node);`
  * detach node from list
* `void name##_print(name##_t *head);`
  * print link layout per node

## example
```
#include "sph/list.h"
sph_slist_declare_type(slist_int, int)
int main(void) {
  slist_int_t *head = 0;
  head = slist_int_add_front(head, 3);
  head = slist_int_add_front(head, 2);
  head = slist_int_add_front(head, 1);
  printf("%zu\n", slist_int_count(head));
  head = slist_int_remove_front(head);
  slist_int_destroy(head);
  return 0;
}
```

# memreg
track memory allocations locally on the stack and free all allocations up to point easily.

* memreg: fixed store, caller-sized
* memreg2: fixed store, caller-sized, custom free handler

```c
#include "sph/memreg.c"

int main() {
  memreg_init(2);
  int* data_a = malloc(12 * sizeof(int));
  if(!data_a) goto exit;  // have to free nothing
  memreg_add(data_a);
  // more code ...
  char* data_b = malloc(20 * sizeof(char));
  if(!data_b) goto exit;  // have to free "data_a"
  memreg_add(data_b);
  // ...
  if(is_error) goto exit;  // have to free "data_a" and "data_b"
  // ...
exit:
  memreg_free;
  return 0;
}
```

introduces two local variables: memreg_register, an array for addresses, and memreg_index, the next index.
memreg_init size must be a static value

## memreg_named
``memreg.c`` also contains a *_named variant that supports multiple concurrent registers identified by name

```c
memreg_init_named(testname, 1);
memreg_add_named(testname, &variable);
memreg_free_named(testname);
```

# memory
functions and macros for dynamic memory allocation and tracked memory management.

## basic allocation
```
status_t sph_memory_malloc(size_t size, void **result);
status_t sph_memory_malloc_string(size_t length, uint8_t **result);
status_t sph_memory_calloc(size_t size, void **result);
status_t sph_memory_realloc(size_t size, void **memory);
```

* `malloc` allocates `size` bytes
* `malloc_string` allocates `length+1` bytes and null-terminates
* `calloc` allocates `size` bytes cleared to zero
* `realloc` resizes memory and updates pointer

return value: `status_t` (0 on success, error status otherwise)

## memory tracking
```
status_t sph_memory_add_with_handler(sph_memory_t *a, void *address, void (*handler)(void *));
void sph_memory_destroy(sph_memory_t *a);
```

* `sph_memory_add_with_handler` registers `address` with its release `handler`
* `sph_memory_destroy` calls all registered handlers and clears array

## convenience macros
```
#define sph_malloc(size, result) sph_memory_malloc(size, (void **)(result))
#define sph_malloc_string(size, result) sph_memory_malloc_string(size, (uint8_t **)(result))
#define sph_calloc(size, result) sph_memory_calloc(size, (void **)(result))
#define sph_realloc(size, result) sph_memory_realloc(size, (void **)(result))
#define sph_memory_init(a) a.data = 0
```

## example
```
#include "sph/memory.h"

int main(void) {
  status_declare;
  uint8_t *buf;
  status_require(sph_malloc_string(32, &buf));
  sph_memory_t mem;
  sph_memory_init(mem);
  sph_memory_add_with_handler(&mem, buf, free);
  sph_memory_destroy(&mem);
exit:
  status_return;
}
```

# queue
a fifo queue with the operations enqueue and dequeue that can enqueue structs of mixed types.
elements need to be a struct with a field queue_node_t with a specific name. a queue_node_t object to be added must not already be in the queue.
the queue will use and reference the queue_node field and does not need to allocate new memory. depends on queue.c

## example usage
```c
typedef struct {
  // custom field definitions ...
  queue_node_t queue_node;
} element_t;

element_t e;
queue_t q;
queue_init(&q);
queue_enq(&q, &e.queue_node);
queue_get(queue_deq(&q), element_t, queue_node);
if(0 = q.size) { printf("it's empty\n"); }
```

# set
macro that defines open-addressing set data structures for arbitrary value types.

## characteristics
* linear probing for lookup and insertion
* backward-shift deletion to preserve probe continuity
* expected constant-time insert, remove, and lookup
* fixed capacity; to expand, create a larger set and reinsert values
* occupancy tracked with a bitset, no tombstone markers
* supports one special "null" value as a valid element using a nullable flag

## usage examples
```
#include "sph/set.h"

// name, value_type, hash, equal, null_value, size_factor
sph_set_declare_type(myset, int, sph_set_hash_integer,
  sph_set_equal_integer, 0, 2);

int main(void) {
  myset_t a;
  if (myset_new(3, &a)) return 1;
  myset_add(&a, 3);
  myset_add(&a, 5);
  myset_remove(&a, 3);
  myset_get(a, 5);
  myset_free(a);
  return 0;
}
```

`sph_set_declare_type` defines these functions:

```c
// returns 0 on success, 1 if memory allocation failed
uint8_t name##_new(size_t min_size, name##_t *result);

// clears all entries
void name##_clear(name##_t *a);

// returns a pointer to the stored value, or 0 if not found
// if the value equals the null value, returns a.values if present, otherwise 0
value_type *name##_get(name##_t a, value_type value);

// inserts the value if not present and returns its address, or 0 if no space remains
// if the value equals the null value, sets a->nullable and returns a->values
value_type *name##_add(name##_t *a, value_type value);

// removes the value and returns 0 on success, 1 if not found
// if the value equals the null value, clears a->nullable
uint8_t name##_remove(name##_t *a, value_type value);

void name##_free(name##_t a);
```

example hash and equality macros:

```c
#define sph_set_hash_integer(value, size) ((value) % (size))
#define sph_set_equal_integer(a, b) ((a) == (b))
```

## layout and sizing
* declared type: `name##_t` contains `.size`, `.mask`, `.values`, `.occupied`, `.nullable`
* the null element, if present, is tracked through `.nullable`, and its storage address is `a.values`
* the number of elements allocated equals the next power of two greater than or equal to `(size_factor × requested_size)`
* the hash function must return an index within the range from zero up to but not including the table size

# status
helpers for error and return status code handling with a routine local goto label and a tiny status object that includes the status id and an id for the library it belongs to, for when multiple libraries can return possibly overlapping error codes

status_t is defined as follows
```c
typedef struct {
  int id;
  uint8_t* group;
} status_t;
```

group is a string, otherwise it would be more difficult to not conflict with groups used by other libraries

## usage example
```c
status_t test() {
  status_declare;
  if (1 < 2) {
    int group_id = "test";
    int error_id = 456;
    status_set_goto(group_id, error_id);
  }
exit:
  return status;
}

int main() {
  status_declare;
  // code ...
  status_require(test());
  // more code ...
exit:
  return status.id;
}
```

``status_require`` goes directly to exit if the returned ``status_t`` from ``test()`` isnt ``status_id_success``

## macros
~~~
status_declare: declares a variable with name `status` of type `status_t`
status_group_undefined
status_i_require(expression): `status.id = expression`, otherwise like status_require
status_id_success: 0
status_is_failure: true if status.id is zero
status_is_success: true if status.id is non-zero
status_require(expression): evaluates `status = expression` and jumps to exit on error
status_return: same as `return status`
status_set(group_id, status_id): `status.group = group_id; status.id = status_id;`
status_set_goto(group_id, status_id): like status_set but goes to exit
~~~

## types
```
status_t: struct
  id: int
  group: uint8_t*
```

# test
[test.h](https://github.com/sph-mn/sph-sc-lib/blob/master/src/c-precompiled/sph/test.h)

convenient testing with just a few tiny macros.

each assertion and test prints its result to standard output.

```c
#include "sph/status.h"
#include "sph/test.h"

status_t test_example() {
  test_helper_assert("my assertion", 1 == 1)
exit:
  status_return;
}

int main() {
  status_declare;
  test_helper_test_one(test_example);
exit:
  test_helper_display_summary();
  return status.id;
}
```

# thread-pool
```c
#include <inttypes.h>
#include "sph/queue.h"
#include "sph/thread-pool.c"

void work(thread_pool_task_t* task) {
  // ... do things in parallel ...
  // task can be freed here
  free(task);
};

int main() {
  int error;
  sph_thread_pool_t pool;
  sph_thread_pool_task_t* task;
  error = sph_thread_pool_new(10, &pool);
  if(error) return error;
  task = malloc(sizeof(sph_thread_pool_task_t));
  if(!task) return 1;
  task->f = work;
  sph_thread_pool_enqueue(a, task);
  // when the thread pool is not needed anymore then all threads can be closed.
  // arguments: thread_pool, no_wait, discard_queue
  sph_thread_pool_finish(&pool, 0, 0);
  return 0;
}
```

# spline-path
spline-path creates discrete 2d paths with segments that interpolate between given points.
paths can be composed to construct more complex paths.

implemented segment types and interpolation methods
* linear: draws lines between points
* bezier: 4 point cubic bezier interpolation
* move: gap, moves the point the next segment starts on
* constant: repeats the last value as a flat line to infinity
* path: another spline path as a segment
* power: power-curve interpolation. 'y = y0 + (y1 - y0) * t ** gamma'
* exponential: 'y = y0 + (y1 - y0) * ((e ** (gamma * t) - 1) / (e ** gamma - 1))'

features
* extremely fast through only supporting 2d, with only selected and optimized interpolators, limited segment count and sampling portions of paths at once instead of only single points
* maps from one independent discrete value to one dependent continuous value and only the dependent value is returned
* paths are stateless and the same path object can be used by multiple threads
* multidimensional interpolation can be archieved with separate calls for additional dimensions
* originally developed for dsp and the sampling of many paths 96000 times per second in parallel

## example
example of a path that begins at x 10, draws a line to x 20, a bezier curve to x 40 and any y value after that will be 25.
paths start at (0, 0) and every segment gives the target point to draw to, like lineTo and similar in svg paths.

```c
int main() {
  // declaration
  spline_path_value_t out[50];
  spline_path_time_t i;
  spline_path_t path;
  spline_path_point_t p;
  spline_path_segment_t s;
  spline_path_segment_t segments[4];
  spline_path_segment_count_t segments_len;
  // path segment configuration.
  s.interpolator = spline_path_i_move;
  p.x = 10;
  p.y = 5;
  (s.points)[0] = p;
  segments[0] = s;
  s.interpolator = spline_path_i_line;
  p.x = 20;
  p.y = 10;
  (s.points)[0] = p;
  segments[1] = s;
  s.interpolator = spline_path_i_bezier;
  p.x = 25;
  p.y = 15;
  (s.points)[0] = p;
  p.x = 30;
  p.y = 20;
  (s.points)[1] = p;
  p.x = 40;
  p.y = 25;
  (s.points)[2] = p;
  segments[2] = s;
  s.interpolator = spline_path_i_constant;
  segments[3] = s;
  segments_len = 4;

  // path object creation
  if(spline_path_new(segments_len, segments, &path)) return 1;

  // get points on the path, a range of points at once
  spline_path_get(path, 5, 25, out);
  spline_path_get(path, 25, 55, 20 + out);

  // display the extracted points
  for (i = 0; (i < 50); i = (1 + i)) {
    printf("%lu %f\n", i, (out[i]));
  };

  spline_path_free(path);
}
```

# quicksort
works with arrays of structs and any other array type.

```c
void quicksort(
  uint8_t (*less_p)(void*, ssize_t, ssize_t),
  void (*swap)(void*, ssize_t, ssize_t),
  void* array,
  ssize_t left,
  ssize_t right);
```

```c
#include <stdio.h>
#include <inttypes.h>
#include <sys/types.h>
#include "sph/quicksort.c"

uint8_t uint32_less_p(void* a, ssize_t b, ssize_t c) {
  // typecast "a" to the right pointer type and compare elements at indices b and c
  return (((uint32_t*)(a))[b] < ((uint32_t*)(a))[c]);
}

void uint32_swapper(void* a, ssize_t b, ssize_t c) {
  uint32_t d;
  d = ((uint32_t*)(a))[b];
  ((uint32_t*)(a))[b] = ((uint32_t*)(a))[c];
  ((uint32_t*)(a))[c] = d;
}

#define test_element_count 100;

int main() {
  // prepare input
  size_t i;
  uint32_t uint32_array[test_element_count];
  for (i = 0; (i < test_element_count); i = 1 + i) {
    uint32_array[i] = test_element_count - i;
  };

  // sort
  quicksort(uint32_less_p, uint32_swapper, uint32_array, 0, test_element_count - 1);

  // display results
  for (i = 0; (i < test_element_count); i = 1 + i) {
    printf("%u", uint32_array[i]);
  };
  return 0;
};
```

# random
[xoshiro256**](http://xoshiro.di.unimi.it/) and xoshiro256+ implementation (article about the algorithm [here](https://nullprogram.com/blog/2017/09/21/)).
unbiased bounding ([lemire](https://arxiv.org/abs/1805.10941), [o'neill](https://www.pcg-random.org/posts/bounded-rands.html)).

## usage
```c
#include <inttypes.h>
#include "sph/random.h"
#include "sph/random.c"

sph_random_state_t s = sph_random_state_new(11223344);

uint64_t a = sph_random_u64(&s);
double b = sph_random_f64(&s);
uint64_t c = sph_random_u64_bounded(&s, 10);

uint64_t a_array[100];
double b_array[100];
uint64_t c_array[100];
sph_random_u64_array(&s, 100, a_array);
sph_random_f64_array(&s, 100, b_array);
sph_random_u64_bounded_array(&s, 100, 10, c_array);

```

## api
### routines
```
sph_random_f64 :: sph_random_state_t*:state -> double
sph_random_f64_array :: sph_random_state_t*:state size_t:size double*:out -> void
sph_random_state_new :: uint64_t:seed -> sph_random_state_t
sph_random_u64 :: sph_random_state_t*:state -> uint64_t
sph_random_u64_array :: sph_random_state_t*:state size_t:size uint64_t*:out -> void
sph_random_u64_bounded :: sph_random_state_t*:state uint64_t:range -> uint64_t
sph_random_u64_bounded_array :: sph_random_state_t*:state uint64_t:range size_t:size uint64_t*:out -> void
```

### macros
```
rotl(x, k)
sph_random_f64_from_u64(a)
```

### types
```
sph_random_state_t: struct
  data: array uint64_t 4
```

# ikv
indent-key-value - file format and hashtable type for possibly nested named arrays.
use case: text configuration file format.
the parser is written so that it should be relatively easy to add custom value types.

depends on stdio.h, inttypes.h, murmur3.c, sph/status.c and sph/hashtable.c.
it uses getline which with gcc needs ``#define _GNU_SOURCE`` before including stdio.h.

## the file format
* one key/value association per line
* key and values separated by space
* values can be integers, reals or strings
* associations can be nested by using two spaces of indentation in lines subsequent to keys that have no value

~~~
key1 0 1 2 3 4
key2 0.1 5 6.33 7 8
key3 string1 string2 string3
nest1
  nest11
    nest111 string4 string5
  nest12 9
  nest13 string6
key4 string7
~~~

## code example
~~~
#define _GNU_SOURCE
#include <stdio.h>
#include <inttypes.h>
#include <sph/status.h>
#include <sph/hashtable.c>
#include <murmur3.c>
#include <sph/ikv.h>
#include <sph/ikv.c>

int main() {
  ikv_t a;
  ikv_t b;
  ikv_value_t* value;
  status_declare;
  status_i_require(ikv_new(100, &a));
  /* read/write */
  status_require(ikv_read_file("other/ikv-test-data", a));
  ikv_write_file(a, "tmp/ikv-test");
  ikv_free_all(a);
  status_i_require(ikv_new(100, &a));
  status_require(ikv_read_file("tmp/ikv-test", a));
  /* access top level */
  value = ikv_get(a, "key4");
  printf("key4 string: %s\n", ikv_value_get_string(value, 0));
  value = ikv_get(a, "key3");
  printf("key3 string: %s\n", ikv_value_get_string(value, 2));
  /* access nested */
  value = ikv_get(a, "nest1");
  b = ikv_value_get_ikv(value);
  value = ikv_get(b, "nest11");
  value = ikv_get((ikv_value_get_ikv(value)), "nest111");
  printf("nest111 string: %s\n", ikv_value_get_string(value, 0));
  value = ikv_get(b, "nest12");
  printf("nest12 integer %lu\n", ikv_value_get_integer(value, 0));
  ikv_free_all(a);
exit:
  return status.id;
}
~~~

## api
### functions
~~~
ikv_floats_new :: size_t:size ikv_float_t**:out -> status_t
ikv_free_all :: ikv_t:a -> void
ikv_hash_64 :: ikv_key_t*:key size_t:size -> uint64_t
ikv_integers_new :: size_t:size ikv_integer_t**:out -> status_t
ikv_read_file :: ikv_string_t*:path ikv_t:ikv -> status_t
ikv_read_indent :: FILE*:file ikv_read_value_t:read_value ikv_t:ikv -> status_t
ikv_read_value :: char*:line size_t:size ikv_value_t*:value -> status_t
ikv_write_file :: ikv_t:a ikv_string_t*:path -> void
ikv_write_file_direct :: ikv_t:a FILE*:file ikv_nesting_t:nesting -> void
~~~

## macros
~~~
ikv_equal(a, b)
ikv_float_t
ikv_integer_t
ikv_key_t
ikv_max_keysize
ikv_max_nesting
ikv_memory_error
ikv_nesting_t
ikv_s_group_ikv
ikv_s_id_file_open_failed
ikv_s_id_full
ikv_s_id_memory
ikv_string_t
ikv_type_floats
ikv_type_ikv
ikv_type_integers
ikv_type_strings
ikv_type_t
ikv_value_get_float(ikv_value_t*:a, index)
ikv_value_get_ikv(a)
ikv_value_get_integer(a, index)
ikv_value_get_string(a, index)
~~~

## types
~~~
ikv_read_value_t: char* size_t ikv_value_t* -> status_t
ikv_value_t: struct
  type: ikv_type_t
  size: ikv_integer_t
  data: void*
~~~

## possible enhancements
* read/write from/to strings
