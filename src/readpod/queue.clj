;; Readpod uses a work queue for processing the text files into audio
;; files. Locally we want this queue to run in process for easy
;; testing but in deployment it will run using redis as a backend with
;; the web service adding items and one or more worker services
;; consuming them.
(ns readpod.queue
  (:use [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)]))

(defprotocol Queue
  "A work queue."
  (enqueue [q msg] "Adds a message to the queue.")
  (process [q handler] "Adds a handler to be called with each message"))

;; An in memory implementation of a queue, can only have one consumer.
(deftype MemoryQueue [consumer]
  Queue
  (enqueue [this msg] (@consumer msg))
  (process [this handler] (reset! consumer handler)))

(defn new-memory-queue []
  (info "Creating in memory queue")
  (MemoryQueue. (atom (fn [msg] (println "no consumer")))))

(defn connect-prod-queue [] nil)
