(ns readpod.web
  (:require [readpod.api :as api])
  (:use ring.adapter.jetty))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (run-jetty api/app {:port port})))
