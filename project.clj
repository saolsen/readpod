(defproject readpod "0.1.0-SNAPSHOT"
  :description "Audio podcast of readability reading list."
  :url "http://readpod.herokuapp.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring/ring-core "1.1.0"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [compojure "1.1.0" :exclude [org.clojure/clojure "1.2.0"]]
                 [enlive "1.0.0"]
                 [clj-oauth "1.3.1-SNAPSHOT"]
                 [org.clojars.tavisrudd/clj-apache-http "2.3.2-SNAPSHOT"]
                 [org.clojure/data.json "0.1.3"]
;                 [org.mobicents.external.freetts/cmulex "1.0"]
;                 [org.mobicents.external.freetts/freetts "1.0"]
;                 [org.mobicents.external.freetts/cmudict04 "1.0"]
;                 [org.mobicents.external.freetts/cmutimelex "1.0"]
;                 [org.mobicents.external.freetts/cmu_time_awb "1.0"]
;                 [org.mobicents.external.freetts/en_us "1.0"]
;                 [org.mobicents.external.freetts/cmu_us_kal "1.0"]
                 [com.taoensso/timbre "0.8.0"]
                 [clj-aws-s3 "0.3.2"]
                 [com.cemerick/bandalore "0.0.3"]
                 [com.cemerick/rummage "1.0.1"]
                 ]
  :plugins [[lein-marginalia "0.7.1"]
            [lein-midje "2.0.0-SNAPSHOT"]
            [lein-swank "1.4.4"]]
  :ring {:handler readpod.api/app})
