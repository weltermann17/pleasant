(ns pleasant.future-test
  (:refer-clojure :exclude [await future promise])
  (:require [clojure.test :refer :all]
            [pleasant.logging :as log]
            [pleasant.monadic.try :refer :all]
            [pleasant.concurrent.executor :refer :all]
            [pleasant.concurrent.future :refer :all]))

(deftest basics
  (testing "Basics"
    (is (= 42 (let [f (blocking-future (log/trace "inside a future 1") (Thread/sleep 100) 42)]
                (Thread/sleep 200) @@f)))
    (is (= 43 (let [f (future (log/trace "inside a future 2") (Thread/sleep 100) 43)]
                @@(await f 1000))))
    (is (success? @(let [f (future (log/trace "inside a future 3") (Thread/sleep 100) 44)]
                     (await f 1000))))
    (is (failure? @(let [f (future (log/trace "inside a future 4") (Thread/sleep 100) (/ 1 0))]
                     (await f 1000))))
    (is (failure? @(let [f (future (log/trace "inside a future 5") (Thread/sleep 100) (throw (OutOfMemoryError. "oops")))]
                     (await f 1000))))
    ))

;; eof
