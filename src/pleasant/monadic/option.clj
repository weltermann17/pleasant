(ns pleasant.monadic.option
  (:import
    java.util.NoSuchElementException
    clojure.lang.IDeref)
  (:refer-clojure :exclude [])
  (:require
    [clojure.core.strint :refer [<<]]
    [clojure.algo.monads :refer :all]))

(defprotocol IOption
  (undefined? [_])
  (defined? [_]))

(deftype Some [value]
  IOption
  (undefined? [_] false)
  (defined? [_] true)
  IDeref
  (deref [_] value)
  Object
  (equals [this other] (and (instance? Some other) (= @this @other)))
  (hashCode [this] (hash @this))
  (toString [this] (comment this) (<< "Some(~{@this})")))

(def None
  (reify
    IOption
    (undefined? [_] true)
    (defined? [_] false)
    IDeref
    (deref [_] (throw (NoSuchElementException. "Cannot deref None.")))
    Object
    (toString [_] "None")))

(defn option [value] (if value (Some. value) None))

(defmonad
  option-m
  [m-bind (fn m-bind-option [mv f] (if (defined? mv) (f @mv) None))
   m-result (fn m-result-option [v] (option v))
   m-zero (fn m-zero-option [] None)
   m-plus (fn m-plus-option [& mvs] (let [x (first (drop-while undefined? mvs))] (if x x None)))])

(comment some none)

;; eof
