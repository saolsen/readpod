(ns readpod.env
  (:use
   [taoensso.timbre :as timbre
    :only (trace debug info warn error fatal spy)]))

;; Code that deals with extracting values from environment variables.
;; Alternatively they can be in a json file called config.js
;; These must all be defined anywhere this app is run.
;; BASEURL       - Used to constuct the oauth callback url
;; READAPIKEY    - Readability API key
;; READAPISECRET - Readability API secret
;; AWSKEY        - AWS nKey
;; AWSSECRET     - AWS Secret
;; PLATFORM      - OSX or LINUX - used so we can test locally with the
;; say command.

(defn bind-vars
  "Binds vars at runtime instead of compile time"
  []
  (def vars
    (let [env-vars (System/getenv)]
      (if (every? #(contains? env-vars %)
                  ["BASEURL" "READAPIKEY" "READAPISECRET" "AWSKEY" "AWSSECRET"])
        {:BASEURL (get env-vars "BASEURL")
         :READAPIKEY (get env-vars "READAPIKEY")
         :READAPISECRET (get env-vars "READAPISECRET")
         :AWSKEY (get env-vars "AWSKEY")
         :AWSSECRET (get env-vars "AWSSECRET")
         :PLATFORM (get env-vars "PLATFORM")
         }
        (throw (Exception. "Please Define All Environment Variables")))))
  (debug vars)
)