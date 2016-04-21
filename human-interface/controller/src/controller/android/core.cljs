(ns controller.android.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs.core.async :refer [<! timeout]]
            [controller.android.gyro :as gyro]
            [controller.handlers]
            [controller.subs]
            [controller.wheels :as wheels]
            [controller.throttle]
            [controller.comms :as comms]
            [taoensso.timbre :refer-macros [spy info warn debug]]))

(set! js/React (js/require "react-native"))

(def app-registry (.-AppRegistry js/React))
(def text (r/adapt-react-class (.-Text js/React)))
(def input (r/adapt-react-class (.-TextInput js/React)))
(def view (r/adapt-react-class (.-View js/React)))
(def image (r/adapt-react-class (.-Image js/React)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight js/React)))

(def logo-img (js/require "./images/cljs.png"))

(defn alert [title]
  (.alert (.-Alert js/React) title))

(defn endpoint []
  (let [endpoint (subscribe [:get-state :net :endpoint])
        message (subscribe [:get-state :net :message])
        socket-open? (subscribe [:get-state :net :open])
        reason (subscribe [:get-state :net :close :reason])
        ready-state (subscribe [:get-state :net :ready-state])]
    (fn []
      [view {:style {:flex-direction "column" :align-items "center"}}
       [input {:style {:height 40
                       :border-color "gray"
                       :border-width 1}
               :default-value @endpoint
               :placeholder "NodeMCU endpoint"
               :keyboard-type "url"
               :on-change-text #(dispatch-sync [:set-state [:net :endpoint-edit] (or % @endpoint)])
               :on-end-editing #(do (dispatch-sync [:update-state
                                                    (fn [state]
                                                      (-> (assoc-in state
                                                                    [:net :endpoint]
                                                                    (-> state :net :endpoint-edit))))])
                                    (dispatch [:init-websocket])
                                    (dispatch [:update-readystate]))}]
       [text
        "Opened: " (str @socket-open?)
        " | Message: " (or (not-empty (str @message))
                           "<none>")
        " | ready-state: " (name (or @ready-state :error))]
       [text (str @reason)]])))

(defn gyro []
  (let [throttles (drive/throttle-subscribe)
        disp      (fn [axis] (-> @throttles axis (* 10) Math/round (/ 10)))]
    (fn []
      [view {:style {:flex-direction "column" :margin 20 :align-items "center"}}
       [text {:style {:font-weight "bold"}}
        "Throttle (raw): " (disp :throttle)]
       [text {:style {:font-weight "bold"}}
        "Wheels (raw): " (disp :wheels)]
       ])))

(defn app-root []
  (fn []
    [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
     [image {:source logo-img
             :style  {:width 40 :height 40 :margin-bottom 20}}]

     [endpoint]
     [gyro]
     [view  {:style {:flex-direction "row"}}
      [touchable-highlight {:style    {:background-color "#399"
                                       :padding 30
                                       :margin-right 50
                                       :border-radius 5}
                            :on-press-in #(dispatch [:drive/start-drive])
                            :on-press-out #(dispatch [:drive/stop-drive])}
       [text {:style {:text-align "center" :font-weight "bold"}}
        "Drive"]]
      [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                            :on-press #(dispatch-sync [:init-websocket])}
       [text {:style {:text-align "center" :font-weight "bold"}}
        "Reconnect"]]]]))



(defn hooks []
  (gyro/hooks))

(defn init []
  (do
    (dispatch-sync [:initialize-db])
    (loops/loops)
    (hooks)
    (dispatch [:init-websocket])
    (.registerComponent app-registry "Controller" #(r/reactify-component app-root))))
