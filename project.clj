(defproject
  pleasant "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [
   ;; logging
   [org.slf4j/slf4j-api "1.7.12"]
   [org.apache.logging.log4j/log4j-core "2.3"]
   [org.apache.logging.log4j/log4j-api "2.3"]
   [org.apache.logging.log4j/log4j-slf4j-impl "2.3"]
   [com.lmax/disruptor "3.3.2"]
   [org.clojure/tools.logging "0.3.1"]

   ;; clojure
   [org.clojure/clojure "1.7.0"]
   [org.clojure/algo.monads "0.1.5"]
   [org.clojure/core.incubator "0.1.3"]
   [expectations "2.1.2"]
   [potemkin "0.3.13"]

   ;; not yet used
   ; [prismatic/schema "0.4.3"]
   ; [org.clojure/core.typed "0.3.0"] ;; not yet used
   ; [org.clojure/tools.trace "0.7.8"]
   ; [midje "1.7.0"]
   ]
  :main pleasant.core
  :target-path "target/"
  :profiles {:uberjar {:aot :all}})
