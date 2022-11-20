 (ns chronos.core-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [chronos.core :refer [app]]
            [next.jdbc :as jdbc]
            [chronos.db :refer [get-todos create-todo]]
            [ragtime.jdbc :as ragtime]
            [chronos.migrations :refer [migrate]]))


(def test-db-spec {:dbtype "postgres"
                   :host "localhost"
                   :port "5432"
                   :user "chronos"
                   :password "secret"
                   :dbname "chronos_test"})

(def test-db (jdbc/get-datasource test-db-spec))

(use-fixtures :once (fn [test] (migrate test-db-spec) (test)))

(use-fixtures
  :each
  (fn [test]
    (jdbc/with-transaction [txn test-db {:rollback-only true}]
      (binding [chronos.db/*db* txn] (test)))))

(defn request [app method resource]
  (app {:request-method method :uri resource}))


(deftest read-todo-200
  (testing "todo is returned if it exists"
	(create-todo {:id 1 :title "Hello World!"})
    (let [response (request app :get "/api/v1/todos/1")]
      (is (= 200 (:status response)))
      (is (= 1 (get-in response [:body :todos/id]))))))

(deftest read-entry-404
  (testing "404 is returned if todo does not exist"
    (let [response (request app :get "/api/v1/todos/1")]
      (is (= 404 (:status response)))
      (is (= "not found" (get-in response [:body :error]))))))
