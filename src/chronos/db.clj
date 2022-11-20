(ns chronos.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]))

(def db-spec {:dbtype "postgres"
              :user "chronos"
              :password "secret"
              :host "localhost"
              :port "5432"
              :dbname "chronos"})


(def ^:dynamic *db* (jdbc/get-datasource db-spec))

(defn get-todos []
  (into [] (sql/query *db* ["select * from todos order by created_at desc"])))

(defn create-todo [todo]
  (sql/insert! *db* :todos todo))

(defn get-todo [id]
  (sql/get-by-id *db* :todos id))

(defn archive-todo [{id :todos/id}]
  (sql/update! *db* :todos {:archived_at (java.time.LocalDateTime/now)} {:id id}))

(defn complete-todo [{id :todos/id}]
  (sql/update! *db* :todos {:completed_at (java.time.LocalDateTime/now)} {:id id}))
