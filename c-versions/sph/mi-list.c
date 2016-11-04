
#ifndef sc_included_stdlib_h
#include <stdlib.h>
#define sc_included_stdlib_h
#endif
#ifndef sc_included_inttypes_h
#include <inttypes.h>
#define sc_included_inttypes_h
#endif
#ifndef mi_list_name_infix

#define mi_list_name_infix 64

#endif
#ifndef mi_list_element_t

#define mi_list_element_t uint64_t

#endif
#ifndef mi_list_name_concat

#define mi_list_name_concat(a,b) mi_list_##a##_##b
#define mi_list_name_concatenator(a,b) mi_list_name_concat(a,b)
#define mi_list_name(name) mi_list_name_concatenator(mi_list_name_infix,name)

#endif
#define mi_list_struct_name mi_list_name(struct)
#define mi_list_t mi_list_name(t)
typedef struct mi_list_struct_name {
    struct mi_list_struct_name* link;
    mi_list_element_t data;
} mi_list_t;
#ifndef mi_list_first

#define mi_list_first(a) (*a).data
#define mi_list_first_address(a) &(*a).data
#define mi_list_rest(a) (*a).link

#endif
mi_list_t* mi_list_name(drop)(mi_list_t* a) {
    mi_list_t* a_next=mi_list_rest(a);
    free(a);
    return(a_next);
};
void mi_list_name(destroy)(mi_list_t* a) {
    mi_list_t* a_next=0;
    while(a) {
        a_next=(*a).link;
        free(a);
        a=a_next;
    };
};
mi_list_t* mi_list_name(add)(mi_list_t* a,mi_list_element_t value) {
    mi_list_t* element=calloc(1,sizeof(mi_list_t));
    if(!element) {
        return(0);
    };
    (*element).data=value;
    (*element).link=a;
    return(element);
};
size_t mi_list_name(length)(mi_list_t* a) {
    size_t result=0;
    while(a) {
        result=(1+result);
        a=mi_list_rest(a);
    };
    return(result);
};
#undef mi_list_name_infix
#undef mi_list_element_t
#undef mi_list_struct_name
#undef mi_list_t

