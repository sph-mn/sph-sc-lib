(pre-include "sph/status.h" "stdio.h")

(pre-define
  (test-helper-test-one func)
  (begin
    (sc-comment (printf "%s\n" (pre-stringify func)))
    (status-require (func)))
  (test-helper-assert description expression)
  (if (not expression) (begin (printf "%s failed\n" description) (status-set-goto status.group 1)))
  (test-helper-display-summary)
  (if status-is-success
    (printf "--\ntests finished successfully.\n")
    (printf "\ntests failed. %d\n" status.id)))