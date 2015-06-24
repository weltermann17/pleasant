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

(declare option-m)

(defmonad
  option-m
  [m-zero None
   m-result (fn [v] (option v))
   m-bind (fn [mv f] (if (empty? (option mv)) None (f mv)))
   m-plus (fn [& mvs] (first (drop-while (comp option empty?) mvs)))])

;; tests

(def sequence-maybe-m (maybe-t sequence-m))

(println (domonad sequence-maybe-m [a [nil 1 nil] b [10 20]] (+ a b)))

(with-monad option-m (def ^:private option-+ (m-lift 2 +)))

(println (map (comp #(.toString %) identity) (domonad sequence-m [a [nil 1 nil] b [10 20]] (option-+ a b))))

;; eof
