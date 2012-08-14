(ns readpod.queue)
;; Readpod uses a work queue for processing the text files into audio
;; files. Locally we want this queue to run in process for easy
;; testing but in deployment it will run using redis as a backend.

(defprotocol WorkQueue
  "A queue for processing background jobs in other threads or processes"
  (add-job [q job] "Add a job to the queue")
  (get-job [q] "Get the next job on the queue."))

;; Takes a ref with a PersistantQueue in it
;; (LocalQueue. (atom clojure.lang.PersistantQueue/EMPTY))
(deftype LocalQueue [q]
  WorkQueue
  (add-job [this job] (dosync (alter q conj job)))
  (get-job [this] (dosync
                   (let [next (peek @q)]
                     (alter q pop)
                     next))))