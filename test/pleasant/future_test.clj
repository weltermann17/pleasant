(ns pleasant.future-test
  (:import [java.util.concurrent TimeoutException])
  (:refer-clojure :exclude [await future promise])
  (:require
    [clojure.test :refer :all]
    [clojure.algo.monads :refer :all]
    [pleasant.util :refer :all]
    [pleasant.monad :refer :all]))

(deftest basics
  (testing "Basics"
    (is (= 42 (let [f (blocking-future (trace "inside a future 1") (Thread/sleep 0) 42)]
                (Thread/sleep 100) @@f)))
    (is (= 43 (let [f (future (trace "inside a future 2") (Thread/sleep 0) 43)]
                @@(await f))))
    (is (success? (let [f (future (trace "inside a future 3") (Thread/sleep 0) 44)]
                    @(await f 1000))))
    (is (failure? (let [f (future (trace "inside a future 4") (Thread/sleep 0) (/ 1 0))]
                    @(await f 1000))))
    (is (failure? (let [f (future (trace "inside a future 5") (Thread/sleep 0) (throw (Exception. "oops")))]
                    @(await f 1000))))
    (is (success? (let [f (future (trace "inside a future 6") (Thread/sleep 0) (+ 45 0))]
                    (on-complete f #(info "complete 6 :" %))
                    (on-success f #(info "the success is 1:" %))
                    (on-success f #(info "the success is 2:" %))
                    (on-success f #(info "the success is 3:" %))
                    (on-failure f #(info "the failure is :" %))
                    @(await f 1000))))
    (is (failure? (let [f (future (trace "inside a future 7") (Thread/sleep 0) (/ 45 0))]
                    (on-complete f #(info "complete 7 :" (type @%)))
                    (on-success f #(info "the success is :" %))
                    (on-failure f #(info "the failure is :" (type %)))
                    (on-failure f #(debug "the verbose failure is :" %))
                    @(await f 1000))))
    (is (instance? TimeoutException (let [f (->future (promise))] @@(await f 0))))
    (is (nil? (Thread/sleep 0)))
    (is (nil? (trace *executor*)))
    ))

(deftest future-m-test
  (testing "Future monad"
    (is (= 1
           (let [f (domonad
                     future-m
                     [x (future (trace "inside a future 8") (Thread/sleep 0) 42)
                      y (future (trace "inside a future 9") (Thread/sleep 0) (+ x 14))
                      a (immediate-future 1)
                      z (future (trace "inside a future 10" y) (Thread/sleep 0) (/ (* a y) 7))]
                     (/ z 8))]
             @@(await f))))
    (is (failure?
          (let [f (domonad
                    future-m
                    [x (future (trace "inside a future 11") (Thread/sleep 0) 42)
                     y (future (trace "inside a future 12") (Thread/sleep 0) (+ x 14))
                     z (future (trace "inside a future 13" y) (Thread/sleep 0) (/ y 1))]
                    (/ z 0))]                               ;; oops
            @(await f))))
    (is (instance? NullPointerException
                   (let [f (domonad
                             future-m
                             [x (future (trace "inside a future 11") (Thread/sleep 0) 42)
                              y (future (trace "inside a future 12") (Thread/sleep 0) (+ x nil)) ;; oops
                              z (future (trace "inside a future 13" y) (Thread/sleep 0) (/ y 1))]
                             (/ z 8))]
                     @@(await f))))
    (with-monad
      future-m
      (is (= 31
             (let [f (->> (map future-fn (repeat 3 (fn [] (Thread/sleep 0) 10)))
                          (m-reduce + 1))]
               (do (m-fmap #(is (= 31 %)) f) @@(await f)))))
      (let [f-+ (m-lift 2 +)
            f-inc (m-lift 1 inc)
            f-dec (m-lift 1 dec)
            f1 (for [a (future (trace "inside 14") (Thread/sleep 0) 1)
                     b (f-+ a (future (trace "inside 15") (Thread/sleep 0) 2))
                     c (f-+ b (future (trace "inside 16") (Thread/sleep 0) 3))]
                 (f-inc c))
            r1 (await (first f1))
            f2 (for [a (future (trace "inside 17") (Thread/sleep 0) 1)
                     b (f-+ a (future (trace "inside 18") (Thread/sleep 0) "2"))
                     c (f-+ b (future (trace "inside 19") (Thread/sleep 0) 3))]
                 (f-inc c))
            r2 (await (first f2))]
        (is (success? @r1))
        (is (= 7 @@r1))
        (is (failure? @r2))
        (is (instance? ClassCastException @@r2))
        (let [m (map f-inc)
              n (map f-dec)
              t (take 1)
              xf (comp n t m)
              a (future (trace "inside 20") (Thread/sleep 0) 1)
              r (first (into [] xf a))]
          (is (success? @r))
          (is (= 1 @@r))))
      (is (nil? (trace *executor*)))
      )))

;; eof
