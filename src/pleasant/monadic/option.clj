(ns pleasant.monadic.option
  (:import
    java.util.NoSuchElementException
    clojure.lang.IDeref)
  (:refer-clojure :exclude [empty?])
  (:require
    [clojure.core.strint :refer [<<]]
    [clojure.algo.monads :refer :all]))

(defprotocol IOption
  (empty? [_])
  (defined? [_]))

(deftype Some [value]
  IOption
  (empty? [_] false)
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
    (empty? [_] true)
    (defined? [_] false)
    IDeref
    (deref [_] (throw (NoSuchElementException. "Cannot deref None.")))
    Object
    (toString [_] "None")))

(defn option [value] (if value (Some. value) None))

(defmonad
  option-m
  [m-result (fn m-result-option [v] (option v))
   m-bind (fn m-bind-option [mv f] (if (defined? mv) (f (deref mv)) None))
   m-plus (fn m-plus-option [& mvs] (first (drop-while (comp empty? option) mvs)))
   m-zero None])

;; eof
