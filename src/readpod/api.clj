(ns readpod.api
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.util.response :as resp]
            [ring.middleware.file-info :as file]
            [clojure.string :as str]
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
   :headers {"Content-Type" "text/html"}
   :body data
   :session session})

;; Page Handlers
(defn main-page-handler
  "Main page of app, loaded once oauth is set up"
  [auth-token]
  (println auth-token)
  (let [articles (read/get-reading-list auth-token)
        title-id-pairs (map (fn [x] {:title (:title (:article x))
                                     :id (:id (:article x))}) articles)]
    (html-page (temp/render
                (temp/mainpage title-id-pairs)) {:auth-token auth-token})))

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

(defn article-handler
  "Turns the article into text and returns the audio file."
  [request]
  (println "hello")
  (let [params (:params request)
        auth-token (:auth-token (:session request))
        id (first (str/split (:id params) #".wav"))
        text (read/get-article-text auth-token id)
        audio-file (tts/render text id)]
    (do
      (clean-up id)
      (resp/file-response (str id ".wav")))))

;; Routes
(defroutes main-routes
  (GET "/" request (index-handler request))
  (GET "/authenticated" request (authed-handler request))
  (GET "/article/:id" request (article-handler request))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (file/wrap-file-info (handler/site main-routes)))