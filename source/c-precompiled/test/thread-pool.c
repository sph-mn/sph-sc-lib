#include <inttypes.h>
#include "./test.c"
#include "../main/queue.c"
#include "../main/thread-pool.c"
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
int main() {
  status_declare;
  test_helper_test_one(test_thread_pool);
exit:
  test_helper_display_summary();
  return ((status.id));
};