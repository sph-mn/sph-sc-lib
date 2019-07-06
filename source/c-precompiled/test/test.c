#include "../main/status.c"
#include <stdio.h>
#define test_helper_test_one(func) \
  printf("%s\n", #func); \
  s((func()))
#define test_helper_assert(description, expression) \
  if (!expression) { \
    printf("%s failed\n", description); \
    s_set_goto((s_current.group), 1); \
  }
#define test_helper_display_summary() \
  if (s_is_success) { \
    printf(("--\ntests finished successfully.\n")); \
  } else { \
    printf(("\ntests failed. %d\n"), (s_current.id)); \
  }
