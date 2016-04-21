(ns controller.loops
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [controller.comms :as comms]
   [cljs.core.async :refer [<! timeout]]
   [re-frame.core :refer [subscribe dispatch dispatch-sync]]
   [taoensso.timbre :refer-macros [spy info warn debug]]))

(defn loops []
  ; readystate polling
  (go-loop []
    (let [readystate (subscribe [:get-state :net :ready-state])]
      (when (some? readystate)
        (case @readystate
          :closed (dispatch [:init-websocket])
          nil))
      (<! (timeout 100))
      (dispatch [:update-readystate]))
    (recur))

  ; throttled websocket messages
  (go-loop []
    (let [message (<! comms/messages)
          ws      (subscribe [:get-state :net :ws])]
      (try
        (some->> message
                 (clj->js)
                 (.stringify js/JSON)
                 (.send @ws))
        (catch :default e (warn e)))
      (<! (timeout 50)))
    (recur))

  )
