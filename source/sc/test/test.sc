(pre-include "../main/status.c" "stdio.h")

(pre-define
  (test-helper-test-one func) (begin (printf "%s\n" (pre-stringify func)) (s (func)))
  (test-helper-assert description expression)
  (if (not expression) (begin (printf "%s failed\n" description) (s-set-goto s-current.group 1)))
  (test-helper-display-summary)
  (if s-is-success (printf "--\ntests finished successfully.\n")
    (printf "\ntests failed. %d\n" s-current.id)))