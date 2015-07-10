(ns pleasant.util-test
  (:require
    [clojure.test :refer :all]
    [pleasant.util :refer :all]))

(deftest util
  (testing "Check java versions"
    (is (true? (>=-java-version "1.7.0" "1.7.0")))
    (is (true? (>=-java-version "1.7.3" "1.7.2")))
    (is (true? (>=-java-version "1.8.0" "1.7.9")))
    (is (true? (>=-java-version "1.8.0_45" "1.8.0_40")))
    (is (false? (>=-java-version "1.8.0_40" "1.8.0_45")))
    (is (false? (>=-java-version "1.8.0" "2.0")))
    (is (false? (>=-java-version "1.8.0_45" "2")))
    (is (false? (>=-java-version "1.8.0" "2.0.0.0.0")))
    (is (true? (>=-java-version "2.0.0.0.0" "1.8.0")))
    ))

;; eof
