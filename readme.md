# sph-sc-lib

various c libraries written in [sc](http://sph.mn/content/3d3).

the directory "c-versions" contains transcompiled c versions of the sc code from under sources/. commentary unfortunately is only to be found in the sc versions.

# included files
* imht-set: a minimalistic fixed size hash table based data structure for sets of integers
* sph: a small file with mostly type aliases and utilities for using the local-error and local-memory pattern as described on http://sph.mn/content/3827
* one: this library contains more experimental definitions
* scm: helpers for working with guile
* quicksort: an untested quicksort array sorting algorithm implementation derived from the c implementation by darel rex finley from http://alienryderflex.com/quicksort/

# license
the code is implicitly under gpl3, the documentation under cc-by-nc.

# imht-set

can easily deal with millions of values. a benchmark on an amd phenom 2 with 3ghz wrote and read 100 million entries in 4 seconds.
insert/delete/search should all be o(1). space usage is roughly double the size of elements. the maximum set size is defined on creation and does not automatically increase.
this implementation sets on maximum read and write speed and small code size and trades a higher memory usage for it. if lower memory usage is important, there is an option to reduce memory for a potential performance loss. otherwise you might want to look for a set implementation based on a red/black tree.

"imht-set" is an abbreviation for "integer modulo hash table set".

## configuration options
### integer size
an imht-set only stores integers of the same type. supported are all typical integer values, from char to uint64_t.
the type is fixed and set before compilation in the imht-set.sc source code file.

```
(define-macro imht-set-key-t uint64_t)
```

the default type is unsigned 64 bit.

if you would like to use multiple sets with different integer sizes at the same time, you might have to create a derivative of the imht-set.sc file with adjusted identifiers.

### zero support
by default, the integer zero is a valid value for the set. but as an optimisation, zero support can be disabled by commenting out the definition of "imht-set-can-contain-zero?"

```
(define-macro imht-set-can-contain-zero?)
```

without this definition, a zero in sets can not be found, but the set routines should work a tiny little bit faster.

### memory usage
```
(define-macro imht-set-size-factor 2)
```

by default the memory allocated for the set is at least double the number of elements it is supposed to store.
this can be changed in this definition, and a lower set-size-factor approaching 1 leads to more efficient memory usage, with 1 being the lowest possible, where only as much space as the elements need by themselves is allocated.
the downside is, that the insert/delete/search performance may approach and reach o(n).

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

the set size does not automatically grow in this implementation, so a new set has to be created should the specified size turn out to be insufficient.

### insert
```
(imht-set-add set 4)
```

returns the address of the added or already included element, 0 if there is no space left in the set.

### search
```
(define value-address uint64_t* (imht-set-find set 4))
```

returns the address of the element in the set, 0 if it was not found.
caveat: if imht-set-can-contain-zero? is defined, which is the default, dereferencing the address for the value 0, if included, will give 1 instead.

```
(if (imht-set-contains? set 4) #t #f)
```

### deletion
```
(imht-set-remove set 4)
```

returns 1 if the element was removed, 0 if it was not found.

### destruction
```
(imht-set-destroy set)
```

this is an important call for when the set is no longer needed, since its memory is otherwise not deallocated until the process ends. manual memory management is typical for c

## modularity and implementation
the imht-set-t type is a structure with the two fields "size" and "content".
"content" is a one-dimensional array that stores values at indices determined by a hash function.
the set routines automatically adapt should the values for size and content change. therefore, automatic resizing can be easily implemented (i would do it for bitcoin).
