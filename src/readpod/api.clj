(ns readpod.api
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [readpod.readability :as read]))

(defn render
  [template]
  (apply str template))

;; Templates
(html/deftemplate index "templates/index.html"
  [auth-url]
  [:div#login] (html/html-content
                (str "<a href='" auth-url "'>Login</a>")))

;; Routes
(defroutes main-routes
  (GET "/" [] (render (index "test-link")))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))