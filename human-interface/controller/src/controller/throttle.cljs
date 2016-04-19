(ns controller.throttle
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub register-handler]]
            [taoensso.timbre :refer-macros [spy]]))

(def initial-state
  {:speed 1023
   :min 0
   :max 2046})

(register-handler
 :throttle/zero
 (fn [db _]
   (assoc-in db [:throttle :speed] 1023)))

(register-handler
 :throttle/set
 (fn [db [_ speed]]
   (assoc-in db [:throttle :speed] speed)))
