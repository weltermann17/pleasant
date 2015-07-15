(in-ns 'pleasant.core)

(defprotocol IComponent
  (start [_])
  (stop [_]))

(comment IComponent start stop)

;; eof
