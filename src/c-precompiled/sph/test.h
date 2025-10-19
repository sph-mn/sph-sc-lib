
#ifndef sph_test_h
#define sph_test_h

#include <stdio.h>
#include <sph/status.h>

#define test_helper_test_one(func) \
  printf("%s\n", #func); \
  status_require((func()))
#define test_helper_assert(description, expression) \
  if (!expression) { \
    printf("%s failed\n", description); \
    status_set_goto("", 1); \
  }
#define test_helper_display_summary(status_description) \
  if (status_is_success) { \
    printf(("--\ntests finished successfully.\n")); \
  } else { \
    printf(("\ntests failed. %d %s\n"), (status.id), (status_description(status))); \
  }
#define test_helper_display_summary_id() \
  if (status_is_success) { \
    printf(("--\ntests finished successfully.\n")); \
  } else { \
    printf(("\ntests failed. %d\n"), (status.id)); \
  }
#endif
