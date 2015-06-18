(ns pleasant.core
  (:use potemkin)
  (:require
    [taoensso.timbre]))

(import-vars

  [taoensso.timbre debug info error]

  )

(def write-to-file spit)


