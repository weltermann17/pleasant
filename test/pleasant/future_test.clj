(ns pleasant.future-test
  (:refer-clojure :exclude [await future promise])
  (:require [clojure.test :refer :all]
            [pleasant.logging :as log]
            [pleasant.executor :refer :all]
            [pleasant.future :refer :all]))

(deftest basics
  (testing "Future basics"
    (is (= 42 (let [f (blocking-future (log/trace "inside a future 1") (Thread/sleep 100) 42)]
                (Thread/sleep 200) @f)))
    (is (= 43 (let [f (future (log/trace "inside a future 2") (Thread/sleep 100) 43)]
                @(await f 10000))))
    (is (nil? (log/trace *executor*)))
    ))

;; eof
