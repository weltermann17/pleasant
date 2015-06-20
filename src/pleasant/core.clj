(ns pleasant.core
  (:require
    [clojure.test :refer [run-tests]]
    [pleasant.executor-test])
  (:gen-class))

(set! *warn-on-reflection* true)

(def write-to-file spit)

(def abc 3.14)

(defn ^String f [^Number i] (pr-str i))

(defn -main
  [& _]
  (run-tests 'pleasant.executor-test)
  (prn "Good bye."))

(comment (-main) (write-to-file))

;; eof
