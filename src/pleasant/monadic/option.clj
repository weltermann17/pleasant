(ns pleasant.monadic.option
  (:import clojure.lang.IDeref)
  (:refer-clojure :exclude [empty?])
  (:require [clojure.algo.monads :refer :all]))

(defprotocol IOption
  (empty? [_])
  (defined? [_]))

(deftype Some [value]
  IDeref
  (deref [_] value)
  IOption
  (empty? [_] false)
  (defined? [_] true))

(def None
  (reify IOption (empty? [_] true) (defined? [_] false) IDeref (deref [_] nil)))

(defn option [value] (if value (Some. value) None))

;; eof
