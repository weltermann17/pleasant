(ns pleasant.core
  (:use potemkin)
  (:require
    [clojure.core.typed :refer [ann cf]]))

(ann ^:no-check clojure.core/spit Object)

(ann write-to-file Object)

(def write-to-file spit)

(ann abc Number)

(def abc 3.14)

(ann f [Number -> String])

(defn ^String f [^Number i] (pr-str i))

;; eof
