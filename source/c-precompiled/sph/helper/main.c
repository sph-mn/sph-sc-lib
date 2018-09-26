status_t sph_helper_primitive_malloc(size_t size, void** result) {
  status_declare;
  void* a;
  a = malloc(size);
  if (a) {
    *result = a;
  } else {
    status.group = sph_status_group_sph;
    status.id = sph_status_id_memory;
  };
  return (status);
};
/** like sph-helper-malloc but allocates one extra byte that is set to zero */
status_t sph_helper_primitive_malloc_string(size_t length, uint8_t** result) {
  status_declare;
  uint8_t* a;
  status_require((sph_helper_malloc((1 + length), (&a))));
  a[length] = 0;
  *result = a;
exit:
  return (status);
};
status_t sph_helper_primitive_calloc(size_t size, void** result) {
  status_declare;
  void* a;
  a = calloc(size, 1);
  if (a) {
    *result = a;
  } else {
    status.group = sph_status_group_sph;
    status.id = sph_status_id_memory;
  };
  return (status);
};
status_t sph_helper_primitive_realloc(size_t size, void** block) {
  status_declare;
  void* a;
  a = realloc((*block), size);
  if (a) {
    *block = a;
  } else {
    status.group = sph_status_group_sph;
    status.id = sph_status_id_memory;
  };
  return (status);
};
/** get a decimal string representation of an unsigned integer */
uint8_t* sph_helper_uint_to_string(uintmax_t a, size_t* result_len) {
  size_t size;
  uint8_t* result;
  size = (1 + ((0 == a) ? 1 : (1 + log10(a))));
  result = malloc(size);
  if (!result) {
    return (0);
  };
  if (snprintf(result, size, "%ju", a) < 0) {
    free(result);
    return (0);
  } else {
    *result_len = (size - 1);
    return (result);
  };
};
/** display the bits of an octet */
void sph_helper_display_bits_u8(uint8_t a) {
  uint8_t i;
  printf("%u", (1 & a));
  for (i = 1; (i < 8); i = (1 + i)) {
    printf("%u", (((((uint8_t)(1)) << i) & a) ? 1 : 0));
  };
};
/** display the bits of the specified memory region */
void sph_helper_display_bits(void* a, size_t size) {
  size_t i;
  for (i = 0; (i < size); i = (1 + i)) {
    display_bits_u8((((uint8_t*)(a))[i]));
  };
  printf("\n");
};