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
            [readpod.core :as core]
            [readpod.oauth :as oauth]
            [readpod.readability :as read]
            [readpod.env :as env]
            [readpod.templates :as temp]))

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
  [request]
  (let [
        user-id (:user-id (:session request))
        auth-token (core/get-token-by-userid user-id)
        articles (read/get-reading-list auth-token)
        article-maps (map #(let [article (:article %)]
                             {:title (:title article)
                              :id (:id article)
                              :wordcount (:word_count article)}) articles)]
    (html-page (temp/render
                (temp/mainpage article-maps)) {:user-id user-id})))

(defn index-handler
  "Index, sets up the oauth if not authorized, otherwise loads main page"
  [request]
  (if (:user-id (:session request))
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
  (debug request)
  (let [params (:params request)
        verifyer (:oauth_verifier params)
        oauth-token (:oauth_token params)
        request-token (:request-token (:session request))
        auth-token (oauth/get-access-token request-token verifyer)
        user-id (read/get-user auth-token)]
    (core/save-token user-id auth-token)
    ;; Redirect to the main page
    {:status 302
     :headers {"Location" (str "http://" (:BASEURL env/vars))}
     :body ""
     :session {:user-id user-id}}))

;; Should return the url to the article, if it's not already rendered it should
;; check and see if rendering is in process, if so tell the user it's processing.
;; If it isn't already processing it should add it to the queue, save that processing
;; has begun and tell the user it's processing.

(defn article-handler [request] (println "here's your article bro"))

;;(defn article-handler
;; "Returns the location of article, queue's for rendering if it isn't already."
;; [request]
;; (info "getting article")
;; (let [params (:params request)
;;       auth-token (:auth-token (:session request))
;;       id (first (str/split (:id params) #".wav"))
;;       article-exists? (s/has-file? audio-store id)
;;       article-url (s/get-url audio-store id)]
;;   (if article-exists?
;;     (json-resp article-url auth-token)
;;     (let [text (read/get-article-text auth-token id)]
;;       (w/worker-render queue text)
;;       (json-resp article-url)))))

(defn podcast-handler
  "Returns the podcast for the user."
  [request]
  (println "here's your podcast bro"))

(defroutes main-routes
  (GET "/" request (index-handler request))
  (GET "/authenticated" request (authed-handler request))
  (GET "/article/:id" request
    (article-handler request))
  (GET "/podcast/:id" request
    (podcast-handler request))
  (route/resources "/")
  (route/not-found "Page not found"))

(def api
  (-> main-routes
    (file/wrap-file-info)
    (handler/site)))
