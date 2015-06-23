(ns pleasant.core
  (:require
    [clojure.test :refer [run-tests]]
    [pleasant.executor-test]
    [pleasant.future-test])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn check-java-version [minimum-versions]
  (let [v (System/getProperty "java.version")]
    (when (not-any? #(.startsWith v %) minimum-versions)
      (throw (RuntimeException. (str "Invalid Java version. Version found : " v))))))

(defn -main
  [& _]
  (check-java-version ["1.8" "1.9"])
  (run-tests 'pleasant.executor-test)
  (run-tests 'pleasant.future-test)
  (prn "Good bye."))

(comment (-main) (write-to-file))

;; eof
