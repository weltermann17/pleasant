(ns pleasant.monadic.try
  (:import
    clojure.lang.IDeref)
  (:refer-clojure :exclude [])
  (:require
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
  (equals [this other] (and (instance? Success other) (= @this @other)))
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

(defn fatal? [^Throwable e]
  (some #(instance? % e) [InterruptedException
                          LinkageError
                          ThreadDeath
                          VirtualMachineError]))

(def default-fatal-exception-handler
  (fn [^Throwable e]
    (when (fatal? e)
      (future
        (do (Thread/sleep 1000)
            (log/fatal "Fatal exception :" e)
            (log/fatal "Terminate JVM runtime now.")
            (future
              (Thread/sleep 2000)
              (println "JVM runtime halted.")
              (.halt (Runtime/getRuntime) 1))
            (.exit (Runtime/getRuntime) 1))))))

(def ^:dynamic *fatal-exception-handler* default-fatal-exception-handler)

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

;; eof
