(ns controller.drive
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [controller.comms :as comms]
   [re-frame.core :refer [register-handler register-sub subscribe dispatch dispatch-sync]]
   [cljs.core.async :as a :refer [<! >! chan timeout sliding-buffer]]
   [taoensso.timbre :refer-macros [spy]]))

(def initial-state
  {:drive? false
   :magneto {:x 0
             :y 0
             :z 0}
   :magneto-zero {:x 0
                  :y 0
                  :z 0}})

(defn throttle-subscribe []
  (let [throttle-raw  (subscribe [:get-state :drive :magneto :z])
        throttle-zero (subscribe [:get-state :drive :magneto-zero :z])]
    (subscribe [:drive/throttles] [throttle-raw throttle-zero])))

(defn- raw->human [number]
  (-> number
      (* 10)
      Math/round
      (/ 10)))

(register-sub
  :drive/throttles
  (fn [db _ [throttle-raw throttle-zero]]
    (let [drive?             (get-in @db [:drive :drive?])
          corrected-throttle (- throttle-raw throttle-zero)
          machine-throttle   (-> corrected-throttle
                                 (Math/pow 2)
                                 (* 4)
                                 (* (if (pos? corrected-throttle)
                                      -1
                                      1))
                                 Math/floor
                                 (+ 1023)
                                 (max (get-in @db [:throttle :min]))
                                 (min (get-in @db [:throttle :max])))]


      (comms/enqueue-message {:speed  (if drive?
                                        machine-throttle
                                        1023)})


      (reaction
        {:throttle (raw->human corrected-throttle)}))))

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
