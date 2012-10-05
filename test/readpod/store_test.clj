(ns readpod.store-test
  (:use clojure.test
        readpod.store))

(deftest test-local-store
  (testing "In Memory Store Tests"
    (let [local (new-memory-store)]
      (set-key local "steve" 1)
      (set-key local "ben" 2)
      (is (= 1 (get-key local "steve")))
      (is (nil? (get-key local "freddy"))))))