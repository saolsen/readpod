;; Worker process for translating text to speech.
(ns readpod.worker
  (:require [readpod.tts :as tts]
            [readpod.core :as core])
  (:use [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)])
  (:gen-class))

;;

(defn consume
  "Consumes sqs messages to render text to speech"
  [message]
  )

(defn -main [& args]
  (info "Starting Worker Process")
  (loop []
      (core/consume-messages consume)
    (recur)))