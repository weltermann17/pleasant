(ns pleasant.util.fatal
  (:require
    [pleasant.util.logging :as log]))

(defn check-java-version [minimum-versions]
  (let [v (System/getProperty "java.version")]
    (when (not-any? #(.startsWith v %) minimum-versions)
      (throw (RuntimeException. (str "Invalid Java version. Version found : " v))))))

(defn fatal? [^Throwable e]
  (some #(instance? % e) [InterruptedException
                          LinkageError
                          ThreadDeath
                          VirtualMachineError]))

(def default-fatal-exception-handler
  (fn [^Throwable e]
    (when (fatal? e)
      (future
        (do (Thread/sleep 1000)
            (log/fatal "Fatal exception :" e)
            (log/fatal "Terminate JVM runtime now.")
            (future
              (Thread/sleep 2000)
              (println "JVM runtime halted.")
              (.halt (Runtime/getRuntime) 1))
            (.exit (Runtime/getRuntime) 1))))))

(def ^:dynamic *fatal-exception-handler* default-fatal-exception-handler)

(comment check-java-version *fatal-exception-handler*)

;; eof
