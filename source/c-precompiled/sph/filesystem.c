#include <unistd.h>
#include <sys/stat.h>
#include <libgen.h>
#include <errno.h>
#define file_exists_p(path) !(access(path, F_OK) == -1)
/** like posix dirname, but never modifies its argument and always returns a new string */
uint8_t* dirname_2(uint8_t* a) {
  uint8_t* path_copy = string_clone(a);
  return ((dirname(path_copy)));
};
/** return 1 if the path exists or has been successfully created */
uint8_t ensure_directory_structure(uint8_t* path, mode_t mkdir_mode) {
  if (file_exists_p(path)) {
    return (1);
  } else {
    uint8_t* path_dirname = dirname_2(path);
    uint8_t status = ensure_directory_structure(path_dirname, mkdir_mode);
    free(path_dirname);
    return ((status && ((EEXIST == errno) || (0 == mkdir(path, mkdir_mode)))));
  };
};