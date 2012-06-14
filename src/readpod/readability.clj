(ns readpod.readability
  (:require [clojure.string :as s]
            [ring.util.codec :as c]
            [readpod.oauth :as oa]))

;; This is kind of hacky
(defn content-to-string
  "Takes the html representation of the article
   and turns it into a string of text"
  [content]
  (-> content
      (s/replace #"(&)([^;]*)(;)" " ")
      (s/replace #"(<)([^<>]*)(>)" " ")))

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
                  {})
        content (:content (:content response))]
    (content-to-string content)))