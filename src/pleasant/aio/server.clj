(ns pleasant.aio.server
  (:import
    [java.nio.channels
     AsynchronousChannelGroup
     AsynchronousServerSocketChannel
     AsynchronousSocketChannel
     CompletionHandler]
    [java.net
     InetSocketAddress
     StandardSocketOptions])
  (:refer-clojure :exclude [await future promise])
  (:require
    [pleasant.logging :as log]
    [pleasant.monadic.try :refer [failure success]]
    [pleasant.concurrent.executor :refer [*executor*]]
    [pleasant.concurrent.future :refer :all]))

(def default-channel-group
  (AsynchronousChannelGroup/withThreadPool *executor*))

(def ^:dynamic *channel-group* default-channel-group)

(defn socket-server
  ([^Long port]
   (socket-server (InetSocketAddress. port) 0))
  ([^InetSocketAddress address ^Long backlog]
   (doto (AsynchronousServerSocketChannel/open *channel-group*)
     (.setOption StandardSocketOptions/SO_REUSEADDR true)
     (.setOption StandardSocketOptions/SO_RCVBUF (int (* 256 1024)))
     (.bind address backlog))))

(def accept-handler
  (reify CompletionHandler
    (^void failed [_ ^Throwable e p]
      (log/error "failed" e)
      (complete p (failure e)))
    (^void completed [_ socket p]
      (log/trace "completed" socket)
      (complete p (success socket)))))

(defn accept [^AsynchronousServerSocketChannel server]
  (let [p (promise)]
    (.accept server p accept-handler)
    (->future p)))

(let [s (accept (socket-server 8000))]
  (on-complete s #(log/info "We have a" @%)))

;; eof
