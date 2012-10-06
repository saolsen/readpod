;; Worker process for translating text to speech.
(ns readpod.worker
  (:require [readpod.env :as env]
            [readpod.core :as core]
            [readpod.readability :as read]
            [clojure.java.shell :as shell]
            [clojure.string :as str])
  (:use [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)])
  (:gen-class))

;;
(defn write-file
  [b filename]
  (with-open [w (clojure.java.io/output-stream filename)]
    (.write w b)))

(defn render
  "Runs text through festival, gets saved as a wav file.
   (should end in .wav)"
  [text filename]
  (let [platform (:PLATFORM env/vars)]
    (if (= platform "OSX")
      (shell/sh
       "say" "-o" filename "--data-format=LEF32@8000" text)
      (let [audio (shell/sh "festival_client" "--ttw" :in text :out-enc :bytes)]
        (write-file (:out audio) filename)))))

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
    (spy (render article-text wavname))
    ;; Convert to mp3
    (info "Converting")
    (debug (convert wavname mp3name))
    ;; save mp3 to s3
    (info "Saving to s3")
    (core/add-file mp3name)
    ;; record that we're done processing that file
    (info "Recording")
    (core/record-completed-article article-id (core/get-url article-id))
    ;; Delete the files on the local filesystem.
    (delete wavname)
    (delete mp3name)
    ))

(defn -main [& args]
  (info "Starting Worker Process")
  (env/bind-vars)
  (core/setup-connections)
  (loop []
    (info "Checking for messages")
    (core/consume-messages consume)
    (recur)))