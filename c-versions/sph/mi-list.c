
#include <stdlib.h>
#include <inttypes.h>

#ifndef mi_list_element_t
typedef uint64_t mi_list_element_t;
#endif
typedef struct mi_list_struct {
    struct mi_list_struct* link;
    mi_list_element_t data;
} mi_list_t;
#define mi_list_create() (mi_list_t*)(0)
#define mi_list_first(a) ((0==a)?0:(*a).data)
#define mi_list_rest(a) ((0==a)?0:(*a).link)
mi_list_t* mi_list_drop(mi_list_t* a) {
    mi_list_t* a_next=mi_list_rest(a);
    free(a);
    return(a_next);
}
void mi_list_destroy(mi_list_t* a) {
    mi_list_t* a_next=a;
    while(a_next) {
        a_next=(*a).link;
        free(a);
        a=a_next;
    }
}
mi_list_t* mi_list_add(mi_list_t* a,mi_list_element_t value) {
    mi_list_t* element=malloc(sizeof(mi_list_t));
    if(!element) {
        return(0);
    }(*element).data=value;
    if(a) {
        (*element).link=a;
    }
    return(element);
}