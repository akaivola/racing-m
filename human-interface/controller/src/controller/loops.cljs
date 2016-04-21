(ns controller.loops)

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
