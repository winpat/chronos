(ns chronos.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [clojure.spec.alpha :as s]
            [ring.util.response :refer [response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [chronos.db :as db]
            [chronos.ui :refer [index]]
            [compojure.core :refer [defroutes GET POST PUT]]
            [compojure.route :as route]))

(defonce ^:private api-server (atom nil))

(defn stop-server
  "Gracefully shutdown the server, waiting 100ms "
  []
  (when-not (nil? @api-server)
    (@api-server :timeout 100)
    (reset! api-server nil)))


(defn transform-keys
  [t coll]
  (clojure.walk/postwalk (fn [x] (if (map? x) (update-keys x t) x)) coll))


(defn wrap-remove-namespace-keywords [handler & _]
  (fn [req]
    (let [resp (handler req)]
      (cond-> resp
        (comp map? :body) (update :body
                                  (partial transform-keys
                                           (comp keyword name)))))))


(s/def :todo/id int?)
(s/def :todo/title string?)
(s/def :todo/description (s/nilable string?))
(s/def :todo/status #(re-matches #"(todo|done)" %))
(s/def :todo/created-at (s/nilable inst?))
(s/def :todo/completed-at (s/nilable inst?))
(s/def :todo/archived-at (s/nilable inst?))

(s/def :todo/todo (s/keys :req-un [:todo/title]
			              :opt-un [:todo/id :todo/description :todo/status :todo/created-at :todo/completed-at :todo/archived-at]))

(defn read-todo
  "Retrieve todo."
  [request]
  (let [todo-id (Integer/parseInt (get-in request [:route-params :id]))
        todo (db/get-todo todo-id)]
     (if (some? todo)
      (response todo)
      {:status 404 :body {:error "not found"}})))


(defn update-todo
  "Update todo."
  [request]
  (let [todo-data (:body request)]
    (if-not (s/valid? :todo/todo todo-data)
      {:status 400}
      (response (db/update-todo todo-data)))))


(defn create-todo
  "Create todo."
  [request]
  (let [todo-data (:body request)]
    (if-not (s/valid? :todo/todo todo-data)
      {:status 400}
      (response (db/create-todo todo-data)))))


(defroutes app
  (GET "/api/v1/todos/:id" [] read-todo)
  (POST "/api/v1/todos/" []  create-todo)
  (PUT "/api/v1/todos/" []  update-todo)
  (GET "/" [] (response (index)))
  (route/resources "/")
  (route/not-found "Not found!"))


(defn -main [& args]
  (println "Starting server.")
  (let [server-cfg {:port 5000 :ip "0.0.0.0"}]
    (reset!
     api-server
     (server/run-server (-> app
                            (wrap-remove-namespace-keywords)
                            (wrap-json-body)
                            (wrap-json-response)
                            (wrap-reload))
                        server-cfg))))
