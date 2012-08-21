(ns readpod.queue-test
  (:use clojure.test
        readpod.queue))

(deftest test-local-queue
  (testing "some stuff"
    (let [local (get-local-queue)]
      (send-message local 1)
      (send-message local 2)
      (send-message local 3)
      (is (= 2 (consume-message local #(+ 1 %))))
      (is (= 2 (consume-message local identity)))
      (send-message local 4)
      (is (= 3 (consume-message local identity)))
      (is (= 4 (consume-message local identity))))))