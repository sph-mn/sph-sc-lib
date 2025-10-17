
#ifndef sph_memory
#define sph_memory

#include <stdlib.h>
#include <stdio.h>
#include <sph/memory.h>
uint8_t* sph_status_description(status_t a) {
  uint8_t* b;
  if (sph_status_id_memory == a.id) {
    b = "not enough memory or other memory allocation error";
  } else {
    b = "";
  };
}
uint8_t* sph_status_name(status_t a) {
  uint8_t* b;
  if (sph_status_id_memory == a.id) {
    b = "memory";
  } else {
    b = "unknown";
  };
}

/** allocation helpers use status-t and have the same interface */
status_t sph_primitive_malloc(size_t size, void** result) {
  status_declare;
  void* a;
  a = malloc(size);
  if (a) {
    *result = a;
  } else {
    sph_memory_error;
  };
exit:
  status_return;
}

/** like sph-malloc but allocates one extra byte that is set to zero */
status_t sph_primitive_malloc_string(size_t length, uint8_t** result) {
  status_declare;
  uint8_t* a;
  status_require((sph_malloc((1 + length), (&a))));
  a[length] = 0;
  *result = a;
exit:
  status_return;
}
status_t sph_primitive_calloc(size_t size, void** result) {
  status_declare;
  void* a;
  a = calloc(size, 1);
  if (a) {
    *result = a;
  } else {
    sph_memory_error;
  };
exit:
  status_return;
}
status_t sph_primitive_realloc(size_t size, void** memory) {
  status_declare;
  void* a;
  a = realloc((*memory), size);
  if (a) {
    *memory = a;
  } else {
    sph_memory_error;
  };
  status_return;
}

/** event memory addition with automatic array expansion */
status_t sph_memory_add_with_handler(sph_memory_t* a, void* address, void (*handler)(void*)) {
  status_declare;
  status_require((sph_memory_ensure(a, sph_memory_initial_size)));
  sph_memory_add_directly(a, address, handler);
exit:
  status_return;
}

/** free all registered memory and unitialize the event-memory register */
void sph_memory_free(sph_memory_t* a) {
  if (!a->data) {
    return;
  };
  memreg2_t m;
  for (size_t i = 0; (i < a->size); i += 1) {
    m = array3_get((*a), i);
    (m.handler)((m.address));
  };
  array3_free(a);
  a->data = 0;
}
#endif
