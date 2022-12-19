(ns chronos.frontend.app
  (:require [reagent.core :as reagent]
            ["react-dom/client" :refer [createRoot]]
            [goog.dom :as gdom]
            [re-frame.core :as rf]))


(rf/reg-event-db
 :initialize
 (fn [ _ _ ]
   {:todos [{:title "First todos!"}
            {:title "Second todos!"}]}))


(defn dispatch-todo-added-event
  [title]
    (rf/dispatch [:todo-added title]))


(rf/reg-event-db
 :todo-added
 (fn [db [_ title]]
   (let [updated-todos (conj (:todos db) {:title title})]
	   (assoc db :todos updated-todos))))


(rf/reg-sub
 :todos
 (fn [db _]
   (:todos db)))


(defn input-bar-component
  []
  [:input {:type "text"
           :placeholder "Type here!"
           :on-key-press (fn [ev]
                           (when (= (.-key ev) "Enter")
                              (dispatch-todo-added-event (-> ev .-target .-value))))}])


(defn todo-list-component
  []
  [:div [input-bar-component]
	  (let [todos @(rf/subscribe [:todos])]
    [:ul
	 (for [todo todos]
	   [:li (:title todo)])])])


(defn ui
  []
  [:div
   [:h1 "Hello World?"]
   [todo-list-component]])


(defonce root (createRoot (gdom/getElement "chronos-app")))

(defn init
  []
  (rf/dispatch-sync [:initialize])
  (.render root (reagent/as-element [ui])))

(defn ^:dev/after-load re-render
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code.
  ;; This function is called implicitly by its annotation.
  (init))
