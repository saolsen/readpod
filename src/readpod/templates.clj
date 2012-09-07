(ns readpod.templates
  (:require [net.cgrand.enlive-html :as html]))

;; Helpers
(defn render
  [template]
  (apply str template))

(defn link
  [url text]
  (str "<a href='" url "'>" text "</a>"))

(defn article-list-item
  [name link]
  (str "<li>" name " | " link "</li>"))

(defn article-url
  [id]
  (str "/article/" id ".wav"))

;; Templates
(html/deftemplate index "templates/index.html"
  [auth-url]
  [:div#homepage_login] (html/html-content (link auth-url "Log in with Readability")))

(html/deftemplate mainpage "templates/mainpage.html"
  [title-id-pairs]
  [:ul#titles] (html/html-content
                (apply str (map
                            #(article-list-item
                              (:title %)
                              (link (article-url (:id %)) "Audio"))
                            title-id-pairs))))
