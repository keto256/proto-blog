(ns proto-site.db
  (:require [yesql.core :refer [defquery defqueries]]
            [proto-site.config :as conf]))

(def sqlite-spec
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname (conf/config-m :db-location)})

(def db2
  (assoc sqlite-spec :subname "resources/db/db_v2.sqlite"))

(defqueries "sql/init-db.sql")

(defqueries "sql/queries_post.sql")

(defqueries "sql/queries_tag.sql")

(defn count-posts* [db]
  (-> (count-posts db)
      ffirst
      second ))

(defn find-post-by-id [db id]
  (let [id (if (number? id) 
             id 
             (Integer/valueOf id))]
    (->> id
         (id->post db)
         first)))
