;; Readpod needs to save the oauth tokens for verified users once they
;; authenticate so that it can pull the users reading list to create
;; the podcast. Locally we just store them in memory but in prodution
;; they will probably be in SimpleDB (or another store)
(ns readpod.store
  (:use [ring.adapter.jetty]
        [taoensso.timbre :as timbre
         :only (trace debug info warn error fatal spy)]))

(defprotocol Store
  "A key-value store for oauth tokens"
  (set-key [s user-id oauth-token] "Saves the oauth token for a user.")
  (get-key [s user-id] "Gets the oauth token for a user if it exists"))

;; An in memory implementation of the token store.
(deftype MemoryStore [storage]
  Store
  (set-key [this user-id token] (swap! storage assoc user-id token))
  (get-key [this user-id] (get @storage user-id)))

(defn new-memory-store []
  (info "Creating in memory store.")
  (MemoryStore. (atom {})))
