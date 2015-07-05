(ns pleasant.core
  (:require
    [clojure.test :refer [run-tests]]
    [pleasant.util.fatal :refer :all]
    [pleasant.executor-test]
    [pleasant.future-test])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn -main
  [& _]
  (check-java-version ["1.8" "1.9"])
  (run-tests 'pleasant.executor-test)
  (run-tests 'pleasant.future-test))

(comment -main)

;; eof
