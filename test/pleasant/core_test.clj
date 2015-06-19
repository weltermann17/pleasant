(ns pleasant.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.typed :refer [check-ns-info]]
            [pleasant.core :refer [abc]]))
(defn- t []
  (prn "type of abc :" (type abc))
  abc)

(deftest core
  (testing "Core functions"
    (is (instance? Number abc))
    (is (= 3.14 (t)))))

;; (check-ns-info 'pleasant.core)

;; eof
