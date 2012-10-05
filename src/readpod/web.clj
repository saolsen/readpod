;; Main entry point to the web process. If run in dev mode (see below)
;; will also create an in memory queue and an internal worker process.
(ns readpod.web
  (:require [readpod.api :as api]
            [readpod.core :as core]
            [readpod.env :as env])
  (:use [ring.adapter.jetty]
        [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)])
  (:gen-class))

;; To run in dev mode which starts the worker process internally.
;;
;;    lein run -m readpod.web dev
;;
;; NO THIS IS REDICULOUS!
(defn -main [& args]
  (info "Starting Web Process")
  (env/bind-vars)
  (core/setup-connections)
  (run-jetty api/api {:port (Integer/parseInt (System/getenv "PORT"))}))
