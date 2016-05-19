; a minimalistic fixed size hash table based data structure for sets of integers.
; Copyright (C) 2016 sph <sph@posteo.eu>
; This program is free software; you can redistribute it and/or modify it
; under the terms of the GNU General Public License as published by
; the Free Software Foundation; either version 3 of the License, or
; (at your option) any later version.
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
; GNU General Public License for more details.
; You should have received a copy of the GNU General Public License
; along with this program; if not, see <http://www.gnu.org/licenses/>.
(pre-include "stdlib.h" "inttypes.h")
;the following definition sets the integer type and size for values
(define-macro imht-set-key-t uint64_t)
;commenting out the following leads to slightly faster set operations but a stored zero will not be found
(define-macro imht-set-can-contain-zero?)
;the minimum memory usage is size times imht-set-size-factor
(define-macro imht-set-size-factor 2)

(define-array imht-set-primes uint16_t
  #f 3
  7 13
  19 29
  37 43
  53 61
  71 79
  89 101
  107 113
  131 139
  151 163
  173 181
  193 199
  223 229
  239 251
  263 271
  281 293
  311 317
  337 349
  359 373
  383 397
  409 421
  433 443
  457 463
  479 491
  503 521
  541 557
  569 577
  593 601
  613 619
  641 647
  659 673 683 701 719 733 743 757 769 787 809 821 827 839 857 863 881 887 911 929 941 953 971 983 997)

(define imht-set-primes-end uint16_t* (+ imht-set-primes 83))
(define-type imht-set-t (struct (size size-t) (content imht-set-key-t*)))

(define (imht-set-calculate-hash-table-size min-size) (size-t size-t)
  (set min-size (* imht-set-size-factor min-size)) (define primes uint16_t* imht-set-primes)
  (while (<= primes imht-set-primes-end)
    (if (<= min-size (deref primes)) (return (deref primes)) (set primes (+ 1 primes))))
  ;if no prime has been found, use double the size made odd as a best guess
  (return (bit-or 1 min-size)))

(define (imht-set-create min-size result) (uint8_t size-t imht-set-t**)
  ;returns 1 on success or 0 if the memory allocation failed
  (set (deref result) (malloc (sizeof imht-set-t)))
  (set min-size (imht-set-calculate-hash-table-size min-size))
  (struct-set (deref (deref result)) content
    (calloc min-size (sizeof imht-set-key-t)) size min-size)
  (return (if* (struct-deref (deref result) content) #t #f)))

(define (imht-set-destroy a) (void imht-set-t*)
  (if a (begin (free (struct-deref a content)) (free a))))

(pre-if-defined imht-set-can-contain-zero?
  (define-macro (imht-set-hash value hash-table)
    (if* (= 0 value) 0 (+ 1 (modulo value hash-table.size))))
  (define-macro (imht-set-hash value hash-table) (modulo value hash-table.size)))

(define (imht-set-find a value) (imht-set-key-t* imht-set-t* imht-set-key-t)
  ;returns the address of the element in the set, 0 if it was not found.
  ;caveat: if imht-set-can-contain-zero? is defined, which is the default, dereferencing a returned address for the found value 0 will return 1 instead
  (define h imht-set-key-t* (+ (struct-deref a content) (imht-set-hash value (deref a))))
  (if (deref h)
    (begin
      (pre-if-defined imht-set-can-contain-zero?
        (if (or (= 0 value) (= (deref h) value)) (return h)) (if (= (deref h) value) (return h)))
      (define h2 imht-set-key-t* (+ 1 h))
      (define content-end imht-set-key-t* (+ (struct-deref a content) (- (struct-deref a size) 1)))
      (while (<= h2 content-end)
        (if (= value (deref h2)) (return h2) (if (not (deref h2)) (return 0))) (set h2 (+ 1 h2)))
      (if (> h2 content-end)
        (begin (set h2 (struct-deref a content))
          (while (< h2 h) (if (= value (deref h2)) (return h2) (if (not (deref h2)) (return 0)))
            (set h2 (+ 1 h2)))
          (if (= h2 h) (return 0)))))
    (return 0)))

(define-macro imht-set-contains? imht-set-find)

(define (imht-set-remove a value) (uint8_t imht-set-t* imht-set-key-t)
  ;returns 1 if the element was removed, 0 if it was not found
  (define value-address imht-set-key-t* (imht-set-find a value))
  (if value-address (begin (set (deref value-address) 0) (return #t)) (return #f)))

(define (imht-set-add a value) (imht-set-key-t* imht-set-t* imht-set-key-t)
  ;returns the address of the added or already included element, 0 if there is no space left
  (define h imht-set-key-t* h2 imht-set-key-t*)
  (define content-end imht-set-key-t* (+ (struct-deref a content) (- (struct-deref a size) 1)))
  (set h (+ (struct-deref a content) (imht-set-hash value (deref a))))
  (if (deref h)
    (begin (if (= value (deref h)) (return h)) (set h2 (+ 1 h))
      (while (and (<= h2 content-end) (deref h2)) (set h2 (+ 1 h2)))
      (if (> h2 content-end)
        (begin (set h2 (struct-deref a content))
          (while (and (< h2 h) (deref h2)) (set h2 (+ 1 h2))) (if (= h2 h) (return 0)))
        (begin (set (deref h2) value) (return h2))))
    (begin
      (pre-if-defined imht-set-can-contain-zero? (set (deref h) (if* (= 0 value) 1 value))
        (set (deref h) value))
      (return h))))
