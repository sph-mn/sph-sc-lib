#define read_paths_display_error(format, ...) fprintf(stderr, "error: %s:%d " format "\n", __func__, __LINE__, __VA_ARGS__)
#define read_paths_memory_error() do { read_paths_display_error("%s", "memory allocation failed"); return 0; } while (0)

char* read_paths(char delimiter, char*** paths, size_t* paths_used) {
  size_t data_size;
  size_t data_used;
  char* data;
  char* p;
  char* end;
  char* start;
  size_t paths_count;
  size_t paths_cap;
  size_t i;
  ssize_t r;
  data_size = paths_data_size_min;
  data_used = 0;
  data = malloc(data_size);
  if (!data) read_paths_memory_error();
  for (;;) {
    if (data_used == data_size) {
      size_t new_data_size;
      char* new_data;
      new_data_size = data_size * 2;
      new_data = realloc(data, new_data_size);
      if (!new_data) {
        free(data);
        read_paths_memory_error();
      }
      data = new_data;
      data_size = new_data_size;
    }
    r = read(0, data + data_used, data_size - data_used);
    if (r <= 0) break;
    data_used = data_used + (size_t)r;
  }
  if (!data_used) {
    free(data);
    return 0;
  }
  p = data;
  end = data + data_used;
  start = p;
  paths_count = 0;
  while (p < end) {
    if (*p == delimiter) {
      if (p > start) paths_count = paths_count + 1;
      start = p + 1;
    }
    p += 1;
  }
  paths_capacity = paths_count;
  if (!paths_capacity) paths_capacity = 1;
  *paths = malloc(paths_capacity * sizeof(char*));
  if (!*paths) {
    free(data);
    read_paths_memory_error();
  }
  *paths_used = 0;
  p = data;
  start = p;
  while (p < end) {
    if (*p == delimiter) {
      if (p > start) {
        *p = 0;
        (*paths)[*paths_used] = start;
        *paths_used = *paths_used + 1;
      }
      start = p + 1;
    }
    p += 1;
  }
  return data;
}

#define simple_option_parser_declare(option_array_name) simple_option_parser_option_t option_array_name[256] = {0}
#define simple_option_parser_set_option(option_array, option_character, requires_value_flag) do { \
  uint8_t simple_option_parser_index; \
  simple_option_parser_index = (uint8_t)(unsigned char)(option_character); \
  (option_array)[simple_option_parser_index].is_supported = 1; \
  (option_array)[simple_option_parser_index].requires_value = (requires_value_flag) ? 1 : 0; \
  (option_array)[simple_option_parser_index].is_present = 0; \
  (option_array)[simple_option_parser_index].value = 0; \
} while (0)

typedef struct  {
  uint8_t is_supported;
  uint8_t requires_value;
  uint8_t is_present;
  char* value;
} simple_option_parser_option_t;

uint8_t simple_option_parser(
  uint32_t argument_count,
  char** argument_values,
  simple_option_parser_option_t *options,
  uint32_t *rest_index
) {
  uint32_t index;
  uint32_t length;
  char* argument_value;
  uint8_t option_key;
  simple_option_parser_option_t* option;
  length = argument_count;
  index = 0;
  while (index < length) {
    argument_value = argument_values[index];
    if (argument_value[0] == '-' && argument_value[1] == '-' && argument_value[2] == 0) {
      index += 1;
      break;
    }
    if (argument_value[0] != '-') break;
    if (argument_value[1] == 0) break;
    if (argument_value[2] != 0) break;
    option_key = (uint8_t)argument_value[1];
    option = options + option_key;
    if (option->is_supported == 0) break;
    option->is_present = 1;
    if (option->requires_value != 0) {
      if (index + 1 >= length) return 0;
      index += 1;
      option->value = argument_values[index];
      index += 1;
    } else {
      index += 1;
    }
  }
  *rest_index = index;
  return 1;
}
