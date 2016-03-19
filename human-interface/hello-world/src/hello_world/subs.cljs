(ns hello-world.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe register-sub dispatch dispatch-sync]]
            [taoensso.timbre :refer-macros [spy info]]))

(register-sub
 :get-state
 (fn [db [_ & keys]]
   (reaction (get-in @db keys))))

(register-sub
 :send-websocket
 (fn [db [_] [wheels-position]]
   (do
     (let [open? (get-in @db [:net :open])
           error? (get-in @db [:net :error])
           ws (get-in @db [:net :ws])
           ready-state (get-in @db [:net :ready-state])]
       (if (and open? (not error?) (some? ws) (= :open ready-state))
         (do
           (some->> (spy {:wheels wheels-position})
                    (clj->js)
                    (.stringify js/JSON)
                    (.send ws))
           (reaction true))
         (reaction false))))))
