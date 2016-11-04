
#ifndef sc_included_stdlib_h
#include <stdlib.h>
#define sc_included_stdlib_h
#endif
#ifndef sc_included_errno_h
#include <errno.h>
#define sc_included_errno_h
#endif
#ifndef sc_included_sys_stat_h
#include <sys/stat.h>
#define sc_included_sys_stat_h
#endif
#ifndef sc_included_libgen_h
#include <libgen.h>
#define sc_included_libgen_h
#endif
#ifndef sc_included_string_h
#include <string.h>
#define sc_included_string_h
#endif
#ifndef sc_included_unistd_h
#include <unistd.h>
#define sc_included_unistd_h
#endif
#define string_length strlen
#define string_length_n strnlen
#define string_copy strcpy
#define string_copy_n strncpy
#define string_concat strcat
#define string_concat_n strncat
#define string_index strchr
#define string_index_right strrchr
#define string_index_string strstr
#define string_duplicate strdup
#define string_duplicate_n strndup
#define string_index_ci strcasestr
#define string_span strcspn
#define string_break strpbrk
#define string_compare strcmp
#define memory_copy memcpy
#define memory_compare memcmp
#define file_exists_p(path) !(access(path,F_OK)==-1)
char* ensure_trailing_slash(char* a) {
    b8 a_len=string_length(a);
    if((!a_len||('/'==(*(a+(a_len-1)))))) {
        return(a);
    }
    else {
        char* new_a=malloc((2+a_len));
        if(!new_a) {
            return(0);
        };
        memory_copy(new_a,a,a_len);
        memory_copy((new_a+a_len),"/",1);
        *(new_a+(1+a_len))=0;
        return(new_a);
    };
};
b8* string_clone(b8* a) {
    size_t a_size=(1+string_length(a));
    b8* result=malloc(a_size);
    if(result) {
        memory_copy(result,a,a_size);
    };
    return(result);
};
b8* string_append(b8* a,b8* b) {
    "always returns a new string";
    size_t a_length=string_length(a);
    size_t b_length=string_length(b);
    b8* result=malloc((1+a_length+b_length));
    if(result) {
        memory_copy(result,a,a_length);
        memory_copy((result+a_length),b,(1+b_length));
    };
    return(result);
};
b8* dirname_2(b8* a) {
    b8* path_copy=string_clone(a);
    return(dirname(path_copy));
};
boolean ensure_directory_structure(b8* path,mode_t mkdir_mode) {
    if(file_exists_p(path)) {
        return(1);
    }
    else {
        b8* path_dirname=dirname_2(path);
        boolean status=ensure_directory_structure(path_dirname,mkdir_mode);
        free(path_dirname);
        return((status&&((EEXIST==errno)||(0==mkdir(path,mkdir_mode)))));
    };
};
#define local_define_malloc(variable_name,type,on_error) type* variable_name=malloc(sizeof(type));if(!variable_name){on_error;}
#define free_and_null(a) free(a);a=0
#define pointer_equal_p(a,b) (((b0*)(a))==((b0*)(b)))
