(ns pleasant.executor-test
  (:require
    [clojure.test :refer :all]
    [pleasant.util :refer :all])
  (:import
    (java.util.concurrent ForkJoinWorkerThread)))

(deftest basics
  (testing "Basics"
    (is (not (nil? *executor*)))
    (is (nil? (trace *executor*)))
    (is (instance? ForkJoinWorkerThread (.newThread threadfactory *executor*)))
    ))

;; eof
