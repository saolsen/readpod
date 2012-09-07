(ns readpod.templates
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as s]))

;; To work with matt's design we have to generate a li in this format.
;; <li class="track clearfix">
;;   <div class="track_number">#</div>
;;   <a class="track_title" href="#">Title</a>
;;   <div class="track_meta">000 words • source</div>
;;   <div class="track_actions">
;;     <a class="action download inline-exclude" href="#">Download track</a>
;;     <a class="action read" href="#">Read now</a>
;;   </div>
;; </li>

;; Need: track number, title, #of words, url,
(def li-template
  (str "<li class=\"track clearfix\" id=\"{id}\">"
       "<div class=\"track_number\">#</div>"
       "<a class=\"track_title\" href=\"#\">{title}</a>"
       "<div class=\"track_meta\">{wordcount} words • source</div>"
       "<div class=\"track_actions\">"
       "<a class=\"action download inline-exclude\" href=\"#\">Download track</a>"
       "<a class=\"action read\" href=\"#\">Read now</a>"
       "</div>"))

(defn get-article-li
  "Takes an article and generates the html li"
  [{:keys [title wordcount id]}]
  (-> li-template
      (s/replace #"\{title\}" title)
      (s/replace #"\{wordcount\}" (str wordcount))
      (s/replace #"\{id\}" id)))

(defn link
  [url text]
  (str "<a href='" url "'>" text "</a>"))

(defn render
  [template]
  (apply str template))

;; Templates
(html/deftemplate index "templates/index.html"
  [auth-url]
  [:div#homepage_login] (html/html-content (link auth-url "Log in with Readability")))

;; Takes a list of articles in the form
;;     {:title "Article Title"
;;      :wordcount 5000
;;      :id readability-page-id}
;;
(html/deftemplate mainpage "templates/mainpage.html"
  [articles]
  [:ul.playlist] (html/html-content
                  (apply str (map get-article-li articles))))
