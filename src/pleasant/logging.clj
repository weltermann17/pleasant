(ns pleasant.logging
  (:use potemkin)
  (:require
    [clojure.core.typed :refer [ann]]
    [taoensso.timbre :as logger]
    [taoensso.timbre.appenders.core :as basic])
  )

(set! *warn-on-reflection* true)

(ann ^:no-check potemkin.namespaces/link-vars Object)

(import-vars

  [taoensso.timbre trace debug info warn error fatal]

  )
(def default-config
  {

   :level          :trace

   :ns-whitelist   []
   :ns-blacklist   []

   :middleware     []

   :timestamp-opts {:pattern "yyyy-MM-dd'T'HH:mm:ss.SSSX"}

   :output-fn      logger/default-output-fn

   :appenders      {
                    :console (merge (basic/println-appender) {:async? true})
                    :logfile (merge (basic/spit-appender) {:fname "/tmp/logging.log"})
                    :bla     (taoensso.timbre.appenders.carmine/carmine-appender)
                    }

   })

(logger/set-config! (merge default-config {:level :trace}))


;; eof
