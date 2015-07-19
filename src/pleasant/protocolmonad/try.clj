(in-ns 'pleasant.protocolmonad)

(require
  '[clojure.core.match :refer [match]]
  '[clojure.core.match.protocols :refer [IMatchLookup]])

(defprotocol Try)
(defprotocol Success
  (-success [m]))
(defprotocol Failure
  (-failure [m]))

(defn success [v]
  (reify
    Try
    Success
    (-success [_] v)

    Object
    (equals [_ o] (and (satisfies? Success o) (= v (-success o))))
    (toString [_] (str "Success " v))

    Functor
    (-fmap [_ f] (success (f v)))

    Pure
    (-pure [_ u] (success u))

    Monad
    (-bind [_ f] (f v))

    IMatchLookup
    (val-at [_ k not-found]
      (case k
        :success v
        not-found))))

(defn failure [v]
  (reify
    Either
    Failure
    (-failure [_] v)

    Object
    (equals [_ o] (and (satisfies? Failure o) (= v (-failure o))))
    (toString [_] (str "Failure " v))

    Functor
    (-fmap [this _] this)

    Pure
    (-pure [_ u] (success u))

    Monad
    (-bind [m _] m)

    IMatchLookup
    (val-at [_ k not-found]
      (case k
        :failure v
        not-found))))

(defn match-try [f g t]
  (match [t]
         [{:failure v}] (f v)
         [{:success v}] (g v)))

(comment failure match-try)

;; eof
