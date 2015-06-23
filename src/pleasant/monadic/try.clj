(ns pleasant.monadic.try
  (:import
    clojure.lang.IDeref)
  (:refer-clojure :exclude [])
  (:require
    [pleasant.util.logging :as log]
    [clojure.algo.monads :refer :all]))

(defprotocol ITry
  (success? [_])
  (failure? [_]))

(deftype Success [value]
  IDeref
  (deref [_] value)
  ITry
  (success? [_] true)
  (failure? [_] false))

(deftype Failure [value]
  IDeref
  (deref [_] value)
  ITry
  (success? [_] false)
  (failure? [_] true))

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
              (prn "JVM runtime halted.")
              (.halt (Runtime/getRuntime) 1))
            (.exit (Runtime/getRuntime) 1))))))

(def ^:dynamic *fatal-exception-handler* default-fatal-exception-handler)

(defn ->try [f]
  (try
    (success (f))
    (catch Throwable e (when (fatal? e) (*fatal-exception-handler* e))
                       (failure e))))

;; eof
