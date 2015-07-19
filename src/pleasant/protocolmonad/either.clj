(in-ns 'pleasant.protocolmonad)

(require
  '[clojure.core.match :refer [match]]
  '[clojure.core.match.protocols :refer [IMatchLookup]])

(defprotocol Either)
(defprotocol Left
  (-left [m]))
(defprotocol Right
  (-right [m]))

(defn right [v]
  (reify
    Either
    Right
    (-right [_] v)

    Object
    (equals [_ o] (and (satisfies? Right o) (= v (-right o))))
    (toString [_] (str "Right " v))

    Functor
    (-fmap [_ f] (right (f v)))

    Pure
    (-pure [_ u] (right u))

    Monad
    (-bind [_ f] (f v))

    IMatchLookup
    (val-at [_ k not-found]
      (case k
        :right v
        not-found))))

(defn left [v]
  (reify
    Either
    Left
    (-left [_] v)

    Object
    (equals [_ o] (and (satisfies? Left o) (= v (-left o))))
    (toString [_] (str "Left " v))

    Functor
    (-fmap [this _] this)

    Pure
    (-pure [_ u] (right u))                                 ;; yes, right

    Monad
    (-bind [m _] m)

    IMatchLookup
    (val-at [_ k not-found]
      (case k
        :left v
        not-found))))

(defn either [f g e]
  (match [e]
         [{:left v}] (f v)
         [{:right v}] (g v)))

(defn mirror [e]
  (match [e]
         [{:left v}] (right v)
         [{:right v}] (left v)))

(comment either)

;; eof
