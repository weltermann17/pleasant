(in-ns 'pleasant.util)

(require
  '[clojure.string :refer [split]]
  '[clojure.core.strint :refer [<<]])

(defn >=-java-version [a b]
  "Funny how difficult such things are even in this language."
  (let
    [split-v (fn [v] (let [s (split v (re-pattern "\\."))] (flatten (map #(split % (re-pattern "_")) s))))
     a' (split-v a)
     b' (split-v b)
     m (min (count a') (count b'))
     a'' (vec (take m a'))
     b'' (vec (take m b'))]
    (>= (compare a'' b'') 0)))

(defn require-minimum-java-version [minimum-v]
  (let [v (System/getProperty "java.version")]
    (when-not (>=-java-version v minimum-v)
      (let [msg (str (<< "Invalid Java version. Version found : ~{v}, minimum version required : ~{minimum-v}"))]
        (fatal msg)
        (throw (VirtualMachineError. msg))))))

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
            (fatal "Fatal exception :" e)
            (fatal "Terminate JVM runtime now.")
            (future
              (Thread/sleep 2000)
              (println "JVM runtime halted.")
              (.halt (Runtime/getRuntime) 1))
            (.exit (Runtime/getRuntime) 1))))))

(def ^:dynamic *fatal-exception-handler* default-fatal-exception-handler)

(comment check-java-version *fatal-exception-handler*)

;; eof
