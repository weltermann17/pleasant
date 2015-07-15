(ns pleasant.core-test
  (:refer-clojure :exclude [await future promise])
  (:require
    [clojure.test :refer :all]
    [pleasant.util :refer :all]
    [pleasant.core :refer :all]))

(deftest core
  (testing "Core"
    (let [test {:uri "test-uri"}
          prod {:uri "prod-uri"}]
      ((run-app "hello") test)
      ((run-app 42) prod)
      (is true))
    (let [test-context (reify IContext (get-executor [_] (prn "getting the default-executor") *executor*))]
      ((run-app2 1 2 3) test-context))
    ))

;; eof
