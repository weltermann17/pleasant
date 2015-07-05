(ns pleasant.monadic.try
  (:import
    clojure.lang.IDeref)
  (:refer-clojure :exclude [])
  (:require
    [pleasant.util.fatal :refer :all]
    [pleasant.util.logging :as log]
    [clojure.core.strint :refer [<<]]
    [clojure.algo.monads :refer :all]))

(defprotocol ITry
  (success? [_])
  (failure? [_]))

(deftype Success [value]
  ITry
  (success? [_] true)
  (failure? [_] false)
  IDeref
  (deref [_] value)
  Object
  (equals [_ other] (and (instance? Success other) (= value @other)))
  (hashCode [this] (hash @this))
  (toString [this] (comment this) (<< "Success(~{@this})")))

(deftype Failure [value]
  ITry
  (success? [_] false)
  (failure? [_] true)
  IDeref
  (deref [_] value)
  Object
  (equals [this other] (and (instance? Failure other) (= @this @other)))
  (hashCode [this] (hash @this))
  (toString [this] (comment this) (<< "Failure(~{@this})")))


(defn success [v] (Success. v))

(defn failure [v] (Failure. v))

(defn try-fn [f]
  (try
    (success (f))
    (catch Throwable e (when (fatal? e) (*fatal-exception-handler* e))
                       (failure e))))

(defmacro ->try [& body]
  `(try
     (success (do ~@body))
     (catch Throwable e# (when (fatal? e#) (*fatal-exception-handler* e#))
                         (failure e#))))

(defmonad
  try-m
  [m-result (fn m-result-try [v] (->try v))
   m-bind (fn m-result-try [mv f] (if (success? mv) (f (deref mv)) mv))])

(comment log/info)

;; eof
