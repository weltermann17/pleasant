(ns pleasant.monadic-test
  (import
    (pleasant.monadic.option Some))
  (:refer-clojure :exclude [empty?])
  (:require
    [clojure.test :refer :all]
    [midje.sweet :refer [fact]]
    [clojure.algo.monads :refer :all]
    [pleasant.monadic.option :refer :all]
    [pleasant.monadic.either :refer :all]
    [pleasant.monadic.try :refer :all]))

(with-monad maybe-m (def ^:private maybe-+ (m-lift 2 +)))

(with-monad option-m (def ^:private option-+ (m-lift 2 +)))

; (def ^:private sequence-maybe-m (maybe-t sequence-m))

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
    (is (= (domonad (maybe-t sequence-m) [a [nil 1 nil] b [10 20]] (+ a b))
           [nil 11 21 nil]))
    (is (= (domonad sequence-m [a [nil 1 nil] b [10 20]] (maybe-+ a b))
           [nil nil 11 21 nil nil]))
    (is (= (apply str (domonad sequence-m [x (map option [1 nil]) y (map option [10 20])] (option-+ x y)))
           (apply str [(Some. 11) (Some. 21) None None])))
    (is (= (Some. 5) (with-monad option-m (m-fmap (partial + 3) (option 2)))))
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
    (is (= (success nil) (success nil)))
    (is (= (success nil) (success (if false ()))))
    (is (= (->try 42) (success 42)))
    (is (failure? (->try (throw (Exception.)))))
    (is (= (type (->try (/ 1 0))) (type (->try (/ 1 0)))))
    (is (= (try-fn #(/ 1 1))) (try-fn #(/ 2 2)))
    ;(is (= (map success? (domonad sequence-try-m [a [nil 1 nil] b [10 20]] (+ a b)))
    ;       [nil nil 11 21 nil nil]))
    ))

(deftest test-0400
  (let [em1 (left 60)]
    (fact "(left) makes a Left value."
          @em1 => 60
          em1 => left?
          em1 =not=> right?)))

;; eof
