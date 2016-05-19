
#include <stdio.h>
#include <inttypes.h>
#include <assert.h>
#include <time.h>
#include <imht-set.c>

#define test_element_count 1000000
uint8_t test_zero(imht_set_t* set) {
    assert((0==imht_set_find(set,0)));
    imht_set_add(set,0);
    assert(!(0==imht_set_find(set,0)));
    imht_set_remove(set,0);
    assert((0==imht_set_find(set,0)));
}
uint8_t insert_values(imht_set_t* set) {
    size_t counter=test_element_count;
    while(counter) {
        imht_set_add(set,counter);
        counter=(counter-1);
    }
}
uint8_t test_value_existence(imht_set_t* set) {
    size_t counter=test_element_count;
    while(counter) {
        assert(!(0==imht_set_find(set,counter)));
        counter=(counter-1);
    }
}
void print_contents(imht_set_t* set) {
    size_t index=((*set).size-1);
    while(index) {
        printf("%lu\n",*((*set).content+index));
        index=(index-1);
    }
}
#define get_time() (uint64_t)(time(0))
#define print_time(a) printf("%u\n",a)
int main() {
    imht_set_t* set;
    imht_set_create(test_element_count,&set);
    test_zero(set);
    insert_values(set);
    test_value_existence(set);
    imht_set_destroy(set);
    return(0);
}