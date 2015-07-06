(ns pleasant.future-test
  (:import java.util.concurrent.TimeoutException)
  (:refer-clojure :exclude [await future promise])
  (:require
    [clojure.test :refer :all]
    [clojure.algo.monads :refer :all]
    [pleasant.util.logging :as log]
    [pleasant.monadic.try :refer :all]
    [pleasant.concurrent.executor :refer :all]
    [pleasant.concurrent.future :refer :all]))

(deftest basics
  (testing "Basics"
    (is (= 42 (let [f (blocking-future (log/trace "inside a future 1") (Thread/sleep 0) 42)]
                (Thread/sleep 100) @@f)))
    (is (= 43 (let [f (future (log/trace "inside a future 2") (Thread/sleep 0) 43)]
                @@(await f))))
    (is (success? (let [f (future (log/trace "inside a future 3") (Thread/sleep 0) 44)]
                    @(await f 1000))))
    (is (failure? (let [f (future (log/trace "inside a future 4") (Thread/sleep 0) (/ 1 0))]
                    @(await f 1000))))
    (is (failure? (let [f (future (log/trace "inside a future 5") (Thread/sleep 0) (throw (Exception. "oops")))]
                    @(await f 1000))))
    (is (success? (let [f (future (log/trace "inside a future 6") (Thread/sleep 0) (+ 45 0))]
                    (on-complete f #(log/info "complete 6 :" %))
                    (on-success f #(log/info "the success is 1:" %))
                    (on-success f #(log/info "the success is 2:" %))
                    (on-success f #(log/info "the success is 3:" %))
                    (on-failure f #(log/info "the failure is :" %))
                    @(await f 1000))))
    (is (failure? (let [f (future (log/trace "inside a future 7") (Thread/sleep 0) (/ 45 0))]
                    (on-complete f #(log/info "complete 7 :" (type @%)))
                    (on-success f #(log/info "the success is :" %))
                    (on-failure f #(log/info "the failure is :" (type %)))
                    (on-failure f #(log/debug "the verbose failure is :" %))
                    @(await f 1000))))
    (is (instance? TimeoutException (let [f (->future (promise))] @@(await f 0))))
    (is (nil? (Thread/sleep 0)))
    (is (nil? (log/trace *executor*)))
    ))

(deftest future-m-test
  (testing "Future monad"
    (is (= 1
           (let [f (domonad
                     future-m
                     [x (future (log/trace "inside a future 8") (Thread/sleep 0) 42)
                      y (future (log/trace "inside a future 9") (Thread/sleep 0) (+ x 14))
                      z (future (log/trace "inside a future 10" y) (Thread/sleep 0) (/ y 7))]
                     (/ z 8))]
             @@(await f))))
    (is (failure?
          (let [f (domonad
                    future-m
                    [x (future (log/trace "inside a future 11") (Thread/sleep 0) 42)
                     y (future (log/trace "inside a future 12") (Thread/sleep 0) (+ x 14))
                     z (future (log/trace "inside a future 13" y) (Thread/sleep 0) (/ y 1))]
                    (/ z 0))]                               ;; oops
            @(await f))))
    (is (instance? NullPointerException
                   (let [f (domonad
                             future-m
                             [x (future (log/trace "inside a future 11") (Thread/sleep 0) 42)
                              y (future (log/trace "inside a future 12") (Thread/sleep 0) (+ x nil)) ;; oops
                              z (future (log/trace "inside a future 13" y) (Thread/sleep 0) (/ y 1))]
                             (/ z 8))]
                     @@(await f))))
    (with-monad
      future-m
      (is (= 31
             (let [f (->> (map future-fn (repeat 3 (fn [] (Thread/sleep 0) 10)))
                          (m-reduce + 1))]
               (do (m-fmap #(is (= 31 %)) f) @@(await f))))))
    (is (nil? (log/trace *executor*)))
    ))


;; eof
