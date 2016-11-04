# sph-sc-lib

various c libraries written in [sc](http://sph.mn/content/3d3).

the directory "c-versions" contains transcompiled c versions of the sc code from under sources/. commentary unfortunately is only to be found in the sc versions.

# included files
* imht-set: a minimalistic fixed size hash table based data structure for sets of integers
* sph: a small file with mostly type aliases and utilities for using the local-error and local-memory pattern as described on http://sph.mn/content/3827
* one: experimental helpers
* scm: helpers for working with guile
* quicksort: an untested quicksort array sorting algorithm implementation derived from the c implementation by darel rex finley from http://alienryderflex.com/quicksort/

# license
the code is under gpl3, the documentation under cc-by-nc.

# imht-set

a data structure for storing a set of integers.
can easily deal with millions of values. a benchmark on an "amd phenom 2" with 3ghz wrote and read 100 million entries in 4 seconds.
insert/delete/search should all be o(1). space usage is roughly double the size of elements. the maximum number of elements a set can store is defined on creation and does not automatically increase.
this implementation sets on maximum read and write speed and a small code size and trades a higher memory usage for it. if lower memory usage is important, there is an option to reduce memory usage with a potential performance loss; otherwise you might want to look for a set implementation similar to google sparse hash.

"imht-set" is an abbreviation for "integer modulo hash table set".

## dependencies
* the c standard library (stdlib.h and inttypes.h)

## usage examples (in sc)
```
(include-sc "imht-set")
```

the file needs to be in the load path or the same directory.

### creation
```
(define set imht-set-t*)
(imht-set-create 200 (address-of set))
```

returns 1 on success or 0 if the memory allocation failed.

the set size does not automatically grow, so a new set has to be created should the specified size later turn out to be insufficient.

### insert
```
(imht-set-add set 4)
```

returns the address of the added or already included element, 0 if there is no space left in the set.

### search
```
(if (imht-set-contains? set 4) #t #f)
```

```
(define value-address uint64_t* (imht-set-find set 4))
```

returns the address of the element in the set, 0 if it was not found.

caveat: if "imht-set-can-contain-zero?" is defined, which is the default, dereferencing the memory address for the value 0, if it was found, will give 1 instead.

### deletion
```
(imht-set-remove set 4)
```

returns 1 if the element was removed, 0 if it was not found.

### destruction
```
(imht-set-destroy set)
```

this is an important call for when the set is no longer needed, since its memory is otherwise not deallocated until the process ends. manual memory management is typical with c.

## configuration options
configuration options can be set by defining macros before including the imht-set source code.

### integer size
an imht-set only stores integers of the same type. supported are all typical integer values, from char to uint64_t.
the type that sets can take is fixed and can not be changed after inclusion of the imht-set source file.

```
(define-macro imht-set-key-t uint64_t)
```

the default type is unsigned 64 bit.

if you would like to use multiple sets with different integer sizes at the same time, you might have to create a derivative of the imht-set.sc file with modified identifiers.

### memory usage
```
(define-macro imht-set-size-factor 2)
```

by default, the memory allocated for the set is at least double the number of elements it is supposed to store.
this can be changed in this definition, and a lower set size factor approaching 1 leads to more efficient memory usage, with 1 being the lowest possible, where only as much space as the elements need by themselves is allocated.
the downside is, that the insert/delete/search performance may approach and reach o(n).

### zero support
by default, the integer 0 is a valid value for a set. but as an optimisation, this can be disabled by defining a macro for "imht-set-can-contain-zero?" with the value zero.

```
(define-macro imht-set-can-contain-zero? 0)
```

with this definition, a zero in sets can not be found, but the set routines should work a tiny little bit faster.

## modularity and implementation
the "imht-set-t" type is a structure with the two fields "size" and "content".
"content" is a one-dimensional array that stores values at indices determined by a hash function.
the set routines automatically adapt should the values for size and content change. therefore, automatic resizing can be implemented by adding new "add" and "remove" routines and rewriting the hash.

# tests
the test files are currently supposed to be compiled and run separately