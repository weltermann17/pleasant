(ns pleasant.executor
  (:require [pleasant.core :as p])
  (:import
    (java.util.concurrent
      ForkJoinPool ThreadFactory ForkJoinPool$ForkJoinWorkerThreadFactory ForkJoinWorkerThread ForkJoinPool$ManagedBlocker)
    (java.lang
      Thread$UncaughtExceptionHandler)))

(set! *warn-on-reflection* true)

(def parallelism (* 2 (.availableProcessors (Runtime/getRuntime))))

(declare ^:dynamic *reporter*)

(def uncaught-exception-handler
  (reify Thread$UncaughtExceptionHandler (uncaughtException [_ _ e] (*reporter* e))))

(definterface Blocking
  (block [f]))

(def ^ForkJoinPool$ForkJoinWorkerThreadFactory threadfactory
  (let
    [wire (fn [^Thread t]
            (p/debug t)
            (p/write-to-file "/tmp/bla.txt" (prn-str t))
            (doto t
              (.setDaemon true)
              (.setUncaughtExceptionHandler uncaught-exception-handler)))]
    (reify
      ThreadFactory
      (^Thread newThread [_ ^Runnable r]
        (wire (Thread. r)))
      ForkJoinPool$ForkJoinWorkerThreadFactory
      (^ForkJoinWorkerThread newThread [_ ^ForkJoinPool p]
        (wire (proxy
                [ForkJoinWorkerThread Blocking] [p]
                (block [f]
                  (let [result (volatile! nil)
                        done (volatile! false)
                        blocker (reify ForkJoinPool$ManagedBlocker
                                  (block [_] (try (vreset! result (f)) (finally (vreset! done true))))
                                  (isReleasable [_] @done))]
                    (ForkJoinPool/managedBlock blocker) @result))))))))

(defn default-reporter [^Throwable e] (p/error e))

(def default-executor (ForkJoinPool. parallelism threadfactory uncaught-exception-handler true))

(def default-blocking (reify Blocking (block [_ f] (f))))

(def ^:dynamic *reporter* default-reporter)

(def ^:dynamic *executor* default-executor)

(def ^:dynamic *blocking* default-blocking)

;; eof
