(ns readpod.web
  (:require [readpod.api :as api])
  (:use ring.adapter.jetty))

;; How heroku starts it, locally I always use
;; $ lein ring server
;; Because it reloads every file that you save.

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty api/app {:port port})))
