(ns pleasant.core
  (:refer-clojure :exclude [await future promise])
  (:require
    [clojure.test :refer [run-tests]]
    [pleasant.util :refer :all]
    [pleasant.executor-test]
    [pleasant.future-test])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn -main
  [& _]
  (try
    (require-minimum-java-version "1.8.0_45")
    (run-tests 'pleasant.executor-test)
    (run-tests 'pleasant.future-test)
    (catch Throwable e (*fatal-exception-handler* e))))

(comment -main)

;; eof
