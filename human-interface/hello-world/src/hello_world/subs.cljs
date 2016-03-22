(ns hello-world.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe register-sub dispatch dispatch-sync]]
            [hello-world.comms :as comms]
            [taoensso.timbre :refer-macros [spy info]]))

(register-sub
 :get-state
 (fn [db [_ & keys]]
   (reaction (get-in @db keys))))

(register-sub
 :send-websocket
 (fn [db [_] [wheels-position speed]]
   (do
     (let [open? (get-in @db [:net :open])
           error? (get-in @db [:net :error])
           ws (get-in @db [:net :ws])
           ready-state (get-in @db [:net :ready-state])]
       (if (and open? (not error?) (some? ws) (= :open ready-state))
         (do
           (comms/enqueue-message (spy {:wheels wheels-position
                                        :speed speed}))
           (reaction true))
         (reaction false))))))
