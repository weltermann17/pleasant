(in-ns 'pleasant.protocolmonad)

(require
  '[clojure.core.match :refer [match]]
  '[clojure.core.match.protocols :refer [IMatchLookup]])

(declare maybe)
(declare nothing)

(defprotocol Maybe)
(defprotocol Nothing)
(defprotocol Just
  (value [m] "extract value"))

(defn just [v]
  (reify
    Maybe
    Just
    (value [_] v)

    Object
    (equals [_ o] (and (satisfies? Just o) (= v (value o))))
    (toString [_] (str "Just " v))

    Functor
    (-fmap [_ f] (just (f v)))

    Pure
    (-pure [_ u] (just u))

    Applicative
    (-ap [_ m] (maybe nothing (comp just v) m))

    Monad
    (-bind [_ f] (f v))

    IMatchLookup
    (val-at [_ k not-found]
      (case k
        :just v
        not-found))))

(def nothing
  (reify
    Maybe
    Nothing

    Object
    (toString [_] "Nothing")
    (equals [_ o] (satisfies? Nothing o))

    Functor
    (-fmap [_ f] nothing)

    Pure
    (-pure [_ u] (just u))

    Applicative
    (-ap [_ f] nothing)

    Monad
    (-bind [_ f] nothing)

    IMatchLookup
    (val-at [_ k not-found]
      (case k
        :nothing nil
        not-found))))

(defn maybe [d f m]
  (match [m]
         [{:just v}] (f v)
         [{:nothing _}] d))


;; eof
