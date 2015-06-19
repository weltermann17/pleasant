(ns pleasant.core
  (:use potemkin)
  (:require
    [clojure.test :refer :all]
    [clojure.core.typed :refer [ann cf check-ns-info]]
    [pleasant.executor-test])
  (:gen-class))

(set! *warn-on-reflection* true)

(ann ^:no-check clojure.core/spit Object)

(ann write-to-file Object)

(def write-to-file spit)

(ann abc Number)

(def abc 3.14)

(ann f [Number -> String])

(defn ^String f [^Number i] (pr-str i))

(defn -main
  "I don't do a whole lot ... yet."
  [& _]
  (prn "Hello, World!")
  ; (check-ns-info)
  (run-tests 'pleasant.executor-test)
  (prn "Good bye."))

(comment (-main))

;; eof
