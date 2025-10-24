
#include <inttypes.h>
#include <sph/test.h>
#include <sph/queue.h>
#include <sph/thread-pool.h>
#include <sph/thread-pool.c>
#include <sph/futures.h>
#include <sph/futures.c>
status_t test_thread_pool() {
  status_declare;
  sph_thread_pool_t pool;
  test_helper_assert("thread-pool new", (!sph_thread_pool_new(10, (&pool))));
  test_helper_assert("thread-pool finish", (!sph_thread_pool_finish((&pool), 0, 0)));
exit:
  status_return;
}
void* future_work(void* data) {
  uint8_t* a;
  a = malloc((sizeof(uint8_t)));
  *a = (2 + *((uint8_t*)(data)));
  return (a);
}
status_t test_futures() {
  status_declare;
  sph_future_t future;
  uint8_t data;
  uint8_t* result;
  data = 8;
  test_helper_assert("future-init", (!sph_future_init(10)));
  sph_future_new(future_work, (&data), (&future));
  result = ((uint8_t*)(sph_future_touch((&future))));
  test_helper_assert("touch result", ((2 + data) == *result));
  free(result);
  sph_future_deinit();
exit:
  status_return;
}
int main() {
  status_declare;
  test_helper_test_one(test_futures);
  test_helper_test_one(test_thread_pool);
exit:
  test_helper_display_summary;
  return ((status.id));
}
