(ns controller.comms
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [>! chan sliding-buffer]]
            [taoensso.timbre :refer-macros [spy info debug]]))

(defonce messages (chan (sliding-buffer 1)))

(defn enqueue-message [message]
  (go (>! messages message)))
