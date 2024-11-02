void ikv_free_all(ikv_t a) {
  /* hash including all sub hashes and data */
  size_t i;
  size_t j;
  ikv_value_t b;
  for (i = 0; (i < a.size); i += 1) {
    if (!a.flags) {
      continue;
    };
    b = (a.values)[i];
    if ((ikv_type_integers == b.type) || (ikv_type_floats == b.type)) {
      free((b.data));
    } else if (ikv_type_strings == b.type) {
      for (j = 0; (j < b.size); j += 1) {
        free((((ikv_string_t**)(b.data))[j]));
      };
      free((b.data));
    } else if (ikv_type_ikv == b.type) {
      ikv_free_all((*((ikv_t*)(b.data))));
    };
  };
  ikv_free(a);
}
void ikv_write_file_direct(ikv_t a, FILE* file, ikv_nesting_t nesting) {
  size_t i;
  size_t j;
  ikv_value_t b;
  for (i = 0; (i < a.size); i += 1) {
    if (!(a.flags)[i]) {
      continue;
    };
    for (j = 0; (j < nesting); j += 1) {
      fprintf(file, "  ");
    };
    fprintf(file, "%s", (((ikv_key_t**)(a.keys))[i]));
    b = (a.values)[i];
    if (ikv_type_integers == b.type) {
      for (j = 0; (j < b.size); j += 1) {
        fprintf(file, " %u", (ikv_value_get_integer((&b), j)));
      };
      fprintf(file, "\n");
    } else if (ikv_type_floats == b.type) {
      for (j = 0; (j < b.size); j += 1) {
        fprintf(file, " %f", (ikv_value_get_float((&b), j)));
      };
      fprintf(file, "\n");
    } else if (ikv_type_strings == b.type) {
      for (j = 0; (j < b.size); j += 1) {
        fprintf(file, " %s", (ikv_value_get_string((&b), j)));
      };
      fprintf(file, "\n");
    } else if (ikv_type_ikv == b.type) {
      fprintf(file, "\n");
      ikv_write_file_direct((ikv_value_get_ikv((&b))), file, (nesting + 1));
    };
  };
}
void ikv_write_file(ikv_t a, ikv_string_t* path) {
  FILE* file;
  file = fopen(path, "w");
  ikv_write_file_direct(a, file, 0);
  fclose(file);
}
status_t ikv_floats_new(size_t size, ikv_float_t** out) {
  status_declare;
  void* a;
  a = calloc(size, (sizeof(ikv_float_t)));
  if (a) {
    *out = a;
  } else {
    ikv_memory_error;
  };
exit:
  status_return;
}
status_t ikv_integers_new(size_t size, ikv_integer_t** out) {
  status_declare;
  void* a;
  a = calloc(size, (sizeof(ikv_integer_t)));
  if (a) {
    *out = a;
  } else {
    ikv_memory_error;
  };
exit:
  status_return;
}

/** value of i is the index of the space after key */
status_t ikv_read_value(char* line, size_t size, ikv_value_t* value) {
  void* a;
  size_t count;
  size_t i;
  size_t i_start;
  char* line_rest;
  ikv_string_t* string;
  size_t value_i;
  status_declare;
  /* detect format */
  i_start = 0;
  i = 1;
  value->type = ikv_type_integers;
  if (('0' == line[i]) || ('1' == line[i]) || ('2' == line[i]) || ('3' == line[i]) || ('4' == line[i]) || ('5' == line[i]) || ('6' == line[i]) || ('7' == line[i]) || ('8' == line[i]) || ('9' == line[i])) {
    i += 1;
    while ((i < size)) {
      if ('.' == line[i]) {
        value->type = ikv_type_floats;
        break;
      } else if (' ' == line[i]) {
        break;
      } else {
        i += 1;
      };
    };
  } else {
    value->type = ikv_type_strings;
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
  if (ikv_type_strings == value->type) {
    /* is string */
    a = malloc((count * sizeof(ikv_string_t*)));
    if (!a) {
      ikv_memory_error;
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
            a = malloc((((i - i_start) + 1) * sizeof(ikv_string_t*)));
            if (!a) {
              ikv_memory_error;
            };
            string = a;
            memcpy(string, (line + i_start), (i - i_start));
            string[(i_start - i)] = 0;
            ((ikv_string_t**)(value->data))[value_i] = string;
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
    status_require(((ikv_type_integers == value->type) ? ikv_integers_new(count, ((ikv_integer_t**)(&(value->data)))) : ikv_floats_new(count, ((ikv_float_t**)(&(value->data))))));
    value->size = count;
    value_i = 0;
    /* matches space prefixes, -1 to leave room for the last number */
    while ((i < (size - 1))) {
      if (' ' == line[i]) {
        if (ikv_type_integers == value->type) {
          ((ikv_integer_t*)(value->data))[value_i] = strtol((line + i + 1), (&line_rest), 10);
        } else {
          ((ikv_float_t*)(value->data))[value_i] = strtod((line + i + 1), (&line_rest));
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
status_t ikv_read_indent(FILE* file, ikv_read_value_t read_value, ikv_t ikv) {
  void* a;
  size_t i;
  ikv_key_t* key;
  char* line;
  size_t size;
  size_t line_alloc_size;
  ikv_value_t value;
  ikv_t* nested_ikv;
  ikv_nesting_t nesting;
  ikv_key_t* nested_keys[ikv_max_nesting];
  ikv_t nested_ikvs[ikv_max_nesting];
  status_declare;
  line = 0;
  nested_ikvs[0] = ikv;
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
          ikv_memory_error;
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
      a = malloc((sizeof(ikv_t)));
      if (!a) {
        ikv_memory_error;
      };
      nested_ikv = a;
      status_i_require((ikv_new(100, nested_ikv)));
      nested_ikvs[(nesting + 1)] = *nested_ikv;
      value.type = ikv_type_ikv;
      value.size = 100;
      value.data = nested_ikv;
      if (!ikv_set((nested_ikvs[nesting]), key, value)) {
        free(nested_ikv);
        status_set_goto(ikv_s_group_ikv, ikv_s_id_full);
      };
    } else {
      status_require((read_value((line + i), (size - i), (&value))));
      /* insert key/value */
      if (!ikv_set((nested_ikvs[nesting]), (nested_keys[nesting]), value)) {
        status_set_goto(ikv_s_group_ikv, ikv_s_id_full);
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
status_t ikv_read_file(ikv_string_t* path, ikv_t ikv) {
  status_declare;
  FILE* file;
  file = fopen(path, "r");
  if (!file) {
    status_set_goto(ikv_s_group_ikv, ikv_s_id_file_open_failed);
  };
  status_require((ikv_read_indent(file, ikv_read_value, ikv)));
exit:
  if (file) {
    fclose(file);
  };
  status_return;
}
