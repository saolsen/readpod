(ns readpod.api
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [readpod.oauth :as oauth]
            ))

(defn render
  [template]
  (apply str template))

;; Templates
(html/deftemplate index "templates/index.html"
  [auth-url]
  [:div#login] (html/html-content
                (str "<a href='" auth-url "'>Login</a>")))

(html/deftemplate mainpage "templates/mainpage.html"
  []
  )

;; Page Handlers
(defn main-handler
  "Main page handler, run once oauth is set up"
  [auth-token]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str auth-token)
;;   :body (render (mainpage))
   :session {:auth-token auth-token}})

(defn index-handler
  "Main page, sets up the oauth request and gives the link."
  [request]
  (let [auth-token (:auth-token (:session request))]
    (if (nil? auth-token)
      (let [request-token (oauth/get-request-token
                           "http://localhost:8080/authenticated")
            auth-url (oauth/get-auth-url request-token)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (render (index auth-url))
         :session {:request-token request-token}})
      (main-handler auth-token))))

(defn authed-handler
  "Once Oauth is set up, will get the access token and redirect to the
   main page."
  [request]
  (let [params (:params request)
        verifyer (:oauth_verifier params)
        oauth-token (:oauth_token params)
        request-token (:request-token (:session request))
        auth-token (oauth/get-access-token request-token verifyer)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str auth-token)
     :session {:request-token oauth-token :auth-token auth-token}}))

;; Routes
(defroutes main-routes
  (GET "/" request (index-handler request))
  (GET "/authenticated" request (authed-handler request))
  (GET "/test" request (println request))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))