
#include <sph/status.h>
#include <stdio.h>

#define test_helper_test_one(func) \
  printf("%s\n", #func); \
  status_require((func()))
#define test_helper_assert(description, expression) \
  if (!expression) { \
    printf("%s failed\n", description); \
    status_set_goto((status.group), 1); \
  }
#define test_helper_display_summary() \
  if (status_is_success) { \
    printf(("--\ntests finished successfully.\n")); \
  } else { \
    printf(("\ntests failed. %d\n"), (status.id)); \
  }
