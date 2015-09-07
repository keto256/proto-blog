(ns proto-site.middlewares
  (:require [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [proto-site.config :as conf]))


(defn log! [ex]
  (println ex))

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (println "Internal error! ")
        (log! t)
        {:status 500
         :headers {"Content-Type" "text/html; charset=utf-8"}
         :body "Something very bad happend.."}))))

(defn naive-auth [{pass :pass}]
  (let [admin-pass-hash (conf/config-m :admin-pass)]
    (= admin-pass-hash (hash pass))))

(defn wrap-auth [handler param-pred]
  (fn [req]
    (if (some-> req
                :params
                param-pred)
      (handler (-> (assoc-in req [:params :auth] true)
                   (assoc :_auth? true)))
      (handler req))))

(defn wrap-base [app-routes]
  (-> app-routes
      (wrap-auth naive-auth)
      (wrap-defaults site-defaults)))

