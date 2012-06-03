(ns readpod.readability
  (:require [readpod.oauth :as oa]))

(def test-token
  {:oauth_token_secret "yHkxZzREc9wC3AC24ak36KXzvz3wUXmv"
   :oauth_token "DTkeQgQkXYzmH6LAcF" :oauth_callback_confirmed true})

(defn get-reading-list
  "Retrieves the reading list for the user."
  [auth-token]
  (let [response (oa/request
                  auth-token
                  :get "https://www.readability.com/api/rest/v1/bookmarks" {})]
    (:bookmarks (:content response))))

(defn get-article-text
  "Retrieves the text of the article"
  [auth-token id]
  (let [response (oa/request
                  auth-token
                  :get
                  (str "https://www.readability.com/api/rest/v1/articles/" id)
                  {})]
    response))