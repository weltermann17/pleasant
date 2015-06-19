(ns pleasant.logging
  (:use potemkin)
  (:require
    [clojure.core.typed :refer [ann]]
    [clojure.tools.logging :as logger])
  )

(set! *warn-on-reflection* true)

(ann ^:no-check potemkin.namespaces/link-vars Object)

(import-vars

  [clojure.tools.logging
   trace debug info warn error fatal
   spy
   log-stream]

  )

;; Necessary to initialize log4j2 disruptor-based logging
(do
  (System/setProperty "Log4jContextSelector" "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector")
  (System/setProperty "AsyncLogger.WaitStrategy" "Block")
  (System/setProperty "AsyncLogger.RingBufferSize" (str (* 32 1024))))

(logger/spy :fatal "hello")

;; eof
