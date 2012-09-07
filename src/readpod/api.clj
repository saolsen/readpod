(ns readpod.api
  (:use [ring.adapter.jetty]
        [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)]
        [compojure.core])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :as resp]
            [ring.middleware.file-info :as file]
            [clojure.string :as str]
            [clojure.data.json :as j]
            [clojure.java.io :as io]
            [readpod.oauth :as oauth]
            [readpod.readability :as read]
            [readpod.tts :as tts]
            [readpod.env :as env]
            [readpod.templates :as temp]))

;; Agent that is used to clean up the audio files.
;; Kind of a hack....
(defonce cleaner (agent nil))
(defn clean-up
  "Waits awhile then deletes the audio file with that id."
  [id]
  (let [clean (fn [x]
                (do
                  (. Thread (sleep 100000))
                  (io/delete-file (str id ".wav"))
                  nil))]
    (send-off cleaner clean)))

;; Helper
(defn html-page
  [data session]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body data
   :session session})

(defn json-resp
  [data session]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (j/json-str data)
   :session session})

;; Page Handlers
(defn main-page-handler
  "Main page of app, loaded once oauth is set up"
  [auth-token]
  (let [articles (read/get-reading-list auth-token)
        article-maps (map #(let [article (:article %)]
                             {:title (:title article)
                              :id (:id article)
                              :wordcount (:word_count article)}) articles)]
    (html-page (temp/render
                (temp/mainpage article-maps)) {:auth-token auth-token})))

(defn index-handler
  "Index, sets up the oauth if not authorized, otherwise loads main page"
  [request]
  (if-let [auth-token (:auth-token (:session request))]
    (main-page-handler request)
    (let [callback-route (str "http://"
                              (:BASEURL env/vars)
                              "/authenticated")
          request-token (oauth/get-request-token callback-route)
          auth-url (oauth/get-auth-url request-token)]
      (html-page (temp/render (temp/index auth-url))
                 {:request-token request-token}))))

(defn authed-handler
  "Oauth callback, finishes the authentication and loads the main page"
  [request]
  (let [params (:params request)
        verifyer (:oauth_verifier params)
        oauth-token (:oauth_token params)
        request-token (:request-token (:session request))
        auth-token (oauth/get-access-token request-token verifyer)]
    (main-page-handler auth-token)))

;;(defn article-handler
;;  "Returns the location of article, queue's for rendering if it isn't already."
;;  [request]
;;  (let [params (:params request)
;;        auth-token (:auth-token (:session request))
;;        id (first (str/split (:id params) #".wav"))
;;        text (read/get-article-text auth-token id)
;;        audio-file (tts/render text id)]
;;    (do
;;      (clean-up id)
;;      (resp/file-response (str id ".wav")))))

;; STUB
(defn article-handler
  "Returns the location of the articles mp3, if the article doesn't exist yet
   it queue's it for rendering."
  [request]
  (json-resp "https://s3.amazonaws.com/com.readpod.articles/boom.wav"))

(defn get-app
  "Takes a queue (and later a store or whatever) and creates the main api."
  [queue store]
  (let [routes (defroutes main-routes
                 (GET "/" request (index-handler request))
                 (GET "/authenticated" request (authed-handler request))
                 (GET "/article/:id" request (article-handler request))
                 (route/resources "/")
                 (route/not-found "Page not found"))]
    (file/wrap-file-info (handler/site routes))))
