#include <inttypes.h>
#include "./test.c"
#include "../main/queue.c"
#include "../main/thread-pool.c"
#include "../main/futures.c"
status_t test_thread_pool() {
  status_declare;
  thread_pool_t pool;
  thread_pool_task_t task;
  size_t i;
  test_helper_assert("thread-pool new", (!thread_pool_new(10, (&pool))));
  test_helper_assert("thread-pool finish", (!thread_pool_finish((&pool))));
exit:
  return (status);
};
void* future_work(void* data) {
  uint8_t* a;
  a = malloc((sizeof(uint8_t)));
  *a = (2 + *((uint8_t*)(data)));
  return (a);
};
status_t test_futures() {
  status_declare;
  future_t* future;
  uint8_t data;
  uint8_t* result;
  data = 8;
  test_helper_assert("future-init new", (!future_init(10)));
  future = future_new(future_work, (&data));
  test_helper_assert("future-init new", future);
  result = ((uint8_t*)(touch(future)));
  test_helper_assert("touch result", ((2 + data) == *result));
  free(result);
  future_deinit();
exit:
  return (status);
};
int main() {
  status_declare;
  test_helper_test_one(test_futures);
  test_helper_test_one(test_thread_pool);
exit:
  test_helper_display_summary();
  return ((status.id));
};