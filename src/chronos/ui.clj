(ns chronos.ui
  (:require [hiccup.core :refer [html]]))


(defn header []
  [:div {:class "header"} "header"])


(defn content []
  [:div
   [:ul
    (for [x [1 2 3]]
      [:li x])]])


(defn footer []
  [:div {:class "footer"} "footer"])

(defn head []
  [:head
   [:link {:rel "stylesheet" :href "css/core.css"}]])

(defn body []
  [:body
   (header)
   (content)
   (footer)])

(defn index []
  (html [:html (head) (body)]))
