(defproject how-much "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [clj-http "1.0.0"]
                 [hiccup "1.0.5"]
                 [clj-time "0.8.0"]
                 [com.novemberain/monger "2.0.0"]
                 [ring/ring-jetty-adapter "1.2.1"]]
  :main ^:skip-aot how-much.handler
  :plugins [[lein-ring "0.8.11"]]
  :uberjar-name "how-much-standalone.jar"
  :ring {:handler how-much.handler/app}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}
             :uberjar {:aot :all}})
