(ns proto-site.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [redirect]]
            [clojure.string :refer [join split]]
            [proto-site.middlewares :refer [wrap-base]]
            [proto-site.db :as db]
            [proto-site.layout :as html]
            [proto-site.utils :as util :refer [defhandler]])
  (:import java.util.Date))

(def req-deb (atom []))

(def posts-per-page 5)

(def last-insert-rowid
  (keyword "last_insert_rowid()"))

(defn id->post&tags [id]
  (let [post-tags (db/id->post&tags db/db2 id)
        tags (map :tag post-tags)
        post (dissoc (first post-tags) :tag)]
    (assoc post :tags tags)))

(defn assoc-tags [post]
  (assoc post
         :tags
         (map :tag (db/post-id->tags db/db2 (:id post)))))

(defn posts-range [n offset]
  (->> (db/posts-range db/db2 n offset)
       (map assoc-tags)
       doall))

;; First page is no."1"
(defhandler posts-in-page [p :as-int flash]
  (let [p (or p 1)
        targets (posts-range posts-per-page (* (dec p) posts-per-page))
        n-posts (db/count-posts* db/db2)
        max-page (inc (quot n-posts posts-per-page))]
    (html/blog-list targets p max-page flash)))

(defhandler post-detail [id :as-int]
  (let [post (id->post&tags id)]
    (html/blog-post post)))

(defn split-tags [tags-str]
  (distinct (split tags-str #"\s")))

(defn register-tags! [post-id tags]
  (doseq [tag (split-tags tags)]
    (db/assoc-tag! db/db2 tag post-id)))

(defn save-new-post! [title content tags]
  (let [now-epoc (util/now->epoc)]
    (-> (db/save-post<! db/db2 title now-epoc content)
        last-insert-rowid ;; TODO: research the key and replace this ugly ugly..
        (register-tags! tags))
    (println "New post arrived!")
    (util/redirect-with-flash "/" {:message "Posted new article!"})))

(defn validate-new-post [title content tags auth]
  (cond-> []
    (empty? title) (conj "Title is empty")
    (empty? content) (conj "Content is empty")
    (not auth) (conj "Wrong password, maybe")))

(defn illegal-post [title content tags messages]
  (let [flash {:dtitle title
               :dcontent content
               :dtas tags
               :errors messages}]
    (util/redirect-with-flash "/new" flash)))

(defhandler handle-new-post [title content tags auth]
  (if-let [errors (seq (validate-new-post title content tags auth))]
    (illegal-post title content tags (join "." errors))
    (save-new-post! title content tags)))

(defn new-post-form [flash]
  (html/blog-new-article (anti-forgery-field)
                         flash))

(defhandler delete-post! [id :as-int auth]
  (if (and auth id)
    (do
      (db/delete-post! db/db2 id)
      (db/delete-post-tags! db/db2 id)
      (println "Deleted:" id)
      (util/redirect-with-flash "/" {:message (str "Deleted post:" id)}))
    (util/redirect-with-flash (str "/del?id=" id)
                               
                              {:error "Wrong password!!"})))

(defhandler edit-post-form [id :as-int flash]
  (let [the-post (id->post&tags id)] 
    (html/edit-post (anti-forgery-field) the-post (:error flash))))

(defhandler update-post! [id :as-int title content tags auth] ;; TODO: Need refactoring
  (if-let [errors (seq (validate-new-post title content tags auth))] 
    (util/redirect-with-flash (str "/edit?id=" id) ;; send back to edit-post-form
                              {:error (str "ERROR:" (join "/" errors))})
    (let [new-tags (split-tags tags)
          old-tags (map :tag (db/post-id->tags db/db2 id))
          {:keys [pop drop]} (util/update-diff old-tags new-tags)]
      (doseq [add-tag pop]
        (db/assoc-tag! db/db2 add-tag id))
      (when-let [ds (seq drop)]
        (db/dissoc-tags! db/db2 id ds))
      (db/edit-post! db/db2 title (util/now->epoc) content id)
      (util/redirect-with-flash "/"
                                {:message (str "Updated post! id:" id)}))))

(defn search-by [db-query qstr]
  (if qstr
    (let [posts (map assoc-tags (db-query db/db2 (str "%" qstr "%")))]
      (html/search-result posts qstr))
    "Give me some text you want to search."))


(defhandler search-by-date [year :as-int month :as-int date :as-int]
  (if (and date (not month))
    "TODO: ERROR"
    (let [[from til] (util/date-range year month date)
          posts (db/date-between db/db2 from til)]
      (html/search-result posts (str "Posted at " year 
                                     (when month "-" month) 
                                     (when date "-" date))))))

(defroutes app-routes
  (GET "/" [p :as {flash :flash}] (posts-in-page p flash))
  (GET "/article/:id" [id] (html/blog-post (db/find-post-by-id db/db2 id)))
  (GET "/new" {flash :flash} (new-post-form flash))
  (POST "/new" [title content tags auth] (handle-new-post title content tags auth))
  (GET "/del" [id :as {flash :flash}] (html/blog-delete (anti-forgery-field) 
                                             (merge flash
                                                    (db/find-post-by-id db/db2 id))))
  (DELETE "/del" [id auth] (delete-post! id auth))
  (GET "/edit" [id :as {flash :flash}] (edit-post-form id flash))
  (PUT "/edit" [id title content tags auth] (update-post! id title content tags auth))
  (GET "/search/date" [year month date] (search-by-date year month date))
  (GET "/search/text" [q] (search-by db/search-text q))
  (GET "/search/tag" [q] (search-by db/search-by-tag q))
  (ANY "/debug/echo" req (pr-str (swap! req-deb conj req)))
  (route/not-found "Not Found")
  (route/resources "/"))

(def app
  (wrap-base app-routes))

