(in-ns 'pleasant.protocolmonad)

(require
  '[clojure.core.match :refer [match]]
  '[clojure.core.match.protocols :refer [IMatchLookup]])

(declare Future future)

(defprotocol Promise
  (complete [_ v])
  (->future [_]))

(defprotocol Future
  (await [_ milliseconds])
  (on-success [_ f])
  (on-failure [_ f])
  (on-complete [_ f]))

(defn promise []
  (let [value (volatile! ::incomplete)
        callbacks (volatile! [])
        fut (future value callbacks)]
    (reify
      Promise
      (complete [_ v] nil)
      (->future [_] fut))))

(defn future [value callbacks]
  (reify
    Future
    (await [_ _] nil)
    (on-complete [_ _] nil)
    (on-success [_ _] nil)
    (on-failure [_ _] nil)))

;; eof
