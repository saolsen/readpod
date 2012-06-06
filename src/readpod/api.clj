(ns readpod.api
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [readpod.oauth :as oauth]
            [readpod.readability :as read]
            [readpod.tts :as tts]
            [ring.util.response :as resp]
            [ring.middleware.file-info :as file]
            [clojure.string :as str]
            [clojure.java.io :as io]
            ))

;; Agent that is used to clean up the audio files.
;; Pretty much a huge misuse of agents but it lets me prototype this
;; out fast.
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


;; Helpers
(defn render
  [template]
  (apply str template))

(defn html-page
  [data session]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body data
   :session session})

;; Templates
(html/deftemplate index "templates/index.html"
  [auth-url]
  [:div#login] (html/html-content
                (str "<a href='" auth-url "'>Login</a>")))

(html/deftemplate mainpage "templates/mainpage.html"
  [title-id-pairs]
  [:ul#titles] (html/html-content
                (apply str (map #(str "<li>" (:title %)
      " | "
      "<a href='/article/" (:id %) ".wav'>Audio</a></li>")
                     title-id-pairs))))

;; Page Handlers
(defn main-handler
  "Main page of app, once oauth is set up"
  [auth-token]
  (println auth-token)
  (let [articles (read/get-reading-list auth-token)
        title-id-pairs (map (fn [x] {:title (:title (:article x))
                                     :id (:id (:article x))}) articles)]
    (html-page (render (mainpage title-id-pairs)) {:auth-token auth-token})))

(defn index-handler
  "Index, sets up the oauth if not authorized, otherwise redirects to the
   main page"
  [request]
  (let [auth-token (:auth-token (:session request))]
    (if (nil? auth-token)
      (let [request-token (oauth/get-request-token
                           "http://readpod.herokuapp.com/authenticated")
            auth-url (oauth/get-auth-url request-token)]
        (html-page (render (index auth-url))
                   {:request-token request-token}))
      (main-handler auth-token))))

(defn authed-handler
  "Oauth callback, finishes the authentication and loads the main page"
  [request]
  (let [params (:params request)
        verifyer (:oauth_verifier params)
        oauth-token (:oauth_token params)
        request-token (:request-token (:session request))
        auth-token (oauth/get-access-token request-token verifyer)]
    (main-handler auth-token)))

(defn article-handler
  "Turns the article into text and returns the audio file."
  [request]
  (let [params (:params request)
        auth-token (:auth-token (:session request))
        id (first (str/split (:id params) #".wav"))
        text (:content (:content (read/get-article-text auth-token id)))
        audio-file (tts/render text id)]
    (do
      (clean-up id)
      (println id)
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