(defproject
  pleasant "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license
  {:name "Eclipse Public License"
   :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [
   [org.clojure/clojure "1.7.0-RC2"]
   [org.clojure/core.typed "0.3.0-alpha5"]
   [potemkin "0.3.13"]
   [com.lmax/disruptor "3.3.2"]
   [org.slf4j/slf4j-api "1.7.12"]
   [org.apache.logging.log4j/log4j-core "2.3"]
   [org.apache.logging.log4j/log4j-api "2.3"]
   [org.apache.logging.log4j/log4j-slf4j-impl "2.3"]
   [org.clojure/tools.logging "0.3.1"]]
  :main pleasant.core
  :target-path "target/"
  :profiles {:uberjar {:aot :all}})
