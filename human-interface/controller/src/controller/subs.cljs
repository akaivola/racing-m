(ns controller.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe register-sub dispatch dispatch-sync]]
            [controller.comms :as comms]
            [taoensso.timbre :refer-macros [spy info]]))

(register-sub
  :get-state
  (fn [db [_ & keys]]
    (reaction (get-in @db keys))))
