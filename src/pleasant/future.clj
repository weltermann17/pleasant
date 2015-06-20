(ns pleasant.future
  (:import clojure.lang.IDeref)
  (:refer-clojure :exclude [future promise])
  (:require
    [pleasant.logging :as log]
    [pleasant.executor :refer :all]))

;; protocols

(defprotocol IPromise
  (complete [_ value])
  (->future [_]))

(defprotocol IFuture
  (on-complete [_ f])
  (completed? [_]))

;; types

(def ^:private incomplete ::incomplete)

(deftype Promise
  [value callbacks future]
  IPromise
  (complete [_ v]
    (if (= @value incomplete)
      (do (vreset! value v) (execute-all @callbacks v))
      (throw (IllegalStateException. (str "A promise cannot be completed more than once. value = " value)))))
  (->future [_] future)
  IDeref
  (deref [_] @value)
  Object
  (equals [this other] (and (instance? Promise other) (= @this @other)))
  (hashCode [this] (hash @this))
  (toString [this] (pr-str @this)))

(deftype Future
  [value callbacks]
  IFuture
  (on-complete [_ f]
    (let [v @value]
      (if (= v incomplete)
        (vswap! callbacks conj f)
        (execute f v))))
  (completed? [_] (not= @value incomplete))
  IDeref
  (deref [_] @value)
  Object
  (equals [this other] (and (instance? Promise other) (= @this @other)))
  (hashCode [this] (hash @this))
  (toString [this] (pr-str @this)))


;; functions

(defn promise []
  (let [value (volatile! incomplete)
        callbacks (volatile! [])
        future (Future. value callbacks)]
    (log/trace "promise " value)
    (Promise. value callbacks future)))

(defn future-fn [f]
  (let [p (promise)]
    (execute (fn [] (complete p (f))))
    (->future p)))

;; macros

(defmacro future [& body]
  `(future-fn (fn [] ~@body)))

(comment completed? on-complete future)

;;; eof
