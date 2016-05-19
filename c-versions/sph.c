
#include <inttypes.h>

#define pointer uintptr_t
#define b0 void
#define b8 uint8_t
#define b16 uint16_t
#define b32 uint32_t
#define b64 uint64_t
#define b8_s int8_t
#define b16_s int16_t
#define b32_s int32_t
#define b64_s int64_t
#define f32_s float
#define f64_s double
#if debug_log_p

#define debug_log(format,...) fprintf(stderr,"%s:%d " format "\n",__func__,__LINE__,__VA_ARGS__)

#else

#define debug_log(format,...) null

#endif
#define null (b0)(0)
#define _readonly const
#define _noalias restrict
#define increment_one(a) a=(1+a)
#define decrement_one(a) a=(a-1)
#define local_memory_init(size) b0* sph_local_memory_addresses[size];b8 sph_local_memory_index=0
#define local_memory_add(pointer) *(sph_local_memory_addresses+sph_local_memory_index)=pointer;sph_local_memory_index=(1+sph_local_memory_index)
#define local_memory_free while(sph_local_memory_index){decrement_one(sph_local_memory_index);free((*(sph_local_memory_addresses+sph_local_memory_index)));}
#define local_error_init b32_s local_error_number;b8* local_error_module
#define local_error(module_identifier,error_identifier) local_error_module=module_identifier;local_error_number=error_identifier;goto error
