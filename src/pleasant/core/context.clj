(in-ns 'pleasant.core)

(require
  '[clojure.core.strint :refer [<<]]
  '[clojure.algo.monads :refer :all]
  '[pleasant.util])

(defn connect-db []
  (domonad reader-m
           [uri (asks :uri)]
           (prn (<< "Connected to ~{uri}"))))

;; with default value

(defprotocol IContext
  (get-executor [_]))

(defn execute-some []
  (domonad reader-m
           [ctx (ask)]
           (let [e (get-executor ctx)]
             (prn e (<< "We do something with this executor : ~{e}")))))

(defn run-app [msg]
  (domonad reader-m
           [_ (connect-db)]
           (prn "application started : " msg)))

(defn run-app2 [a b c]
  (domonad reader-m
           [_ (execute-some)]
           (prn "application started : " a b c)))

;; eof
