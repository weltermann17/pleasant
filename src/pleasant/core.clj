(ns pleasant.core
  (:refer-clojure :exclude [await future promise])
  (:require
    [pleasant.util :refer :all])
  (:gen-class))

(set! *warn-on-reflection* true)

(load "core/configuration")
(load "core/context")
(load "core/component")
(load "core/application")

(defn -main
  [& _]
  (try
    (require-minimum-java-version "1.8.0_45")
    (catch Throwable e (*fatal-exception-handler* e))))

(comment -main)

;; eof
