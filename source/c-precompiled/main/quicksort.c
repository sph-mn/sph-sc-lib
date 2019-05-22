/* a generic quicksort implementation that works with any array type */
/** less should return true if the first argument is < than the second.
   swap should exchange the values of the two arguments it receives */
void quicksort(uint8_t (*less_p)(void*, size_t, size_t), void (*swap)(void*, size_t, size_t), void* array, size_t left, size_t right) {
  if (right <= left) {
    return;
  };
  size_t pivot = (left + ((right - left) / 2));
  size_t l = left;
  size_t r = right;
  while (1) {
    while (less_p(array, l, pivot)) {
      l = (1 + l);
    };
    while (less_p(array, pivot, r)) {
      r = (r - 1);
    };
    if (l > r) {
      break;
    };
    if (pivot == l) {
      pivot = r;
    } else if (pivot == r) {
      pivot = l;
    };
    swap(array, l, r);
    l = (1 + l);
    r = (r - 1);
  };
  quicksort(less_p, swap, array, left, r);
  quicksort(less_p, swap, array, l, right);
};