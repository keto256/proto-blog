(defproject proto-site "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [hiccup "1.0.5"]
                 [yesql "0.4.2"]
                 [org.xerial/sqlite-jdbc "3.8.11"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler proto-site.handler/app}
  :resource-paths ["resources"]
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]
         :resource-paths ["resources"]}})
