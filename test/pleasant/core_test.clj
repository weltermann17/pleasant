(ns pleasant.core-test
  (:require [clojure.test :refer :all]
            [pleasant.core :refer :all]))

(deftest basics
  (testing "Basics"
    (is (= "3.14" (f 3.14)))
    (is (instance? Number abc))
    (is (= 3.14 abc))))

;; eof
