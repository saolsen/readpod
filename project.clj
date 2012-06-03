(defproject readpod "0.1.0-SNAPSHOT"
  :description "Audio podcast of readability reading list."
  :url "http://readpod.herokuapp.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.1.0"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [compojure "1.1.0"]
                 [enlive "1.0.0"]
                 [clj-oauth "1.3.1-SNAPSHOT"]
                 [clj-apache-http "2.3.2-SNAPSHOT"]
                 [org.clojure/data.json "0.1.2"]
                 [org.mobicents.external.freetts/cmulex "1.0"]
                 [org.mobicents.external.freetts/freetts "1.0"]
                 [org.mobicents.external.freetts/cmudict04 "1.0"]
                 [org.mobicents.external.freetts/cmutimelex "1.0"]
                 [org.mobicents.external.freetts/cmu_time_awb "1.0"]
                 [org.mobicents.external.freetts/en_us "1.0"]
                 [org.mobicents.external.freetts/cmu_us_kal "1.0"]]
  :repositories {"local" ~(str (.toURI (java.io.File. "maven_local")))}
  :ring {:handler readpod.api/app})