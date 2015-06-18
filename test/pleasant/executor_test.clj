(ns pleasant.executor-test
  (:require [clojure.test :refer :all]
            [pleasant.core :as p]
            [pleasant.executor :refer :all])
  (:import (java.util.concurrent ForkJoinWorkerThread)))

(deftest basics
  (testing "Basics"
    (is (not (nil? *executor*)))
    (is (nil? (p/info *executor*)))
    (is (instance? ForkJoinWorkerThread (.newThread threadfactory *executor*)))))

;; eof
