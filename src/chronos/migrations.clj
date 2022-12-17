(ns chronos.migrations
  (:require [ragtime.repl :as repl]
            [ragtime.jdbc :as ragtime]))


(defn generate-migration-config [db-spec]
  {:datastore  (ragtime/sql-database db-spec {:migrations-table "migrations"})
   :migrations (ragtime/load-resources "migrations")})


(defn migrate [db-spec]
   (repl/migrate (generate-migration-config db-spec)))


(defn rollback [db-spec]
  (repl/rollback (generate-migration-config db-spec)))
