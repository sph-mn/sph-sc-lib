
#ifndef sc_included_inttypes_h
#include <inttypes.h>
#define sc_included_inttypes_h
#endif
#ifndef sc_included_stdio_h
#include <stdio.h>
#define sc_included_stdio_h
#endif
#define boolean b8
#define pointer_t uintptr_t
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
#define null ((b0)(0))
#define zero_p(a) (0==a)
;
#ifndef sc_included_string_h
#include <string.h>
#define sc_included_string_h
#endif
#ifndef sc_included_stdlib_h
#include <stdlib.h>
#define sc_included_stdlib_h
#endif
#ifndef sc_included_unistd_h
#include <unistd.h>
#define sc_included_unistd_h
#endif
#ifndef sc_included_sys_stat_h
#include <sys/stat.h>
#define sc_included_sys_stat_h
#endif
#ifndef sc_included_libgen_h
#include <libgen.h>
#define sc_included_libgen_h
#endif
#ifndef sc_included_errno_h
#include <errno.h>
#define sc_included_errno_h
#endif
#define file_exists_p(path) !(access(path,F_OK)==-1)
#define pointer_equal_p(a,b) (((b0*)(a))==((b0*)(b)))
#define free_and_set_zero(a) free(a);\
  a=0
#define increment(a) a=(1+a)
#define decrement(a) a=(a-1)
/** set result to a new string with a trailing slash added, or the given string if it already has a trailing slash.
  returns 0 if result is the given string, 1 if new memory could not be allocated, 2 if result is a new string */
b8 ensure_trailing_slash(b8* a, b8** result)
{
    b32 a_len = strlen(a);
    if((!a_len || ('/' == (*(a + (a_len - 1)))))) {
        (*result) = a;
        return(0);
    } else {
        char* new_a = malloc((2 + a_len));
        if(!new_a) {
            return(1);
        };
        memcpy(new_a, a, a_len);
        memcpy((new_a + a_len), "/", 1);
        (*(new_a + (1 + a_len))) = 0;
        (*result) = new_a;
        return(2);
    };
};
/** return 1 if the path exists or has been successfully created */
boolean ensure_directory_structure(b8* path, mode_t mkdir_mode)
{
    if(file_exists_p(path)) {
        return(1);
    } else {
        b8* path_dirname = dirname_2(path);
        boolean status = ensure_directory_structure(path_dirname, mkdir_mode);
        free(path_dirname);
        return((status && ((EEXIST == errno) || (0 == mkdir(path, mkdir_mode)))));
    };
};
/** return a new string with the same contents as the given string. return 0 if the memory allocation failed */
b8* string_clone(b8* a)
{
    size_t a_size = (1 + strlen(a));
    b8* result = malloc(a_size);
    if(result) {
        memcpy(result, a, a_size);
    };
    return(result);
};
/** always returns a new string */
b8* string_append(b8* a, b8* b)
{
    size_t a_length = strlen(a);
    size_t b_length = strlen(b);
    b8* result = malloc((1 + a_length + b_length));
    if(result) {
        memcpy(result, a, a_length);
        memcpy((result + a_length), b, (1 + b_length));
    };
    return(result);
};
/** like posix dirname, but never modifies its argument and always returns a new string */
b8* dirname_2(b8* a)
{
    b8* path_copy = string_clone(a);
    return(dirname(path_copy));
};