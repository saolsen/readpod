;; Holds all global config and connections used by other parts of the code.
(ns readpod.core
  (:use [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)])
  (:require [cemerick.bandalore :as sqs]
            [cemerick.rummage :as sdb]
            [cemerick.rummage.encoding :as enc]
            [aws.sdk.s3 :as s3]
            [readpod.env :as env]))

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
  )

;; # S3
(defn add-file
  "Adds an audio file to the bucket."
  [])

(defn get-url
  "Gets the url to an audio file"
  [article-id]
  )

;; # SDB
(defn get-token-by-userid
  "Returns the user's oauth token or nil if they aren't authorized."
  [user-id]
  (if-let [record (sdb/get-attrs sdb-config "readpod.users" user-id)]
    (:token record)))

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
  [article-id text]
  (sqs/send sqs-client q (pr-str {:id article-id :text text})))

(defn consume-messages
  "Consumes messages from the queue (forever) with the function passed in."
  [funct]
  (doall (map
          (sqs/deleting-consumer sqs-client funct)
          (sqs/polling-receive sqs-client q))))
