(ns hello-world.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [taoensso.timbre :refer-macros [spy]]))

(register-sub
 :get-state
 (fn [db [_ & keys]]
   (reaction (get-in @db keys))))
