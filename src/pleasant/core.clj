(ns pleasant.core
  (:use potemkin)
  (:require
    [clojure.core.typed :refer [ann cf]]
    [taoensso.timbre]))

(ann ^:no-check potemkin.namespaces/link-vars Object)

(import-vars

  [taoensso.timbre debug info error]

  )

(ann ^:no-check clojure.core/spit Object)

(ann write-to-file Object)

(def write-to-file spit)

(ann abc Number)

(def abc 3.14)

;; eof
