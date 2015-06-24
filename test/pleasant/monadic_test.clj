(ns pleasant.monadic-test
  (import
    (pleasant.monadic.option Some))
  (:refer-clojure :exclude [empty?])
  (:require
    [clojure.test :refer :all]
    [pleasant.monadic.option :refer :all]
    [pleasant.monadic.try :refer :all]
    [clojure.algo.monads :refer :all]))

(with-monad option-m (def ^:private option-+ (m-lift 2 +)))

(deftest option-test
  (testing "Option"
    (is (empty? (option nil)))
    (is (defined? (option 42)))
    (is (= 42 @(option 42)))
    (is (= None None))
    (is (= None (option nil)))
    (is (= (option 42) (Some. 42)))
    (is (not= (option 42) (Some. 43)))
    (is (not= None (Some. 43)))
    (is (not= (Some. 43) None))
    (is (= (option "hi!")))
    (is (= (apply str (domonad sequence-m [x [1 nil] y [10 20]] (option-+ x y)))
           (apply str [(Some. 11) (Some. 21) None None])))
    ))

(deftest try-test
  (testing "Try"
    (is (failure? (failure nil)))
    (is (success? (success nil)))
    (is (failure? (try-fn #(/ 1 0))))
    (is (success? (try-fn #(/ 1 1))))
    (is (failure? (->try (/ 1 0))))
    (is (success? (->try (/ 1 1))))
    (is (failure? (->try (+ 1 1) (- 1 1) (let [x 0] (/ 1 x)))))
    (is (success? (->try (+ 1 1) (- 1 1) (let [x 1] (/ 1 x)))))
    (is (= (success 42) (success 42)))
    (is (= (type (->try (/ 1 0))) (type (->try (/ 1 0)))))
    (is (= (try-fn #(/ 1 1))) (try-fn #(/ 2 2)))
    ))

;; eof
