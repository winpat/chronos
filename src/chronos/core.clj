(ns chronos.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [ring.util.response :refer [response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [chronos.db :as db]
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


(defn read-todo
  "Retrieve todo."
  [request]
  (let [todo-id (Integer/parseInt (get-in request [:route-params :id]))
        todo (db/get-todo todo-id)]
    (if (some? todo)
      (response todo)
      {:status 404 :body {:error "not found"}})))


(defn create-todo
  "Create todo."
  [request]
  (let [todo (db/create-todo (:body request))]
    (response todo)))


(defroutes app
  (GET "/api/v1/todos/:id" [] read-todo)
  (POST "/api/v1/todos/" []  create-todo)
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
