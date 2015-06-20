(ns pleasant.core-test
  (:require [clojure.test :refer :all]
            [pleasant.core :refer :all]))

(deftest core
  (testing "Core functions"
    (is (= "3.14" (f 3.14)))
    (is (instance? Number abc))
    (is (= 3.14 abc))))

;; eof
