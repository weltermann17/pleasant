(ns pleasant.monadic-test
  (:refer-clojure :exclude [await future promise])
  (:require
    [clojure.test :refer :all]
    [clojure.algo.monads :refer :all]
    [midje.sweet :refer [fact]]
    [pleasant.monadic :refer :all]))

(with-monad maybe-m (def ^:private maybe-+ (m-lift 2 +)))

(with-monad option-m (def ^:private option-+ (m-lift 2 +)))

(with-monad
  maybe-m
  (deftest maybe-test
    (testing "Maybe"
      (is (= (domonad (maybe-t sequence-m) [a [nil 1 nil]] (+ a 0))
             [nil 1 nil]))
      (is (= (domonad (maybe-t sequence-m) [a [nil 1 nil] b [10 20]] (+ a b))
             [nil 11 21 nil]))
      (is (= (m-plus 1 2 3) 1))
      (is (= (m-plus nil 2 3) 2))
      (is (= (domonad sequence-m [a [nil 1 nil] b [10 20]] (maybe-+ a b))
             [nil nil 11 21 nil nil]))
      (is (= (m-plus nil nil 3) 3))
      )))

(deftest option-test
  (with-monad
    option-m
    (testing "Option"
      (is (undefined? (option nil)))
      (is (defined? (option 42)))
      (is (= 42 @(option 42)))
      (is (= (option nil) (option nil)))
      (is (= (option 42) (->Some 42)))
      (is (not= (option 42) (option 43)))
      (is (= (option "hi!")))
      (is (= @(m-plus (option 1) (option 2))) 1)
      (is (= @(m-plus (option 1) None (option 3))) 1)
      (is (= @(m-plus (option nil) (option 2))) 2)
      (is (undefined? (m-plus None None)))
      (is (undefined? (m-plus)))
      (is (= (option 2) (first (map (m-lift 1 inc) (option 1)))))
      (is (undefined? (first (map (m-lift 1 inc) (option nil)))))
      (is (= [(option 4)] (vec (map (m-lift 1 (partial + 3)) (option 1)))))
      (is (= [(option 4)] (vec (map (m-lift 1 #(+ 3 %)) (option 1)))))
      (is (= (option 2) (first (for [x (option 1)] ((m-lift 1 inc) x)))))
      (let [+ (m-lift 2 clojure.core/+)
            inc (m-lift 1 clojure.core/inc)]
        (is (= (option 7) (first (for [x (option 1) y (+ x (option 2)) z (+ y (option 3))] (inc z)))))
        (is (undefined? (first (for [x (option 1) y (+ x (option 2)) z (+ y None)] (inc z)))))
        )
      (is (= (m-plus (option nil) (option 2) (option 3)) (->Some 2)))
      (is (= (apply str (domonad sequence-m [x (map option [1 nil]) y (map option [10 20])] (option-+ x y)))
             (apply str [(->Some 11) (->Some 21) None None])))
      ))
  (with-monad
    option-m
    (testing "Option 2"
      (is (defined? (m-fmap (partial + 3) (option 2))))
      (is (undefined? (m-fmap (partial + 3) (option nil))))
      )))

(with-monad
  try-m
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
      (is (= (domonad sequence-m [a [1 nil] b [10 20]] (success? (->try (+ a b))))
             [true true false false]))
      (is (= (m-plus (->try (/ 1 0)) (->try (/ 2 1)))
             (success 2)))
      (is (failure? (m-plus (->try (/ 1 0)) (->try (/ 2 0)))))
      (is (= (success 2) (first (map (m-lift 1 inc) (success 1)))))
      (is (failure? (first (map (m-lift 1 inc) (failure nil)))))
      (let [a (first (map (m-lift 1 #(/ % 0)) (success 1)))] (is (failure? a)))
      )))

(deftest combine-option-try
  (let [o-+ (with-monad option-m (m-lift 2 +))
        t-+ (with-monad try-m (m-lift 2 +))
        t-inc (with-monad try-m (m-lift 1 inc))]
    (testing "Combine option with try"
      (let [r (first (for
                       [a (option 1)
                        b (o-+ a (option 2))
                        c (t-+ (->try @b) (success 3))]
                       (t-inc c)))] (is (= r (success 7))))
      (let [r (first (for
                       [a (option 1)
                        b (o-+ a None)
                        c (t-+ (->try @b) (success 3))]
                       (t-inc c)))] (is (failure? r)))
      )))

;; eof
