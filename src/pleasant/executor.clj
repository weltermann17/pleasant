(ns pleasant.executor
  (:import
    (java.lang
      Thread$UncaughtExceptionHandler)
    (java.util.concurrent
      Executor
      ForkJoinPool
      ThreadFactory
      ForkJoinPool$ForkJoinWorkerThreadFactory
      ForkJoinTask
      ForkJoinWorkerThread
      ForkJoinPool$ManagedBlocker
      RecursiveAction))
  (:require
    [pleasant.logging :as log]))

(def ^Long parallelism (* 2 (.availableProcessors (Runtime/getRuntime))))

(defn default-reporter [^Throwable e] (log/error e))

(def ^:dynamic *reporter* default-reporter)

(def uncaught-exception-handler
  (reify Thread$UncaughtExceptionHandler (uncaughtException [_ _ e] (*reporter* e))))

(def ^ForkJoinPool$ForkJoinWorkerThreadFactory threadfactory
  (let
    [wire (fn [^Thread t]
            (log/debug t)
            (doto t
              (.setDaemon true)
              (.setUncaughtExceptionHandler uncaught-exception-handler)))]
    (reify
      ThreadFactory
      (^Thread newThread [_ ^Runnable r]
        (wire (Thread. r)))
      ForkJoinPool$ForkJoinWorkerThreadFactory
      (^ForkJoinWorkerThread newThread [_ ^ForkJoinPool p]
        (wire (proxy [ForkJoinWorkerThread] [p]))))))

(def default-executor (ForkJoinPool.
                        parallelism
                        threadfactory
                        uncaught-exception-handler
                        true))

(def ^:dynamic *executor* default-executor)

(defn- recursive-action [executor f]
  (proxy [RecursiveAction] [] (compute [] (binding [*executor* executor] (f)))))

(defn- managed-blocker [f]
  (let [done (volatile! false)]
    (reify ForkJoinPool$ManagedBlocker
      (block [_] (try (f) (finally (vreset! done true))) true)
      (isReleasable [_] @done))))

(defmulti execute (fn ([_ _] (type *executor*)) ([_] (type *executor*))))

(defmethod execute ForkJoinPool
  ([f value]
    (fn [] (f value)))
  ([f]
    (let [executor *executor*
          action (recursive-action executor f)]
      (if (ForkJoinTask/inForkJoinPool)
        (.fork action)
        (.execute (cast ForkJoinPool executor) action)))))

(defmethod execute :default
  ([f value]
    (fn [] (f value)))
  ([f]
    (let [executor *executor*]
      (.execute ^Executor executor (binding [*executor* executor] (f))))))

(defn execute-all [fs value] (doseq [f fs] (execute f value)))

(defn execute-blocking
  ([f value] (fn [] (f value)))
  ([f] (let [executor *executor*
             action (recursive-action executor (fn [] (ForkJoinPool/managedBlock (managed-blocker f))))]
         (if (ForkJoinTask/inForkJoinPool)
           (.fork action)
           (.execute ^Executor executor action)))))

(comment execute execute-all execute-blocking)

;; eof
