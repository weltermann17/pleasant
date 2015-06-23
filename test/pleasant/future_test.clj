(ns pleasant.future-test
  (:refer-clojure :exclude [await future promise])
  (:require
    [clojure.test :refer :all]
    [pleasant.util.logging :as log]
    [pleasant.monadic.try :refer :all]
    [pleasant.concurrent.executor :refer :all]
    [pleasant.concurrent.future :refer :all]))

(deftest basics
  (testing "Basics"
    (is (= 42 (let [f (blocking-future (log/trace "inside a future 1") (Thread/sleep 10) 42)]
                (Thread/sleep 100) @@f)))
    (is (= 43 (let [f (future (log/trace "inside a future 2") (Thread/sleep 10) 43)]
                @@(await f 1000))))
    (is (success? @(let [f (future (log/trace "inside a future 3") (Thread/sleep 10) 44)]
                     (await f 1000))))
    (is (failure? @(let [f (future (log/trace "inside a future 4") (Thread/sleep 10) (/ 1 0))]
                     (await f 1000))))
    (is (failure? @(let [f (future (log/trace "inside a future 5") (Thread/sleep 10) (throw (Exception. "oops")))]
                     (await f 1000))))
    (is (success? @(let [f (future (log/trace "inside a future 6") (Thread/sleep 10) (+ 45 0))]
                     (on-success f #(log/info "the success is:" %))
                     (on-failure f #(log/info "the failure is:" %))
                     (await f 1000))))
    (is (failure? @(let [f (future (log/trace "inside a future 6") (Thread/sleep 10) (/ 45 0))]
                     (on-success f #(log/info "the success is:" %))
                     (on-failure f #(log/info "the failure is:" (type %)))
                     (await f 1000))))
    ; (is (nil? (Thread/sleep 1000)))
    ))

;; eof
