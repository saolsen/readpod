(ns readpod.oauth
  (:require [readpod.env :as env]
            [oauth.client :as client]
            [com.twinql.clojure.http :as http]
            [clojure.string :as str]
            [clojure.data.json :as json]))

;; Helper
(defn to-upper-keyword
  "Takes a keyword and makes it all upper case"
  [key]
  (keyword (str/upper-case (name key))))

;; Code to deal with the oauth authentication needed to use the
;; readability api.

(defn bind-consumer
  []
  (def consumer
    (client/make-consumer
     (:READAPIKEY env/vars)
     (:READAPISECRET env/vars)
     "https://www.readability.com/api/rest/v1/oauth/request_token/"
     "https://www.readability.com/api/rest/v1/oauth/access_token/"
     "https://www.readability.com/api/rest/v1/oauth/authorize/"
     :hmac-sha1)))

(defn get-request-token [callback-uri]
  (client/request-token consumer callback-uri))

(defn get-auth-url [request-token]
  (client/user-approval-uri consumer
                            (:oauth_token request-token)))

(defn get-access-token
  [request-token verifier]
  (client/access-token consumer
                       request-token
                       verifier))

(defn get-credentials
  [access-token method url params]
  (client/credentials consumer
                      (:oauth_token access-token)
                      (:oauth_token_secret access-token)
                      method
                      url
                      params))

(defn request
  "Makes a request with oauth"
  [access-token request-type url params]
  (let [type (to-upper-keyword request-type)
        choices {:GET http/get
                 :POST http/post
                 :PUT http/put
                 :HEAD http/head
                 :DELETE http/delete}
        method ((keyword type) choices)
        credentials (get-credentials access-token type url params)]
    (method url
            :as :json
            :query (merge credentials params)
            :parameters (http/map->params {:use-expect-continue false}))))