# sph-sc-lib

various minimalistic standalone c utility libraries.
c code is in source/c-precompiled. sc versions are in source/sc. the libraries are developed in sc and then translated to normal, readable c formatted with clang-format

# included libraries
* [futures](#futures): fine-grained parallelism with objects that can be waited on for results
* [hashtable](#hashtable): hash-tables for any key/value type
* [i-array](#i-array): a fixed size array type with variable length content that makes iteration easier to code
* [memreg](#memreg): track heap memory allocations in function scope
* [mi-list](#mi-list): a basic, macro-based linked list
* [queue](#queue): a queue for any data type
* [quicksort](#quicksort): a generic implementation for arrays of any type
* [set](#set): sets for any key/value type
* [spline-path](#spline-path): interpolated 2d paths between given points
* [status](#status): return-status and error handling using a tiny object with status/error id and source library id
* [thread-pool](#thread-pool): a task queue with pthread threads and wait conditions to pause inactive threads
* experimental
  * one: miscellaneous helpers
  * guile: a few helpers for working with guile

# license
code is under gpl3+, documentation under cc-by-nc.

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
    status_set_both_goto(group_id, error_id);
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

## bindings
```
status_is_failure
status_goto
status_group_undefined
status_id_success
status_declare
status_require(expression)
status_reset
status_set_id_goto(status_id)
status_set_group_goto(group_id)
status_set_both(group_id, status_id)
status_set_both_goto(group_id, status_id)
status_is_success
```

# memreg
track memory allocations locally on the stack and free all allocations up to point easily

```c
#include "memreg.c"

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

## memreg_heap
``memreg_heap.c`` is similar to the previously mentioned memreg but uses a special i-array based heap allocated array type ``memreg_register_t`` that can be passed between functions. also supports register sizes given by variables.

```c
memreg_heap_declare(allocations);
if(memreg_heap_allocate(2, allocations)) {
  // allocation error
}
memreg_heap_add(allocations, &variable1);
memreg_heap_add(allocations, &variable2);
memreg_heap_free(allocations);
```

# hashtable
hash-table data structures for custom key and value types, created by a macro that declares hash-table types and corresponding functions.

## dependencies
* the c standard library (stdlib.h and inttypes.h)

## implementation
* linear probing for resolving collisions
* three arrays (flags, keys, values)
* no empty key needed, because the flags array is used to check existence
* no null value needed, because hashtable_get returns addresses
* hashtable-equal and hashtable-hash are macros for inline code
* first version under 150 lines

## usage examples
```
#include "hashtable.c";
```

where the file is in the load path or the same directory.

### declaration
```
hashtable_declare_type(mytypename, uint64_t, uint32_t);
```

the default hash functions use integers

### creation
```
mytype_t ht;
mytype_new(200, &ht);
```

returns 1 on success or 0 if the memory allocation failed.

size does not automatically grow, so a new hash-table would need to be created should the specified size later turn out to be insufficient.

### insert
```c
mytype_set(ht, 44, 5);
```

returns the address of the added or already included element, 0 if there is no space left in the set.

### search
```c
mytype_get(ht, 44);
```

### removal
```c
mytype_remove(ht, 44);
```

returns 1 if the element was removed, 0 if it was not found.

### deallocation
```c
mytype_destroy(ht);
```

## configuration options
configuration is done by defining certain macro variables before including the hashtable source code. multiple configurations can be used by including hashtable.c multiple times, each time setting configuration macro variables beforehand and calling the corresponding type declares afterwards.
the following macro variables can be set, here shown with their default values:

```c
#define hashtable_equal(key_a, key_b) (key_a == key_b)
#define hashtable_hash(key, hashtable) (key % hashtable.size)
#define hashtable_size_factor 2
```

example of declaring typse with different equality functions
```c
#define hashtable_equal(key_a, key_b) (key_a == key_b)
#include "hashtable.c";
hashtable_declare_type(mytypename, uint64_t, uint32_t);
#define hashtable_equal(key_a, key_b) (key_a.member == key_b.member)
#include "hashtable.c";
hashtable_declare_type(mytypename2, mystruct_t, uint32_t);
```

# set
a macro that defines set types for arbitrary value types.

* can easily deal with millions of values on common hardware
* linear probing for collision resolve
* compared to hashtable.c, set.c uses less than half of the space and operations are faster (about 20% in first tests)
* insert/delete/search should all be o(1)
* the set size does not automatically grow. a new set has to be created should the specified size later turn out to be insufficient
* the default hash functions work with integers

## dependencies
* the c standard library (stdlib.h and inttypes.h)

## usage examples
where the file is in the load path or in the same directory.
~~~
#include "set.c";
sph_set_declare_type(myset, int);
void main() {
  myset_t a;
  if(myset_new(3, &a)) {
    // memory allocation failed
  }
  myset_add(a, 3);
  myset_add(a, 5);
  myset_remove(a, 3);
  myset_get(a, 5);
  myset_free(a);
}

~~~

sph_set_declare_type adds the following functions, where "name" is the first argument passed to sph_set_declare_type
~~~
// 0 on success, 1 on memory allocation error
uint8_t name##_new(size_t min_size, name##_t* result);

// returns the address of the value or 0 if it was not found.
// if sph_set_allow_empty_value is true and the value is included, then address points to a sph_set_true_value
value_type* name##_get(name##_t a, value_type value);

// returns the address of the value or 0 if no space is left
value_type* name##_add(name##_t a, value_type value);

// returns 0 if the element was removed, 1 if it was not found
uint8_t name##_remove(name##_t a, value_type value);

void name##_free(name##_t a);
~~~

## configuration options
before including set.c, the following macros can be defined, which must be expressions. redefine and reinclude for varying configuration.
the following shows the defaults.
~~~
#define sph_set_hash(value, hashtable_size) (value % hashtable_size)
#define sph_set_equal(value_a, value_b) (value_a == value_b)
#define sph_set_allow_empty_value 1
#define sph_set_empty_value 0
#define sph_set_true_value 1
#define sph_set_size_factor 2
~~~

### exclude empty value
by default, the empty value is a valid value to be included in a set. but as an optimisation, to make operations a tiny bit faster, this can be disabled by setting the macro variable "sph_set_allow_empty_value" to zero before inclusion.
the empty value can then not be part of the set; it wont be found.

### memory usage
by default, the memory allocated for the set is at least double the number of elements it is supposed to store (possibly rounded to a next higher prime).
a lower set size factor approaching 1 leads to more efficient memory usage, with 1 being the lowest possible, where only as much space as the elements need by themselves is allocated.
the downside is that the insert/delete/search performance is more likely to approach and reach o(n).

## modularity and implementation
declared "name##_t" set types are structures (.size, .values). "values" is a one-dimensional array that stores values at indices determined by a hash function.
if sph_set_allow_empty_value is true, values start at index 1 and index 0 is sph_set_true_value if the empty value is in the set.
if sph_set_allow_empty_value is false, values start at index 0.

## possible enhancement
* pass (hash, equal, empty-value) to the main type declaration macro

# i-array
a fixed size array with variable length content that makes iteration easier to code. it is used similar to a linked list and can replace linked lists in many instances. the overhead is small because it is only four pointers.
most bindings are generic macros that will work on all i-array types. i_array_add and i_array_forward go from left to right

## dependencies
* the c standard library (stdlib.h)

## usage example
```c
// arguments: custom_name, element_type
i_array_declare_type(my_type, int);
my_type_t a;
if(my_type_new(4, &a)) {
  // memory allocation error
}
i_array_add(a, 1);
i_array_add(a, 2);
while(i_array_in_range(a)) { i_array_get(a); }
i_array_free(a);
```

## bindings
### macros
```c
i_array_add(a, value)
i_array_clear(a)
i_array_declare(a, type)
i_array_declare_type(name, element_type)
i_array_forward(a)
i_array_free(a)
i_array_get(a)
i_array_get_at(a, index)
i_array_in_range(a)
i_array_length(a)
i_array_max_length(a)
i_array_remove(a)
i_array_rewind(a)
i_array_set_null(a)
```

### routines
```c
i_array_allocate_custom_##name(length, allocator, result)
i_array_allocate_##name(length, result)
```

# mi-list
a basic linked list with custom element types.

## dependencies
* the c standard library (stdlib.h and inttypes.h)

## usage example
```c
// include to define a list type
#define mi_list_name_prefix mylist
#define mi_list_element_t uint64_t
#include "mi_list.c"

// include again for another list type with a different element type
#define mi_list_name_prefix u32_list
#define mi_list_element_t uint32_t
#include mi_list.c

// new empty list creation
u32_list_t* a = 0;

// add elements
u32_list_add(a, 3);
u32_list_add(a, 4);

// iterate over all list elements
u32_list_t* rest = a;
while(rest) {
  uint32_t element = mi_list_first(rest);
  rest = mi_list_rest(rest);
}
```

## bindings
```c
{prefix}_add
{prefix}_destroy
{prefix}_drop
{prefix}_length
{prefix}_struct
{prefix}_t
mi_list_first
mi_list_first_address
mi_list_rest
```

## type
the mi-list type is defined by mi-list.c on inclusion as follows

```c
typedef struct mi_list_name_prefix##_struct {
  struct mi_list_name_prefix##_struct *link;
  mi_list_element_t data;
} mi_list_t;
```

# queue
a fifo queue with the operations enqueue and dequeue that can enqueue structs of mixed types.
for elements what is needed is a struct with a queue_node_t field with a custom name. a queue_node_t object to be added must not already be in the queue.
the queue does not need to allocate memory. depends on queue.c


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

# thread-pool
```c
#include <inttypes.h>
#include "queue.c"
#include "thread-pool.c"

void work(thread_pool_task_t* task) {
  // ... do things in parallel ...
  // task can be freed here
  free(task);
};

int main() {
  int error;
  thread_pool_t pool;
  thread_pool_task_t* task;
  error = thread_pool_new(10, &pool);
  if(error) return error;
  task = malloc(sizeof(thread_pool_task_t));
  if(!task) return 1;
  task->f = work;
  thread_pool_enqueue(a, task);
  // when the thread pool is not needed anymore then all threads can be closed.
  // arguments: thread_pool, no_wait, discard_queue
  thread_pool_finish(&pool, 0, 0);
  return 0;
}
```

# futures
provides task objects with functions that are executed in parallel with a thread-pool.
calling touch on an object waits for its completion and returns its result.
depends on thread-pool.c

```c
#include <inttypes.h>
#include "queue.c"
#include "thread-pool.c"
#include "futures.c"

void* future_work(void* data) {
  // return value is a void pointer.
  // just for example this returns a new object. data could be modified and the pointer returned
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
gcc "$c/test/thread-pool.c" -o temp/test-thread-pool -lpthread -D _DEFAULT_SOURCE
```

# spline-path
spline-path creates discrete 2d paths with segments that interpolate between some given points.
paths can be composed to construct more complex paths.

implemented segment types and interpolation methods
* linear: draws lines between points
* bezier: 4 point cubic bezier interpolation
* move: gap, moves the point the next segment starts on
* constant: repeats the last value as a flat line to infinity
* path: another spline path as a segment

features
* extremely fast through only supporting 2d, with only selected and optimised interpolators, limited segment count and sampling portions of paths at once instead of only single points
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
#include "quicksort.c"

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
[xoshiro256plus](http://xoshiro.di.unimi.it/) implementation (also referenced [here](https://nullprogram.com/blog/2017/09/21/)). fills an array with random values. also includes a macro to define custom-type random functions. the sph_ prefix was added because "random" is defined in the standard library

```c
sph_random_state_t sph_random_state_new(u64 seed);
void sph_random(sph_random_state_t* state, u32 size, f64* out);
#define define_sph_random(name, size_type, data_type, transfer)
```

```c
#include "types.c"
#include "random-h.c"
#include "random.c"

sph_random_state_t s;
f64 out[100];
s = sph_random_state_new(11223344);
sph_random((&s), 100, out);
```

```c
define_sph_random(sph_random, u32, f64, (f64_from_u64(result_plus)));
```
