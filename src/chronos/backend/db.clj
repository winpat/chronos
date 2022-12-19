(ns chronos.backend.db
  (:require [next.jdbc :refer [get-datasource]]
            [next.jdbc.sql :as db]
            [honey.sql :as sql]))

(def db-spec {:dbtype "postgres"
              :user "chronos"
              :password "secret"
              :host "localhost"
              :port "5432"
              :dbname "chronos"})


(def ^:dynamic *db* (get-datasource db-spec))

(defn get-todos []
  (into [] (db/query *db* ["select * from todos order by created_at desc"])))

(defn create-todo [todo]
  (db/insert! *db* :todos todo))

(defn get-todo [id]
  (db/get-by-id *db* :todos id))

(defn update-todo [{:keys [id] :as todo}]
  (first (db/query *db*
                   (sql/format {:update [:todos]
                                :set todo
                                :where [:= :id id]
                                :returning [:*]}))))

(defn archive-todo [{id :todos/id}]
  (db/update! *db* :todos {:archived_at (java.time.LocalDateTime/now)} {:id id}))

(defn complete-todo [{id :todos/id}]
  (db/update! *db* :todos {:completed_at (java.time.LocalDateTime/now)} {:id id}))
