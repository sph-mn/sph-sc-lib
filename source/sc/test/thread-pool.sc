(pre-include "inttypes.h" "./test.c" "../main/queue.c" "../main/thread-pool.c" "../main/futures.c")

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

(define (future-work data) (void* void*)
  (declare a uint8-t*)
  (set
    a (malloc (sizeof uint8-t))
    (pointer-get a) (+ 2 (pointer-get (convert-type data uint8-t*))))
  (return a))

(define (test-futures) status-t
  status-declare
  (declare
    future future-t*
    data uint8-t
    result uint8-t*)
  (set data 8)
  (test-helper-assert "future-init new" (not (future-init 10)))
  (set future (future-new future-work &data))
  (test-helper-assert "future-init new" future)
  (set result (convert-type (touch future) uint8-t*))
  (test-helper-assert "touch result" (= (+ 2 data) *result))
  (free result)
  (future-deinit)
  (label exit
    (return status)))

(define (main) int
  status-declare
  (test-helper-test-one test-futures)
  (test-helper-test-one test-thread-pool)
  (label exit
    (test-helper-display-summary)
    (return status.id)))