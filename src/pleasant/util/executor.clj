(in-ns 'pleasant.util)

(import
  (java.lang
    Thread$UncaughtExceptionHandler)
  (java.util.concurrent
    Executor
    ExecutorService
    ForkJoinPool
    ThreadFactory
    ForkJoinPool$ForkJoinWorkerThreadFactory
    ForkJoinTask
    ForkJoinWorkerThread
    ForkJoinPool$ManagedBlocker
    RecursiveAction))

(require
  '[clojure.algo.monads :refer :all]
  '[pleasant.util :refer :all])

;; helpers

(defn default-reporter [^Throwable e] (error "reporter :" e))

(def ^:dynamic *reporter* default-reporter)

(def uncaught-exception-handler
  (reify Thread$UncaughtExceptionHandler (uncaughtException [_ _ e] (*reporter* e))))

(def ^ForkJoinPool$ForkJoinWorkerThreadFactory threadfactory
  (let
    [init (fn [^Thread t]
            (doto t
              (.setDaemon true)
              (.setUncaughtExceptionHandler uncaught-exception-handler)))]
    (reify
      ThreadFactory
      (^Thread newThread [_ ^Runnable r]
        (init (Thread. r)))
      ForkJoinPool$ForkJoinWorkerThreadFactory
      (^ForkJoinWorkerThread newThread [_ ^ForkJoinPool p]
        (init (proxy [ForkJoinWorkerThread] [p]))))))

(def ^Long ^:const parallelism (* 2 (.availableProcessors (Runtime/getRuntime))))

(def default-executor
  (ForkJoinPool.
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

;; execute

(defmulti execute (fn ([_ _] (type *executor*)) ([_] (type *executor*))))

(defmulti execute-blocking (fn ([_ _] (type *executor*)) ([_] (type *executor*))))

(defmethod execute ForkJoinPool
  ([f value]
    (execute (fn [] (f value))))
  ([f]
    (let [executor *executor*
          action (recursive-action executor f)]
      (if (ForkJoinTask/inForkJoinPool)
        (.fork action)
        (.execute (cast ForkJoinPool executor) action)))))

(defmethod execute :default
  ([f value]
    (execute (fn [] (f value))))
  ([f]
    (let [executor *executor*]
      (.execute ^Executor executor (binding [*executor* executor] (f))))))

;; blocking

(defmethod execute-blocking ForkJoinPool
  ([f value] (execute-blocking (fn [] (f value))))
  ([f] (let [executor *executor*
             action (recursive-action executor (fn [] (ForkJoinPool/managedBlock (managed-blocker f))))]
         (if (ForkJoinTask/inForkJoinPool)
           (.fork action)
           (.execute (cast ForkJoinPool executor) action)))))

(defmethod execute-blocking :default
  ([f value] (execute (fn [] (f value))))
  ([f] (execute f)))

(defn execute-all [fs value] (doseq [f fs] (execute f value)))

(comment execute execute-all execute-blocking)

;; just playing

(defrecord ExecutorConfiguration
  [parallelism-factor
   parallelism
   forkjoin-executor])

(defn default-parallelism-factor [] (domonad reader-m [] 2.0))

(defn default-parallelism []
  (domonad
    reader-m
    [pf (asks :parallelism-factor)
     p (pf)]
    (max 2 (int (* p (.availableProcessors (Runtime/getRuntime)))))))

(defn default-forkjoin-executor []
  (domonad
    reader-m
    [p' (asks :parallelism)
     p (p')]
    (str "forkjoinpool:p=" p)))

(defn default-executor-configuration []
  (map->ExecutorConfiguration
    {:parallelism-factor default-parallelism-factor
     :parallelism        default-parallelism
     :forkjoin-executor  default-forkjoin-executor}
    ))

(defrecord ExecutorContext
  [^ExecutorService executor])

(defn executor-context []
  (domonad reader-m
           [e' (asks :forkjoin-executor)
            e (e')]
           (map->ExecutorContext {:executor e})))

(defn test-executor []
  (domonad reader-m [e (asks :executor)] (println "my-executor : " e)))




;; eof
