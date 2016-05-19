
#include <stdlib.h>
#include <inttypes.h>

#define imht_set_key_t uint64_t
#define imht_set_can_contain_zero_p
#define imht_set_size_factor 2
uint16_t imht_set_primes[]= {3,7,13,19,29,37,43,53,61,71,79,89,101,107,113,131,139,151,163,173,181,193,199,223,229,239,251,263,271,281,293,311,317,337,349,359,373,383,397,409,421,433,443,457,463,479,491,503,521,541,557,569,577,593,601,613,619,641,647,659,673,683,701,719,733,743,757,769,787,809,821,827,839,857,863,881,887,911,929,941,953,971,983,997};
uint16_t* imht_set_primes_end=(imht_set_primes+83);
typedef struct {
    size_t size;
    imht_set_key_t* content;
} imht_set_t;
size_t imht_set_calculate_hash_table_size(size_t min_size) {
    min_size=(imht_set_size_factor*min_size);
    uint16_t* primes=imht_set_primes;
    while((primes<=imht_set_primes_end)) {
        if((min_size<=(*primes))) {
            return((*primes));
        }
        else {
            primes=(1+primes);
        }
    }
    return((1|min_size));
}
uint8_t imht_set_create(size_t min_size,imht_set_t** result) {
    (*result)=malloc(sizeof(imht_set_t));
    min_size=imht_set_calculate_hash_table_size(min_size);
    (*(*result)).content=calloc(min_size,sizeof(imht_set_key_t));
    (*(*result)).size=min_size;
    return(((*(*result)).content?1:0));
}
void imht_set_destroy(imht_set_t* a) {
    if(a) {
        free((*a).content);
        free(a);
    }
}
#ifdef imht_set_can_contain_zero_p

#define imht_set_hash(value,hash_table) ((0==value)?0:(1+(value%hash_table.size)))

#else

#define imht_set_hash(value,hash_table) (value%hash_table.size)

#endif
imht_set_key_t* imht_set_find(imht_set_t* a,imht_set_key_t value) {
    imht_set_key_t* h=((*a).content+imht_set_hash(value,(*a)));
    if((*h)) {
#ifdef imht_set_can_contain_zero_p
        if(((0==value)||((*h)==value))) {
            return(h);
        }
#else
        if(((*h)==value)) {
            return(h);
        }
#endif
        imht_set_key_t* h2=(1+h);
        imht_set_key_t* content_end=((*a).content+((*a).size-1));
        while((h2<=content_end)) {
            if((value==(*h2))) {
                return(h2);
            }
            else {
                if(!(*h2)) {
                    return(0);
                }
            }
            h2=(1+h2);
        }
        if((h2>content_end)) {
            h2=(*a).content;
            while((h2<h)) {
                if((value==(*h2))) {
                    return(h2);
                }
                else {
                    if(!(*h2)) {
                        return(0);
                    }
                }
                h2=(1+h2);
            }
            if((h2==h)) {
                return(0);
            }
        }
    }
    else {
        return(0);
    }
}
#define imht_set_contains_p imht_set_find
uint8_t imht_set_remove(imht_set_t* a,imht_set_key_t value) {
    imht_set_key_t* value_address=imht_set_find(a,value);
    if(value_address) {
        (*value_address)=0;
        return(1);
    }
    else {
        return(0);
    }
}
imht_set_key_t* imht_set_add(imht_set_t* a,imht_set_key_t value) {
    imht_set_key_t* h;
    imht_set_key_t* h2;
    imht_set_key_t* content_end=((*a).content+((*a).size-1));
    h=((*a).content+imht_set_hash(value,(*a)));
    if((*h)) {
        if((value==(*h))) {
            return(h);
        }
        h2=(1+h);
        while(((h2<=content_end)&&(*h2))) {
            h2=(1+h2);
        }
        if((h2>content_end)) {
            h2=(*a).content;
            while(((h2<h)&&(*h2))) {
                h2=(1+h2);
            }
            if((h2==h)) {
                return(0);
            }
        }
        else {
            (*h2)=value;
            return(h2);
        }
    }
    else {
#ifdef imht_set_can_contain_zero_p
        (*h)=((0==value)?1:value);
#else
        (*h)=value;
#endif
        return(h);
    }
}