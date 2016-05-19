
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
#define local_memory_init(size) b0* _local_memory_addresses[size];b8 _local_memory_index=0
#define local_memory_add(pointer) *(_local_memory_addresses+_local_memory_index)=pointer;_local_memory_index=(1+_local_memory_index)
#define local_memory_free while(_local_memory_index){decrement_one(_local_memory_index);free((*(_local_memory_addresses+_local_memory_index)));}
#define local_error_init b32_s local_error_number;b8 local_error_module
#define local_error(module_identifier,error_identifier) local_error_module=module_identifier;local_error_number=error_identifier;goto error
#define local_error_assert_enable 1
#define sph 1
#if local_error_assert_enable

#define local_error_assert(module,number,expr) if(!expr){local_error(module,number);}

#else

#define local_error_assert(module,number,expr) null

#endif
#define error_memory -1
#define error_input -2
char* error_description(b32_s n) {
    return(((error_memory==n)?"memory":((error_input==n)?"input":"unknown")));
}
#define local_define_malloc(variable_name,type) type* variable_name=malloc(sizeof(type));if(!variable_name){local_error(sph,error_memory);}
