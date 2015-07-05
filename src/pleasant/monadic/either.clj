(ns pleasant.monadic.either
  (:import
    clojure.lang.IDeref)
  (:require
    [clojure.core.strint :refer [<<]]
    [clojure.algo.monads :refer :all]))

(defprotocol IEither
  (left? [_])
  (right? [_]))

(deftype Left [value]
  IEither
  (left? [_] true)
  (right? [_] false)
  IDeref
  (deref [_] value)
  Object
  (equals [this other] (and (instance? Left other) (= @this @other)))
  (hashCode [this] (hash @this))
  (toString [this] (comment this) (<< "Left(~{@this})")))

(deftype Right [value]
  IEither
  (left? [_] false)
  (right? [_] true)
  IDeref
  (deref [_] value)
  Object
  (equals [this other] (and (instance? Right other) (= @this @other)))
  (hashCode [this] (hash @this))
  (toString [this] (comment this) (<< "Right(~{@this})")))

(defn left [value] (->Left value))

(defn right [value] (->Right value))

(defmonad either-m
          [m-result (fn m-result-either [v] nil)
           m-bind (fn m-result-either [mv f] nil)
           m-zero (fn m-result-either [] nil)
           m-plus (fn m-result-either [& mvs] nil)])

(comment left right)

;; eof
