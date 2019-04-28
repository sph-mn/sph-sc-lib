(pre-include "inttypes.h" "./test.c" "../main/queue.c" "../main/thread-pool.c")

(define (test-thread-pool) status-t
  status-declare
  (declare
    pool thread-pool-t
    task thread-pool-task-t
    i size-t)
  (test-helper-assert "thread-pool new" (not (thread-pool-new 10 &pool)))
  (test-helper-assert "thread-pool finish" (not (thread-pool-finish &pool)))
  (label exit
    (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-thread-pool)
  (label exit
    (test-helper-display-summary)
    (return status.id)))