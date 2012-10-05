(ns readpod.queue-test
  (:use clojure.test
        readpod.queue))

(deftest test-local-queue
  (testing "Test the local queue"
    (let [local (new-memory-queue)
          test (atom [])]
      (process local #(swap! test conj %))
      (enqueue local 1)
      (enqueue local 2)
      (enqueue local 3)
      (is (= [1 2 3] @test)))))