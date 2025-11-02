(pre-include-guard-begin sph-test-h-included)
(pre-include "stdio.h" "sph/status.h")

(pre-define
  (test-helper-test-one func) (begin (printf "%s\n" (pre-stringify func)) (status-require (func)))
  (test-helper-assert description expression)
  (if (not expression) (begin (printf "%s failed\n" description) (status-set-goto "" 1)))
  (test-helper-display-summary-description status-description)
  (if status-is-success (printf "--\ntests finished successfully.\n")
    (printf "\ntests failed. %d %s\n" status.id (status-description status)))
  test-helper-display-summary
  (if status-is-success (printf "--\ntests finished successfully.\n")
    (printf "\ntests failed. %d\n" status.id)))

(pre-include-guard-end)
