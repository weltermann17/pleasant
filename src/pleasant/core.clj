(ns pleasant.core
  (:require
    [clojure.test :refer [run-tests]]
    [pleasant.executor-test]
    [pleasant.future-test])
  (:gen-class))

(set! *warn-on-reflection* true)

(def write-to-file spit)

(def abc 3.14)

(defn ^String f [^Number i] (pr-str i))

(defn -main
  [& _]
  (run-tests 'pleasant.executor-test)
  (run-tests 'pleasant.future-test)
  (prn "Good bye."))

(comment (-main) (write-to-file))

;; eof
