(ns proto-site.core
  (:require [proto-site.handler :refer [app]]
            [ring.adapter.jetty :as jetty]))

(def server (atom nil))

(defn start-server [& [port]]
  (let [port (or port 3000)]
    (println "Start server at port: " port)
    (if-not @server
      (reset! server
              (jetty/run-jetty app {:port port
                                    :join? false}))
      (println "Server already running! @" port))
    nil))

(defn stop-server []
  (println "Stop server..")
  (when @server
    (.stop @server)
    (reset! server nil)))
