
#ifndef sph_ikv_h
#define sph_ikv_h

/* string-array-hash - a file format and hashtable type for named arrays, possibly nested.
it uses getline which needs #define _GNU_SOURCE before including stdio.h.
   depends on murmu3.c */
#include <inttypes.h>
#include <sph/status.h>
#include <sph/hashtable.h>

#ifndef ikv_integer_t
#define ikv_integer_t uintmax_t
#endif
#ifndef ikv_float_t
#define ikv_float_t double
#endif
#ifndef ikv_string_t
#define ikv_string_t uint8_t
#endif
#ifndef ikv_key_t
#define ikv_key_t uint8_t
#endif
#ifndef ikv_type_t
#define ikv_type_t uint8_t
#endif
#ifndef ikv_nesting_t
#define ikv_nesting_t uint8_t
#endif
#ifndef ikv_max_keysize
#define ikv_max_keysize 128
#endif
#ifndef ikv_max_nesting
#define ikv_max_nesting 8
#endif

#define ikv_type_ikv 1
#define ikv_type_integers 2
#define ikv_type_floats 3
#define ikv_type_strings 4
#define ikv_s_group_ikv "ikv"
#define ikv_s_id_file_open_failed 1
#define ikv_s_id_memory 2
#define ikv_s_id_full 3
#define ikv_memory_error status_set_goto(ikv_s_group_ikv, ikv_s_id_memory)
#define ikv_equal(a, b) (0 == strncmp(a, b, ikv_max_keysize))
#define ikv_value_get_string(a, index) ((ikv_string_t**)(a->data))[index]
#define ikv_value_get_integer(a, index) ((ikv_integer_t*)(a->data))[index]
#define ikv_value_get_float(a, index) ((ikv_float_t*)(a->data))[index]
#define ikv_value_get_ikv(a) *((ikv_t*)(a->data))
uint64_t ikv_hash_64(ikv_key_t* key, size_t size) {
  uint64_t a[2];
  MurmurHash3_x64_128(key, (strlen(key)), 0, a);
  return ((a[0]));
}
typedef struct {
  ikv_type_t type;
  ikv_integer_t size;
  void* data;
} ikv_value_t;
sph_hashtable_declare_type(ikv, ikv_key_t*, ikv_value_t, ikv_hash_64, ikv_equal, 2);
typedef status_t (*ikv_read_value_t)(char*, size_t, ikv_value_t*);
void ikv_free_all(ikv_t a);
void ikv_write_file_direct(ikv_t a, FILE* file, ikv_nesting_t nesting);
void ikv_write_file(ikv_t a, ikv_string_t* path);
status_t ikv_floats_new(size_t size, ikv_float_t** out);
status_t ikv_integers_new(size_t size, ikv_integer_t** out);
status_t ikv_read_value(char* line, size_t size, ikv_value_t* value);
status_t ikv_read_indent(FILE* file, ikv_read_value_t read_value, ikv_t ikv);
status_t ikv_read_file(ikv_string_t* path, ikv_t ikv);
#endif
