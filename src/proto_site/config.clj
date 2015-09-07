(ns proto-site.config
  (:require [clojure.edn :as edn]))

(defn config 
  ([]
   (edn/read-string (slurp "resources/config.edn")))
  ([k]
   (get (config) k)))

(def config-m (memoize config))

(defn symbol-cat [& strs]
  (symbol (apply str strs)))

(defn key->defn-form [key]
  (let [sym (symbol-cat "change-" (name key))]
    `(defn ~sym [v#]
       (->> (assoc config ~key v#)
            pr-str
            (spit "resources/config.edn")))))

(defmacro defchanges [filepath]
  (let [edn-config (edn/read-string (slurp filepath))
        config-keys (keys edn-config)
        defn-exprs (map key->defn-form config-keys)]
    `(do
       ~@defn-exprs)))

(defn change-pass [pass]
  (let [hs (hash pass)]
    (->> (assoc (config-m) :admin-pass hs)
         pr-str
         (spit "resources/config.edn"))))

(defchanges "resources/config.edn")

