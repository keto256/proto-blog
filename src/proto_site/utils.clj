(ns proto-site.utils
  (:require [ring.util.response :refer [redirect]]
            [clojure.set :as cs])
  (:import [java.util Date Calendar]
           java.text.SimpleDateFormat))

(defmacro me1 [form]
  `(macroexpand-1 (quote ~form)))

(defn epoc->date [epoc]
  (Date. (long epoc)))

(defn now->epoc []
  (.getTime (Date.)))

(defn trim-str [s n]
  (if (> (count s) n)
    (str (subs s 0 n) "...")
    s))

(defn str->int
  ([s] (str->int s 0))
  ([s df]
   (try
     (Integer/valueOf s)
     (catch Exception e
       df))))

(defn redirect-with-flash [dest-ad flash]
  (assoc (redirect dest-ad)
         :flash
         flash))

(def a-day (* 1000 60 60 24))

(defn- a-day-after [epc]
  (+ epc a-day))

(defn- time-after!
  "Get Calendar obj, field to add, and amount. Return the time in epoc.
  !!BE CAREFUL!! this destructively change the cal obj."
  [cal cal-field amount]
  (.add cal cal-field amount)
  (.getTimeInMillis cal))

(defn- build-cal [year month date]
  (doto (Calendar/getInstance)
        (.set year month date)))

(defn date-range [year & [month day]]
  (let [start (build-cal year (dec (or month 1)) (dec (or day 1)))
        start-epoc (.getTimeInMillis start)]
    (cond
      day [start-epoc (a-day-after start-epoc)]
      month [start-epoc (time-after! start Calendar/MONTH 1)]
      :else [start-epoc (time-after! start Calendar/YEAR 1)])))

(def type-cast-fns
  {:as-int #(str->int % nil)
   :as-keyword keyword
   :as-str str
   :as-it-is identity})

(defn- let-coerce-type [sym-type-pairs]
  (vec
   (mapcat (fn [[s k]]
             [s `((type-cast-fns ~k identity) ~s)])
           sym-type-pairs)))


(defn- arg? [expr]
  (or (symbol? expr) (vector? expr)))

(defmacro defhandler [fname params & body]
  (let [pairs (->> params
                   (partition 2 1)
                   (filter #(and (arg? (first %))
                                 (keyword? (second %)))))
        syms (filterv arg? params)]
    `(defn ~fname ~syms
       (let ~(let-coerce-type pairs) ;; (let [] (body)) is a valid expr.
         ~@body)))) ;; So this is ok. No need for empty checking pairs.


(def date-format
  (SimpleDateFormat. "yyyy/MM/dd"))

(defn date->str [date]
  (.format date-format date))

(defn update-diff [olds news]
  (let [olds (set olds)
        news (set news)
        remain (cs/intersection olds news)
        poped (cs/difference news remain)
        dropped (cs/difference olds remain)]
    {:pop poped
     :drop dropped}))
