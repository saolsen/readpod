;; Worker process for translating text to speech.
(ns readpod.worker
  (:require [readpod.env :as env]
            [readpod.core :as core]
            [readpod.readability :as read])
  (:use [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)])
  (:gen-class))

;;
(defn render
  "Runs text through festival, gets saved as a wav file.
   (should end in .wav)"
  [text filename]
  (clojure.java.shell/sh
   ;; REAL FOR FESTIVAL
   ;;   "echo" "|" "festival_client" "--ttw" "|" "cat" ">" filename))
   ;; OSX
   "say" "-o" filename "--data-format=LEF32@8000" text))

(defn convert
  "Converts the wav file to an mp3 file.
   filenames must be x.wav and x.mp3"
  [old_filename new_filename]
  (clojure.java.shell/sh
   ;; You can '$ brew install lame' for OSX
   "lame" "–h" "–b 192" old_filename new_filename))

(defn delete
  "Deletes a file"
  [filename]
  (clojure.java.shell/sh
   "rm" filename))

(defn consume
  "Consumes sqs messages to render text to speech"
  [message]
  (let [article-id (:id (:body message))
        token (:user-token (:body message))
        article-text (read/get-article-text token article-id)
        wavname (str article-id ".wav")
        mp3name  (str article-id ".mp3")]
    ;; Convert to audio
    (render article-text wavname)
    ;; Convert to mp3
    (convert wavname mp3name)
    ;; save mp3 to s3
    (core/add-file mp3name article-id)
    ;; record that we're done processing that file
    (core/record-completed-article article-id (core/get-url article-id))
    ))

(defn -main [& args]
  (info "Starting Worker Process")
  (env/bind-vars)
  (core/setup-connections)
  (loop []
      (core/consume-messages consume)
    (recur)))