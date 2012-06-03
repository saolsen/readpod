(ns readpod.readability
  (:require [oauth.client :as client]))

;; Code to deal with calling the readability API and authenticating
;; with oauth.
(def consumer (client/make-consumer
               "steveolsen"
               "2xLF2Kvb9NsbUFHsDh4UnUjdestnkBcw"
               "https://www.readability.com/api/rest/v1/oauth/request_token/"
               "https://www.readability.com/api/rest/v1/oauth/access_token/"
               "https://www.readability.com/api/rest/v1/oauth/authorize/"
               :hmac-sha1))

(defn get-request-token [callback-uri]
  (client/request-token consumer callback-uri))

(defn get-auth-url [request-token]
  (client/user-approval-uri consumer
                            (:oauth_token request-token)))

(defn get-access-token
  [request-token verifier]
  (do
    (println request-token)
    (println verifier)
    (client/access-token consumer
                         request-token
                         verifier)))

(defn post
  "Posts to readability with authentication."
  [access-token url params]
  (client/credentials consumer
                      (:oauth_token access-token)
                      (:oauth_token_secret access-token)
                      :POST
                      url
                      params))