;; Readpod uses a work queue for processing the text files into audio
;; files. Locally we want this queue to run in process for easy
;; testing but in deployment it will run using redis as a backend with
;; the web service adding items and one or more worker services
;; consuming them.
(ns readpod.queue)

(defprotocol Queue
  "A queue for processing background jobs in other threads or processes"
  (send-message [q msg]
    "Sends a message to the queue")
  (consume-message [q f]
    "Get the next message on the queue and consumes it with f"))

;; Takes a ref with a PersistantQueue in it. Used for running the
;; whole stack locally in one process.
;;
;;     (LocalQueue. (ref clojure.lang.PersistantQueue/EMPTY))
;;
(deftype LocalQueue [q]
  Queue
  (send-message [this msg] (dosync (alter q conj msg)))
  (consume-message [this f] (dosync
                             (let [next (peek @q)
                                   done (f next)]
                               (alter q pop)
                               done))))

(defn get-local-queue
  "Creates a new local queue"
  []
  (LocalQueue. (ref clojure.lang.PersistentQueue/EMPTY)))

;; Connects via AWS to an SMQ Queue.
(deftype SQSQueue [])