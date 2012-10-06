;; Holds all global config and connections used by other parts of the code.
(ns readpod.core
  (:use [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)])
  (:require [cemerick.bandalore :as sqs]
            [cemerick.rummage :as sdb]
            [cemerick.rummage.encoding :as enc]
            [aws.sdk.s3 :as s3]
            [readpod.env :as env]
            [readpod.oauth :as oauth]))

(defn setup-connections
  "Binds a bunch of vars at runtime based on env vars"
  []
  (def cred {:access-key (:AWSKEY env/vars), :secret-key (:AWSSECRET env/vars)})
  (s3/create-bucket cred "com.readpod.articles")
  (def sdb-client
    (sdb/create-client (:AWSKEY env/vars) (:AWSSECRET env/vars)))
  (def sdb-config (assoc enc/keyword-strings :client sdb-client))
  (sdb/create-domain sdb-client "readpod.users")
  (sdb/create-domain sdb-client "readpod.articles")
  (def sqs-client
    (sqs/create-client (:AWSKEY env/vars) (:AWSSECRET env/vars)))
  (def q (sqs/create-queue sqs-client "readpod-articles"))
  ;; This could maybe just be a var in the core ns but it goes with
  ;; oauth stuff. and yeah
  (oauth/bind-consumer)
  )

;; # S3
(defn add-file
  "Adds an audio file to the bucket."
  [filename id])

(defn get-url
  "Gets the url to an audio file"
  [article-id]
  )

;; # SDB
(defn to-token
  "unserializes the token so we can pass it to the api calling functions"
  [token]
  (let [clj-vals (map read-string token)]
    (reduce #(assoc %1 (first %2) (second %2)) {} clj-vals)))

(defn get-token-by-userid
  "Returns the user's oauth token or nil if they aren't authorized."
  [user-id]
  (if-let [record (sdb/get-attrs sdb-config "readpod.users" user-id)]
    (to-token (:token record))))

(defn save-token
  "Saves a users oauth token."
  [user-id token]
  (sdb/put-attrs sdb-config "readpod.users" {::sdb/id user-id :token token}))

(defn check-render-status
  "Checks if the article id is in sdb,
   if it is already rendered returns the audio file url.
   If it is still processing returns :processing
   If it is not in sdb it returns :none"
  [article-id]
  (if-let [record (sdb/get-attrs sdb-config "readpod.articles" article-id)]
    (if (= (:status record) "completed")
      (:url record)
      :processing)
    :none))

(defn record-processing-article
  [article-id]
  (sdb/put-attrs sdb-config "readpod.articles"
                 {::sdb/id article-id :status "processing"}))

(defn record-completed-article
  [article-id url]
  (sdb/put-attrs sdb-config "readpod.articles"
                 {::sdb/id article-id :status "completed" :url url}))

;; # SQS
(defn queue-article
  [article-id token]
  (sqs/send sqs-client q (pr-str {:id article-id :user-token token}))
  (record-processing-article article-id))

(defn consume-messages
  "Consumes messages from the queue (forever) with the function passed in."
  [funct]
  (doall (map
          (sqs/deleting-consumer sqs-client funct)
          (sqs/polling-receive sqs-client q))))
