(ns chronos.migrations
  (:require [ragtime.repl :as repl]
            [ragtime.protocols :refer [applied-migration-ids]]
            [clojure.set :refer [difference]]
            [ragtime.jdbc :as ragtime]))


(defn generate-migration-config [db-spec]
  {:datastore  (ragtime/sql-database db-spec {:migrations-table "migrations"})
   :migrations (ragtime/load-resources "migrations")})


(defn migrate [db-spec]
   (repl/migrate (generate-migration-config db-spec)))


(defn rollback [db-spec]
  (repl/rollback (generate-migration-config db-spec)))



(defn pending-migrations
  "Check if there are any pending migrations."
  [db-spec]
  (let [migrations (ragtime/load-resources "migrations")
        migration-ids (set (map #(:id %) migrations))
	    applied-migration-ids (set (applied-migration-ids (ragtime/sql-database db-spec {:migrations-table "migrations"})))]
    (difference migration-ids applied-migration-ids)))
