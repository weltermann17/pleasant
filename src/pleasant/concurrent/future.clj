(ns pleasant.concurrent.future
  (:import
    clojure.lang.IDeref
    [java.util.concurrent Phaser TimeoutException TimeUnit])
  (:refer-clojure :exclude [await future promise])
  (:require
    [clojure.core.strint :refer [<<]]
    [pleasant.util.logging :as log]
    [pleasant.concurrent.executor :refer :all]
    [pleasant.monadic.try :refer :all]))

;; protocols

(defprotocol IPromise
  (complete [_ value])
  (->future [_]))

(defprotocol IFuture
  (await [_ milliseconds])
  (on-success [_ f])
  (on-failure [_ f])
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
      (throw (IllegalStateException. (str (<< "A promise cannot be completed more than once, value = ~{value}"))))))
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
  (await [this milliseconds]
    (let [phaser (Phaser. 1)]
      (on-complete this (fn [_] (.arriveAndDeregister phaser)))
      (try
        (.awaitAdvanceInterruptibly phaser 0 milliseconds TimeUnit/MILLISECONDS)
        this
        (catch TimeoutException _
          (throw (TimeoutException. (<< "Timeout during await after ~{milliseconds} ms."))))
        (catch Throwable e
          (throw e)))))
  (completed? [_] (not= @value incomplete))
  (on-success [this f] (on-complete this (fn [v] (when (success? v) (f @v)))))
  (on-failure [this f] (on-complete this (fn [v] (when (failure? v) (f @v)))))
  (on-complete [_ f]
    (let [v @value]
      (if (= v incomplete)
        (vswap! callbacks conj f)
        (execute f v))))
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
    (Promise. value callbacks future)))

(defn future-fn [f]
  (let [p (promise)]
    (execute (fn [] (complete p (->try f))))
    (->future p)))

(defn blocking-future-fn [f]
  (let [p (promise)]
    (execute-blocking (fn [] (complete p (->try f))))
    (->future p)))

;; macros

(defmacro future [& body]
  `(future-fn (fn [] ~@body)))

(defmacro blocking-future [& body]
  `(blocking-future-fn (fn [] ~@body)))

(comment completed? on-complete future blocking-future on-success on-failure)

;;; eof
