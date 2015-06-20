(ns pleasant.future-test
  (:refer-clojure :exclude [future promise])
  (:require [clojure.test :refer :all]
            [pleasant.logging :as log]
            [pleasant.future :refer :all]))

(deftest basics
  (testing "Future basics")
  (is (nil? (let [f (future (log/info "inside a future") 42)]
              (Thread/sleep 1)
              (log/info "after sleep" (completed? f) ":" @f) nil))))

;; eof
