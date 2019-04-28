/* fine-grain parallelism based on sph/thread-pool.c.
provides task objects with functions executed in a thread-pool that can be waited for to get a result value.
manages the memory of thread-pool task objects.
thread-pool.c must be included beforehand */
/* for usleep */
#include <unistd.h>
uint8_t sph_futures_pool_is_initialised;
thread_pool_t sph_futures_pool;
typedef void* (*future_f_t)(void*);
typedef struct {
  thread_pool_task_t task;
  uint8_t finished;
  future_f_t f;
} future_t;
/** call once to initialise the future thread pool that persists for
  the whole process or until future-deinit is called */
int future_init(thread_pool_size_t thread_count) {
  int status;
  status = thread_pool_new(thread_count, (&sph_futures_pool));
  if (!status) {
    sph_futures_pool_is_initialised = 1;
  };
  return (status);
};
/** internal future worker.
  returns true to keep thread running.
  a->f returns because modifying data likely needs extra type conversions inside a->f.
  thread-pool does not have a finished field by default so that tasks can themselves free
  their object when they finish */
void future_eval(thread_pool_task_t* task) {
  future_t* a;
  a = ((future_t*)((((char*)(task)) - offsetof(future_t, task))));
  task->data = (a->f)((task->data));
  a->finished = 1;
};
/** return a new futures object or zero if memory allocation failed.
  the given function receives data as its sole argument */
future_t* future_new(future_f_t f, void* data) {
  future_t* a;
  a = malloc((sizeof(future_t)));
  if (!a) {
    return (0);
  };
  a->finished = 0;
  a->f = f;
  a->task.f = future_eval;
  a->task.data = data;
  thread_pool_enqueue((&sph_futures_pool), (&(a->task)));
  return (a);
};
/** can be called to stop and free the main thread-pool */
void future_deinit() {
  thread_pool_finish((&sph_futures_pool));
  thread_pool_destroy((&sph_futures_pool));
};
/** blocks until future is finished and returns its result */
void* touch(future_t* a) {
  void* result;
loop:
  if (a->finished) {
    result = a->task.data;
    free(a);
    return (result);
  } else {
    /* poll five times per second */
    usleep(20000);
    goto loop;
  };
};