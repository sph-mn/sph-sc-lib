#include <stdlib.h>
#include <inttypes.h>
#include "./status.c"
#define status_group_sph "sph"
/** add explicit type cast to prevent compiler warning */
#define sph_helper_malloc(size, result) sph_helper_primitive_malloc(size, ((void**)(result)))
#define sph_helper_malloc_string(size, result) sph_helper_primitive_malloc_string(size, ((uint8_t**)(result)))
#define sph_helper_calloc(size, result) sph_helper_primitive_calloc(size, ((void**)(result)))
#define sph_helper_realloc(size, result) sph_helper_primitive_realloc(size, ((void**)(result)))
enum { sph_status_id_memory };
status_t sph_helper_primitive_malloc(size_t size, void** result);
status_t sph_helper_primitive_malloc_string(size_t length, uint8_t** result);
status_t sph_helper_primitive_calloc(size_t size, void** result);
status_t sph_helper_primitive_realloc(size_t size, void** block);
uint8_t* sph_helper_uint_to_string(uintmax_t a, size_t* result_len);
void sph_helper_display_bits_u8(uint8_t a);
void sph_helper_display_bits(void* a, size_t size);