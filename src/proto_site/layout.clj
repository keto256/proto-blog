(ns proto-site.layout
  (:require [hiccup.core :refer :all]
            [hiccup.element :refer [link-to]]
            [hiccup.form :refer :all]
            [hiccup.page :refer :all]
            [proto-site.utils :as util]
            [clojure.string :refer [split join]]))

(defn- base-head [title]
  [:head
   [:title title]])

(def page-header
  [:header
   [:h1 "My blog"]
   [:div "here will be menu or so.."
    (link-to "/new" "Post an article")]])

(def search-form
  (form-to [:get "/search/title"]
           (label "q" "Search")
           (text-field "q" "Search title")))

(def page-footer
  [:footer
   [:div.searcher search-form]
   [:div "Footer of a page here."]])

(defn- tag-links [tags]
  (->> tags
       (map (fn [tag]
              [:span.tagLink (link-to (str "/search/tag?q=" tag) tag)]))
       (interpose [:span.separater "|"])))

(defn- list-posts [posts]
  (map (fn [{:keys [id title date content tags]}]
         [:li.brief
          [:div
           [:h3 (link-to (str "/article/" id) title)]
           (when date [:date (util/date->str (util/epoc->date date))])
           (when content [:p (util/trim-str content 15)])
           (when tags [:div "Tags: " (tag-links tags)])]])
       posts))

(defn blog-list [posts cur-page max-page {:keys [message] :as flash}]
  (html5
   (base-head "All posts")
   [:body
    page-header
    (when message [:div message])
    [:div
     [:ul (list-posts posts)]]
    [:div.pager
     [:span.pgback (link-to (str "/?p=" (max 1 (dec cur-page))) "<")]
     (for [p (range 1 (inc max-page))]
       [:span.page  (link-to (str "/?p=" p) (str p))])
     [:span.pgforward (link-to (str "/?p=" (min max-page (inc cur-page))) ">")]]
    page-footer]))

(defn search-result [posts what]
  (let [what-result (str "Search result: " what)]
    (html5
     (base-head what-result)
     [:body
      page-header
      [:h2 what-result]
      [:div.result
       (list-posts posts)]
      [:div (link-to "/") "Back to Home"]
      page-footer])))

(defn blog-post [{:keys [id title date content tags]}]
  (html5
   (base-head title)
   [:body
    page-header
    [:article
     [:h2 title]
     (when date [:date (util/date->str (util/epoc->date date))])
     [:div.content content]
     (when tags [:div "Tags:" (tag-links tags)])]
    [:div.admin
     [:ul.menu
      [:li (link-to (str "/del?id=" id) "Delete")]
      [:li (link-to (str "/edit?id=" id) "Edit")]]]
    page-footer]))

(defn edit-post [af-html {:keys [id title content tags] :as post} error]
  (html5
   (base-head (str "Edit post " title))
   [:body
    page-header
    [:h2 "Edit"]
    (when error [:div.warning error])
    [:p "Enter new text"]
    (form-to [:put "/edit"]
             af-html
             (hidden-field "id" (str id))
             [:div (label "title" "Title:")
              (text-field "title" title)]
             [:div (label "content" "Content:")
              (text-area "content" content)]
             [:div (label "tags" "Tags,separated by space:")
              (text-field "tags" (or (join " " tags) ""))]
             [:div (label "pass" "Password:")
              (password-field "pass")]
             (submit-button "Update")
             )
    page-footer]))

(defn blog-delete [af-html {:keys [id title date content error]}]
  (html5
   (base-head (str "Delete" title "?"))
   [:body
    page-header
    [:h2 "Delete"]
    [:p "Will you really DELETE this post?"]
    (when error [:div.warning error])
    [:h3 title]
    [:p content]
    (form-to [:delete "/del"]
             af-html
             (label "pass" "Password: ")
             (password-field "pass")
             (submit-button "Delete this post")
             (hidden-field "id" (str id)))]))

(defn blog-new-article [af-html {:keys [dtitle dcontent dtags errors] :as flash}]
  (html5
   (base-head "Post new article")
   [:body
    page-header
    [:h2 "Post new article"]
    [:p "Fill in the forms below."]
    (when errors (for [e (split errors #"\.")] [:div.error e]))
    (form-to [:post "../new"]
             af-html
             [:div 
              (label "title" "Title") 
              (text-field "title" (or dtitle "Title here"))]
             [:div 
              (label "content" "Content text") 
              (text-area "content" (or dcontent "Content here"))]
             [:div
              (label "tags" "Tags,separated by space")
              (text-field "tags" (or dtags "tag"))]
             [:div (label "pass" "Password:") (password-field "pass")]
             (submit-button "Send!"))
    page-footer]))
