(ns pleasant.aio-test
  (:refer-clojure :exclude [await future promise])
  (:require
    [clojure.test :refer :all]
    [pleasant.monad :refer :all]
    [pleasant.util :refer :all]
    [pleasant.aio :refer :all]))

(set! *warn-on-reflection* true)

(comment (deftest aio
           (testing "Aio server"
             (let [s (accept (socket-server 8001))]
               (on-complete s #(error "We have a" @%))
               (println "server started")
               (Thread/sleep 30000)
               (is true))))
         )

;; eof
