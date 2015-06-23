(ns pleasant.option-test
  (:refer-clojure :exclude [empty?])
  (:require
    [clojure.test :refer :all]
    [pleasant.monadic.option :refer :all]))

(deftest basics
  (testing "Basics"
    (is (empty? (option nil)))
    (is (= 42 @(option 42)))
    (is (defined? (option "hi!")))
    ))

;; eof
