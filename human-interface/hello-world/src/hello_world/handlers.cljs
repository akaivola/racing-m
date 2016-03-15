(ns hello-world.handlers
  (:require
    [re-frame.core :refer [register-handler after]]
    [schema.core :as s :include-macros true]
    [hello-world.wheels :as wheels]
    [hello-world.db :refer [app-db schema]]))

(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
    (if-let [problems (s/check a-schema db)]
      (throw (js/Error. (str "schema check failed: " problems)))))

(def validate-schema-mw
  (after (partial check-and-throw schema)))

(register-handler
  :initialize-db
  (fn [_ _]
    (assoc
     app-db
     :wheels wheels/initial-state
     :net {:endpoint "http://192.168.8.1:6000"})))

(register-handler
 :update-state
 (fn [db [_ f]]
   (or (f db)
       db)))

(register-handler
 :set-state
 (fn [db [_ path args]]
   (assert (or (vector? path)
               (seq? path)))
   (if (map? args)
     (update-in db path merge args)
     (assoc-in db path args))))
