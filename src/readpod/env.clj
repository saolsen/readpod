(ns readpod.env)

;; Code that deals with extracting values from environment variables.
;; These must all be defined anywhere this app is run.
;; BASEURL       - Used to constuct the oauth callback url
;; READAPIKEY    - Readability API key
;; READAPISECRET - Readability API secret

(def vars
  (let [env-vars (System/getenv)]
    (if (every? #(contains? env-vars %)
                ["BASEURL" "READAPIKEY" "READAPISECRET"])
      {:BASEURL (get env-vars "BASEURL")
       :READAPIKEY (get env-vars "READAPIKEY")
       :READAPISECRET (get env-vars "READAPISECRET")}
      (throw (Exception. "Please Define All Environment Variables")))))
