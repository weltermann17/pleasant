(in-ns 'pleasant.monad)

(import
  java.util.NoSuchElementException
  [clojure.lang IDeref Seqable])

(require
  '[clojure.core.strint :refer [<<]]
  '[clojure.algo.monads :refer :all])

(defprotocol IOption
  (^Boolean defined? [_])
  (^Boolean undefined? [_]))

(deftype ^:private Some [value]
  IOption
  (defined? [_] true)
  (undefined? [_] false)
  IDeref
  (deref [_] value)
  Seqable
  (seq [this] (cons this nil))
  Object
  (equals [_ other] (and (instance? Some other) (= value @other)))
  (hashCode [_] (hash value))
  (toString [_] (<< "Some(~{value})")))

(def None
  (reify
    IOption
    (defined? [_] false)
    (undefined? [_] true)
    IDeref
    (deref [_] (throw (NoSuchElementException. "Cannot deref None.")))
    Seqable
    (seq [this] (cons this nil))
    Object
    (toString [_] "None")))

(defn option [value] (if value (->Some value) None))

(defmonad
  option-m
  [m-bind (fn m-bind-option [mv f] (if (defined? mv) (f @mv) None))
   m-result (fn m-result-option [v] (option v))
   m-zero (fn m-zero-option [] None)
   m-plus (fn m-plus-option [& mvs] (let [x (first (drop-while undefined? mvs))] (if x x None)))])

;; eof
