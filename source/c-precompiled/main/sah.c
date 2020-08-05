/* string-array-hash - a file format and hashtable type for named arrays, possibly nested.
depends on stdio.h, inttypes.h, murmur3.c, sph/status.c and sph/hashtable.c.
it uses getline which needs #define _GNU_SOURCE before including stdio.h */
#ifndef sah_integer_t
#define sah_integer_t uintmax_t
#endif
#ifndef sah_float_t
#define sah_float_t double
#endif
#ifndef sah_nesting_t
#define sah_nesting_t uint8_t
#endif
#ifndef sah_max_keysize
#define sah_max_keysize 128
#endif
#ifndef sah_max_nesting
#define sah_max_nesting 8
#endif
#define sah_type_sah 1
#define sah_type_integers 2
#define sah_type_floats 3
#define sah_type_strings 4
#define sah_equal(a, b) (0 == strncmp(a, b, sah_max_keysize))
#define sah_s_group_sah "sah"
#define sah_s_id_file_open_failed 1
#define sah_s_id_memory 2
#define sah_s_id_full 3
#define sah_memory_error status_set_goto(sah_s_group_sah, sah_s_id_memory)
uint64_t sah_hash_64(uint8_t* key, size_t size) {
  uint64_t a[2];
  MurmurHash3_x64_128(key, (strlen(key)), 0, a);
  return ((a[0]));
}
typedef struct {
  uint8_t type;
  sah_integer_t size;
  void* data;
} sah_value_t;
hashtable_declare_type(sah, uint8_t*, sah_value_t, sah_hash_64, sah_equal, 2);
void sah_free_all(sah_t a) {
  /* hash including all sub hashes and data */
  size_t i;
  size_t j;
  sah_value_t b;
  for (i = 0; (i < a.size); i += 1) {
    if (!a.flags) {
      continue;
    };
    b = (a.values)[i];
    if ((sah_type_integers == b.type) || (sah_type_floats == b.type)) {
      free((b.data));
    } else if (sah_type_strings == b.type) {
      for (j = 0; (j < b.size); j += 1) {
        free((((uint8_t**)(b.data))[j]));
      };
      free((b.data));
    } else if (sah_type_sah == b.type) {
      sah_free_all((*((sah_t*)(b.data))));
    };
  };
  sah_free(a);
}
void sah_write_file_direct(sah_t a, FILE* file, sah_nesting_t nesting) {
  size_t i;
  size_t j;
  sah_value_t b;
  for (i = 0; (i < a.size); i += 1) {
    if (!(a.flags)[i]) {
      continue;
    };
    for (j = 0; (j < nesting); j += 1) {
      fprintf(file, "  ");
    };
    fprintf(file, "%s", (((uint8_t**)(a.keys))[i]));
    b = (a.values)[i];
    if (sah_type_integers == b.type) {
      for (j = 0; (j < b.size); j += 1) {
        fprintf(file, " %u", (((sah_integer_t*)(b.data))[j]));
      };
      fprintf(file, "\n");
    } else if (sah_type_floats == b.type) {
      for (j = 0; (j < b.size); j += 1) {
        fprintf(file, " %f", (((sah_integer_t*)(b.data))[j]));
      };
      fprintf(file, "\n");
    } else if (sah_type_strings == b.type) {
      for (j = 0; (j < b.size); j += 1) {
        fprintf(file, " %s", (((uint8_t**)(b.data))[j]));
      };
      fprintf(file, "\n");
    } else if (sah_type_sah == b.type) {
      fprintf(file, "\n");
      sah_write_file_direct((*((sah_t*)(b.data))), file, (nesting + 1));
    };
  };
}
void sah_write_file(sah_t a, uint8_t* path) {
  FILE* file;
  file = fopen(path, "w");
  sah_write_file_direct(a, file, 0);
}
typedef status_t (*sah_read_value_t)(char*, size_t, sah_value_t*);
status_t sah_floats_new(size_t size, sah_float_t** out) {
  status_declare;
  void* a;
  a = calloc(size, (sizeof(sah_float_t)));
  if (a) {
    *out = a;
  } else {
    sah_memory_error;
  };
exit:
  status_return;
}
status_t sah_integers_new(size_t size, sah_integer_t** out) {
  status_declare;
  void* a;
  a = calloc(size, (sizeof(sah_integer_t)));
  if (a) {
    *out = a;
  } else {
    sah_memory_error;
  };
exit:
  status_return;
}
/** value of i is the index of the space after key */
status_t sah_read_value(char* line, size_t size, sah_value_t* value) {
  void* a;
  size_t count;
  size_t i;
  size_t i_start;
  char* line_rest;
  uint8_t* string;
  size_t value_i;
  status_declare;
  /* detect format */
  i_start = 0;
  i = 1;
  value->type = sah_type_integers;
  if (('0' == line[i]) || ('1' == line[i]) || ('2' == line[i]) || ('3' == line[i]) || ('4' == line[i]) || ('5' == line[i]) || ('6' == line[i]) || ('7' == line[i]) || ('8' == line[i]) || ('9' == line[i])) {
    i += 1;
    while ((i < size)) {
      if ('.' == line[i]) {
        value->type = sah_type_floats;
        break;
      } else if (' ' == line[i]) {
        break;
      } else {
        i += 1;
      };
    };
  } else {
    value->type = sah_type_strings;
  };
  /* value of i still before the space. count array elements */
  i = i_start;
  count = 0;
  while ((i < size)) {
    if (' ' == line[i]) {
      count += 1;
    };
    i += 1;
  };
  i = i_start;
  if (sah_type_strings == value->type) {
    /* is string */
    a = malloc((count * sizeof(uint8_t*)));
    if (!a) {
      sah_memory_error;
    };
    value->data = a;
    value->size = count;
    value_i = 0;
    while ((i_start < (size - 1))) {
      if (' ' == line[i_start]) {
        /* read until next space or eos */
        i_start += 1;
        i = i_start;
        while ((i < size)) {
          if ((' ' == line[i]) || (i == (size - 1))) {
            if (i == (size - 1)) {
              i += 1;
            };
            a = malloc((((i - i_start) + 1) * sizeof(uint8_t*)));
            if (!a) {
              sah_memory_error;
            };
            string = a;
            memcpy(string, (line + i_start), (i - i_start));
            string[(i_start - i)] = 0;
            ((uint8_t**)(value->data))[value_i] = string;
            value_i += 1;
            break;
          };
          i += 1;
        };
      };
      i_start += 1;
    };
  } else {
    /* is number */
    status_require(((sah_type_integers == value->type) ? sah_integers_new(count, ((sah_integer_t**)(&(value->data)))) : sah_floats_new(count, ((sah_float_t**)(&(value->data))))));
    value->size = count;
    value_i = 0;
    /* matches space prefixes, -1 to leave room for the last number */
    while ((i < (size - 1))) {
      if (' ' == line[i]) {
        if (sah_type_integers == value->type) {
          ((sah_integer_t*)(value->data))[value_i] = strtol((line + i + 1), (&line_rest), 10);
        } else {
          ((sah_float_t*)(value->data))[value_i] = strtod((line + i + 1), (&line_rest));
        };
        i = (line_rest - line);
        value_i += 1;
      } else {
        i += 1;
      };
    };
  };
exit:
  status_return;
}
/** reads keys and indentation, creating nested hashtables for keys as required and
   calls read-value to get the value from the rest of a line.
   generic indent key-value reader - custom read-value functions can read custom value formats */
