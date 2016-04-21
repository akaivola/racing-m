(ns controller.drive
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
   [cljs.core.async :as a :refer [<! >! chan timeout sliding-buffer]]))

(def initial-state
  {:drive? false
   :magneto {:x 0
             :y 0
             :z 0}
   :magneto-normal {:x 0
                    :y 0
                    :z 0}})

(defn throttle-subscribe []
  (let [throttle-raw (subscribe [:get-state :drive :magneto :x])
        throttle-normal (subscribe [:get-state :drive :magneto-normal :x])
        wheels-raw (subscribe [:get-state :drive :magneto :y])
        wheels-normal (subscribe [:get-state :drive :magneto-normal :y])]
    (subscribe [:drive/throttles [throttle-raw throttle-zero wheels-raw wheels-zero]])))

(defn raw->human [number]
  (-> number
      (* 10)
      Math/round
      (/ 10)))

(register-sub
  :drive/throttles
  (fn [db _ [throttle-raw throttle-zero wheels-raw wheels-zero]]
    (reaction
      {:throttle (raw->human (- throttle-raw throttle-zero))
       :wheels   (raw->human (- wheels-raw wheels-zero))})))

(register-handler
  :drive/start-drive
  (fn [db _]
    (assoc-in db [:drive]
              {:magneto-zero (-> db :drive :magneto)
               :drive?       true})))

(register-handler
  :drive/stop-drive
  (fn [db _]
    (assoc-in db [:drive :drive?] false)))
