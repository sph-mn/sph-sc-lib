
#ifndef sph_memory_h
#define sph_memory_h

#include <inttypes.h>
#include <sph/status.h>
#include <sph/memreg.h>

#define sph_status_id_memory 1
#define sph_status_group ((uint8_t*)("sph"))
#define sph_memory_error status_set_goto(sph_status_group, sph_status_id_memory)
#define sph_malloc(size, result) sph_primitive_malloc(size, ((void**)(result)))
#define sph_malloc_string(size, result) sph_primitive_malloc_string(size, ((uint8_t**)(result)))
#define sph_calloc(size, result) sph_primitive_calloc(size, ((void**)(result)))
#define sph_realloc(size, result) sph_primitive_realloc(size, ((void**)(result)))
#define sph_memory_growth_factor 2
#define sph_memory_initial_size 4
#define sph_memory_add_directly(a, address, handler) \
  (array3_first_unused(a)).address = address; \
  (array3_first_unused(a)).handler = handler; \
  a->used += 1
array3_declare_type(sph_memory, memreg2_t);
uint8_t* sph_status_description(status_t a);
uint8_t* sph_status_name(status_t a);
status_t sph_primitive_malloc(size_t size, void** result);
status_t sph_primitive_malloc_string(size_t length, uint8_t** result);
status_t sph_primitive_calloc(size_t size, void** result);
status_t sph_primitive_realloc(size_t size, void** memory);
status_t sph_memory_add_with_handler(sph_memory_t* a, void* address, void (*handler)(void*));
void sph_memory_free(sph_memory_t* a);
#endif
