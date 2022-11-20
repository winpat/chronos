(ns chronos.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [ring.util.response :refer [response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-response]]
            [chronos.db :refer [get-todo]]
            [chronos.ui :refer [index]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]))


(defonce ^:private api-server (atom nil))

(defn stop-server
  "Gracefully shutdown the server, waiting 100ms "
  []
  (when-not (nil? @api-server)
    (@api-server :timeout 100)
    (reset! api-server nil)))


(defn read-todo
  "Retrieve todo."
  [request]
  (let [todo-id (Integer/parseInt (get-in request [:route-params :id]))
        todo (get-todo todo-id)]
    (if (some? todo)
      (response todo)
      {:status 404 :body {:error "not found"}})))


(defroutes app
  (GET "/api/v1/todos/:id" [] read-todo)
  (GET "/" [] (response (index)))
  (route/resources "/")
  (route/not-found "Not found!"))


(defn -main [& args]
  (println "Starting server.")
  (let [server-cfg {:port 5000 :ip "0.0.0.0"}]
    (reset! api-server (server/run-server (wrap-json-response (wrap-reload app)) server-cfg))))
