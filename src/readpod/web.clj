;; Main entry point to the web process. If run in dev mode (see below)
;; will also create an in memory queue and an internal worker process.
(ns readpod.web
  (:require [readpod.api :as api]
            [readpod.queue :as q]
            [readpod.worker :as w])
  (:use [ring.adapter.jetty]
        [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)])
  (:gen-class))

;; To run in dev mode which starts the worker process internally.
;;
;;    lein run -m readpod.web dev
;;
(defn -main [& args]
  (let [dev? (first args)
        port (Integer/parseInt (System/getenv "PORT"))
        queue (if dev? (q/new-memory-queue) (q/connect-prod-queue))
        store nil
        worker (if dev? (future (w/start-worker queue)) nil)]
    (info "Starting Web Process")
    (run-jetty (api/get-app queue store) {:port port})))
