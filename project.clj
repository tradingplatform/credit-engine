(defproject credit-engine "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.4"]
                 [com.keminglabs/zmq-async "0.1.0"]]
  :main ^:skip-aot credit-engine.core
  :target-path "target/%s"
  ;:global-vars {*warn-on-reflection* true}
  :profiles {:uberjar {:aot :all}})
  
