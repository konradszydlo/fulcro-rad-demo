(ns development
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.repl :refer [doc source]]
    [clojure.tools.namespace.repl :as tools-ns :refer [disable-reload! refresh clear set-refresh-dirs]]
    [com.example.components.datomic :refer [datomic-connections]]
    [com.example.components.middleware]
    [com.example.components.server]
    [com.example.model.seed :as seed]
    [com.example.model.account :as account]
    [com.example.model.address :as address]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [com.fulcrologic.rad.database-adapters.datomic :as datomic]
    [com.fulcrologic.rad.resolvers :as res]
    [mount.core :as mount]
    [taoensso.timbre :as log]
    [datomic.api :as d]
    [com.fulcrologic.rad.attributes :as attr]))

(set-refresh-dirs "src/main" "src/datomic" "src/dev" "src/shared" "../fulcro-rad-datomic/src/main" "../fulcro-rad/src/main")

(defn seed! []
  (let [connection (:main datomic-connections)]
    (when connection
      (log/info "SEEDING data.")
      @(d/transact connection [(seed/new-address (new-uuid 1) "111 Main St.")
                               (seed/new-account (new-uuid 100) "Tony" "tony@example.com" "letmein"
                                 :account/addresses ["111 Main St."])
                               (seed/new-account (new-uuid 101) "Sam" "sam@example.com" "letmein")
                               (seed/new-account (new-uuid 102) "Sally" "sally@example.com" "letmein")
                               (seed/new-account (new-uuid 103) "Barbara" "barb@example.com" "letmein")]))))

(defn start []
  (mount/start-with-args {:config "config/dev.edn"})
  (seed!)
  :ok)

(defn stop
  "Stop the server."
  []
  (mount/stop))

(def go start)

(defn restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (tools-ns/refresh :after 'development/start))

(def reset #'restart)

(comment
  (d/delete-database "datomic:sql://example?jdbc:postgresql://localhost:5432/datomic?user=datomic&password=datomic")
  (d/q
    '[:find (pull ?e [*])
      :where
      [?e :account/id]]
    (d/db (:main datomic-connections))))
