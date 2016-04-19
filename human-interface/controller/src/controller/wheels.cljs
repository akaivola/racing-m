(ns controller.wheels
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub register-handler]]
            [schema.core :as s :include-macros true]
            [taoensso.timbre :refer-macros [spy]]))

(def Posint
  (s/conditional (s/pred pos?) s/Int))

(defn halfway [min max]
  {:pre [(some? (s/check Posint min))
         (some? (s/check Posint max))
         (> max min)]}
  (-> max
      (- min)
      (/ 2)
      (+ min)))

(def initial-state
  (let [min 120
        max 60
        begin-state (halfway max min)]
    {:raw begin-state
     :min min
     :max max
     :zero begin-state
     :reverse true}))

;;; SUBS

(register-sub
 :wheels/position
 (fn [db _]
   (reaction
    (or (- (get-in @db [:wheels :raw])
           (get-in @db [:wheels :zero]))))))

;;; HANDLERS

(register-handler
 :wheels/zero
 (fn [db _]
   (assoc-in db [:wheels :zero] (-> db :wheels :raw))))

(register-handler
 :wheels/update-raw
 (fn [db [_ raw]]
   (assoc-in db [:wheels :raw] raw)))
