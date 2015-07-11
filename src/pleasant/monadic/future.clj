(in-ns 'pleasant.monadic)

(import
  [clojure.lang IDeref Seqable]
  [java.util.concurrent Phaser TimeoutException TimeUnit])

(require
  '[clojure.core.strint :refer [<<]]
  '[clojure.algo.monads :refer :all])

;; protocols

(defprotocol IPromise
  (complete [_ value])
  (->future [_]))

(defprotocol IFuture
  (await
    [_]
    [_ milliseconds])
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
      (throw (IllegalStateException. (str (<< "A promise cannot be completed more than once, value = ~{value}, not accepted value = ~{v}"))))))
  (->future [_] future)
  IDeref
  (deref [_] @value)
  Seqable
  (seq [this] (cons this nil))
  Object
  (equals [_ other] (and (instance? Promise other) (not= @value incomplete) (= @value @other)))
  (hashCode [_] (hash @value))
  (toString [_] (pr-str @value)))

(def ^:const default-await-timeout Long/MAX_VALUE)

(def ^:dynamic *await-timeout* default-await-timeout)

(declare failed-future)

(deftype Future
  [value callbacks]
  IFuture
  (await [this] (await this *await-timeout*))
  (await [this milliseconds]
    (let [phaser (Phaser. 1)]
      (on-complete this (fn [_] (.arriveAndDeregister phaser)))
      (try
        (.awaitAdvanceInterruptibly phaser 0 milliseconds TimeUnit/MILLISECONDS)
        this
        (catch TimeoutException e
          (failed-future (TimeoutException. (<< "Timeout during await after ~{milliseconds} ms."))))
        (catch Exception e
          (failed-future e)))))
  (completed? [_] (not= @value incomplete))
  (on-complete [_ f]
    (let [v @value]
      (if (= v incomplete)
        (vswap! callbacks conj f)
        (execute f v))))
  (on-failure [this f] (on-complete this (fn [v] (when (failure? v) (f @v)))))
  (on-success [this f] (on-complete this (fn [v] (when (success? v) (f @v)))))
  IDeref
  (deref [_] @value)
  Seqable
  (seq [this] (cons this nil))
  Object
  (equals [_ other] (and (instance? Future other) (not= @value incomplete) (= @value @other)))
  (hashCode [_] (hash @value))
  (toString [_] (pr-str @value)))

;; functions

(defn promise []
  (let [value (volatile! incomplete)
        callbacks (volatile! [])
        future (->Future value callbacks)]
    (->Promise value callbacks future)))

(defn future-fn [f]
  (let [p (promise)]
    (execute (fn [] (complete p (try-fn f))))
    (->future p)))

(defn blocking-future-fn [f]
  (let [p (promise)]
    (execute-blocking (fn [] (complete p (try-fn f))))
    (->future p)))

(defn immediate-future [v]
  (let [p (promise)]
    (complete p (success v))
    (->future p)))

(defn failed-future [v]
  (let [p (promise)]
    (complete p (failure v))
    (->future p)))

;; macros

(defmacro future [& body]
  `(future-fn (fn [] ~@body)))

(defmacro blocking-future [& body]
  `(blocking-future-fn (fn [] ~@body)))

(defmacro try-future
  [& body]
  `(try
     ~@body
     (catch Exception t#
       (failed-future t#))))

;; monadic future

(defmonad
  future-m
  [m-bind (fn m-bind-future [mv f]
            (let [p (promise)]
              (on-complete
                mv (fn [a]
                     (if (success? a)
                       (on-complete (try-future (f @a)) (fn [b] (complete p b)))
                       (complete p a))))
              (->future p)))
   m-result (fn m-result-future [v] (immediate-future v))
   m-zero (fn m-zero-future [] (throw (NoSuchMethodError. "zero")))
   m-plus (fn m-plus-future [& _] (throw (NoSuchMethodError. "plus")))])

;;

(comment completed?)

;; eof
