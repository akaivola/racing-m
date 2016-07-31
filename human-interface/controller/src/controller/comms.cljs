(ns controller.comms
  (:require [taoensso.timbre :refer-macros [spy info debug]]))

(def message (atom nil))

(defn enqueue-message [new-message]
  (swap! message
    (fn [m]
      (if-let [sending? (:sending? m)]
        new-message ; replace entire message
        (merge m new-message))))) ; merge with not-yet-sent message