status_t sah_read_indent(FILE* file, sah_read_value_t read_value, sah_t sah) {
  void* a;
  size_t i;
  uint8_t value_type;
  size_t i_start;
  uint8_t* key;
  char* line;
  size_t size;
  size_t line_alloc_size;
  sah_value_t value;
  sah_t* nested_sah;
  uint8_t nesting;
  uint8_t* nested_keys[sah_max_nesting];
  sah_t nested_sahs[sah_max_nesting];
  status_declare;
  line = 0;
  nested_sahs[0] = sah;
  while (!(-1 == getline((&line), (&line_alloc_size), file))) {
    i = 0;
    size = strlen(line);
    if (0 == size) {
      continue;
    };
    /* remove newline */
    if ('\n' == line[(size - 1)]) {
      size = (size - 1);
      line[size] = 0;
    };
    /* skip indent */
    while (((i < size) && (' ' == line[i]))) {
      i += 1;
    };
    nesting = (i ? (i / 2) : i);
    /* get key */
    while ((i < size)) {
      if ((' ' == line[i]) || (i == (size - 1))) {
        /* up to space or rest of line */
        if (i == (size - 1)) {
          i += 1;
        };
        a = malloc(((i - (2 * nesting)) + 1));
        if (!a) {
          sah_memory_error;
        };
        key = a;
        memcpy(key, ((2 * nesting) + line), (i - (2 * nesting)));
        key[i] = 0;
        nested_keys[nesting] = key;
        /* keep i be before the first space */
        break;
      } else {
        i += 1;
      };
    };
    /* keys without value start nesting */
    if (i == size) {
      a = malloc((sizeof(sah_t)));
      if (!a) {
        sah_memory_error;
      };
      nested_sah = a;
      status_i_require((sah_new(100, nested_sah)));
      nested_sahs[(nesting + 1)] = *nested_sah;
      value.type = sah_type_sah;
      value.size = 100;
      value.data = nested_sah;
      if (!sah_set((nested_sahs[nesting]), key, value)) {
        free(nested_sah);
        status_set_goto(sah_s_group_sah, sah_s_id_full);
      };
    } else {
      status_require((read_value((line + i), (size - i), (&value))));
      /* insert key/value */
      if (!sah_set((nested_sahs[nesting]), (nested_keys[nesting]), value)) {
        status_set_goto(sah_s_group_sah, sah_s_id_full);
      };
    };
  };
exit:
  if (line) {
    free(line);
  };
  status_return;
}
/** read key/value associations from file and add to hashtable.
   values can be integer/float/string arrays.
   file contains one key-value association per line.
   by using two space indentation in lines subsequent to keys, associations can be nested.
     key int ...
     key decimal int/decimal ...
     key
       key int ...
   key s string ... */
status_t sah_read_file(uint8_t* path, sah_t sah) {
  status_declare;
  FILE* file;
  file = fopen(path, "r");
  if (!file) {
    status_set_goto(sah_s_group_sah, sah_s_id_file_open_failed);
  };
  status_require((sah_read_indent(file, sah_read_value, sah)));
exit:
  if (file) {
    fclose(file);
  };
  status_return;
}
