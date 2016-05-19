
#include <string.h>

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
#define file_exists_p(path) !(access(path,F_OK)==-1)
char* ensure_trailing_slash(char* str) {
    b8 str_len=string_length(str);
    if((!str_len||('/'==(*(str+(str_len-1)))))) {
        return(str);
    }
    else {
        char* new_str=malloc((2+str_len));
        memory_copy(new_str,str,str_len);
        memory_copy((new_str+str_len),"/",1);
        *(new_str+(1+str_len))=0;
        return(new_str);
    }
}
#define array_contains_s(array_start,array_end,search_value,index_temp,res) index_temp=array_start;res=0;while((index_temp<=array_end)){if(((*index_temp)==search_value)){res=1;break;}increment_one(index_temp);}
#define require_goto(a,label) if(!a){goto label;}
#if stability_typechecks

#define if_typecheck(expr,action) if(!expr){debug_log("type check failed %s",((string_length(#expr)<24)?#expr:""));action;}

#else

#define if_typecheck(expr,action) null

#endif
#define octet_write_string_binary(target,a) sprintf(target,"%d%d%d%d%d%d%d%d",((a&128)?1:0),((a&64)?1:0),((a&32)?1:0),((a&16)?1:0),((a&8)?1:0),((a&4)?1:0),((a&2)?1:0),((a&1)?1:0))
