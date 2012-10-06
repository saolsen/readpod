;; Worker process for translating text to speech.
(ns readpod.worker
  (:require [readpod.env :as env]
            [readpod.core :as core]
            [readpod.readability :as read]
            [clojure.java.shell :as shell])
  (:use [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)])
  (:gen-class :main :true))

;;
(defn render
  "Runs text through festival, gets saved as a wav file.
   (should end in .wav)"
  [text filename]
  (shell/sh
   ;; REAL FOR FESTIVAL
   "echo" text "|" "festival_client" "--ttw" "|" "cat" ">" filename))
   ;; OSX for local dev
   ;"say" "-o" filename "--data-format=LEF32@8000" text))

(defn convert
  "Converts the wav file to an mp3 file.
   filenames must be x.wav and x.mp3"
  [old-file new-file]
  (shell/sh "lame" "-h" "-b=192" old-file new-file))

(defn delete
  "Deletes a file"
  [filename]
  (shell/sh
   "rm" filename))

(defn consume
  "Consumes sqs messages to render text to speech"
  [message]
  (let [body (read-string (:body message))
        _ (debug body)
        article-id (:id body)
        token (:user-token body)
        _ (debug "user token:" token)
        article-text (read/get-article-text token article-id)
        wavname (str article-id ".wav")
        mp3name  (str article-id ".mp3")]
    (debug "wavname: " wavname)
    (debug "mp3name: " mp3name)
    ;; Convert to audio
    (info "Rendering" article-id)
    (render article-text wavname)
    ;; Convert to mp3
    (info "Converting")
    (debug (convert wavname mp3name))
    ;; save mp3 to s3
    (info "Saving to s3")
    (core/add-file mp3name)
    ;; record that we're done processing that file
    (info "Recording")
    (core/record-completed-article article-id (core/get-url article-id))
    ;; TODO: delete the files on the local filesystem.
    ))

(defn -main [& args]
  (info "Starting Worker Process")
  (env/bind-vars)
  (core/setup-connections)
  (loop []
    (info "Checking for messages")
    (core/consume-messages consume)
    (recur)))