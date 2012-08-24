;; Worker process for translating text to speech.
(ns readpod.worker
  (:require [readpod.queue :as q])
  (:use [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)])
  (:gen-class))

(defn start-worker
  "Takes a queue to consume from and creates a worker for rendering audio."
  [queue]
  (do
    (info "Starting Worker Process")
    (q/process queue println)))

;; To start a production worker process directly.
;;
;;    lein run -m readpod.worker
;;
(defn -main [& args]
  (let [queue (q/connect-prod-queue)]
    (start-worker queue)))