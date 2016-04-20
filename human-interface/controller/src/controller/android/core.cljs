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
(def slider (r/adapt-react-class (js/require "react-native-slider")))

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
        wheels-raw (subscribe [:get-state :wheels :raw])
        speed (subscribe [:get-state :throttle :speed])
        endpoint-answer (subscribe [:send-websocket] [wheels-raw speed])
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
        (str "Messages sent: " @endpoint-answer)
        " | Opened: " (str @socket-open?)
        " | Message: " (or (not-empty (str @message))
                           "<none>")
        " | ready-state: " (name (or @ready-state :error))]
       [text (str @reason)]])))

(defn gyro []
  (let [magneto (subscribe [:get-state :magneto])
        disp (fn [axis] (-> @magneto axis (* 10) Math/round (/ 10)))]
    (fn []
      [view {:style {:flex-direction "column" :margin 20 :align-items "center"}}
       [text {:style {:font-weight "bold"}}
        "X: " (disp :x)]
       [text {:style {:font-weight "bold"}}
        "Y: " (disp :y)]
       [text {:style {:font-weight "bold"}}
        "Z: " (disp :z)]])))

(defn wheels []
  (let [wheels-position     (subscribe [:wheels/position])
        wheels-raw-position (subscribe [:get-state :wheels :raw])
        min                 (subscribe [:get-state :wheels :min])
        max                 (subscribe [:get-state :wheels :max])]
    (fn []
      [view {:style {:flex-direction "column" :margin 20 :align-items "center"}}
       [text {:style {:font-weight "bold"}} "Wheel angle " (* -1 @wheels-position)]
       [text {:style {:font-weight "bold"}} "Raw angle " @wheels-raw-position]

       [slider {:style {:width 240
                        :height 30
                        :margin 10}
                :minimum-value @min
                :maximum-value @max
                :on-value-change #(dispatch-sync [:wheels/update-raw (Math/floor %)])
                :step 1
                :value @wheels-raw-position}]
       ])))

(defn throttle []
  (let [speed (subscribe [:get-state :throttle :speed])
        min   (subscribe [:get-state :throttle :min])
        max   (subscribe [:get-state :throttle :max])]
    (fn []
      [view {:style {:flex-direction "column" :margin 20 :align-items "center"}}
       [text {:style {:font-weight "bold"}} "Speed " (- @speed 1023)]
       [slider {:style {:width 240
                        :height 30
                        :margin 10}
                :minimum-value @min
                :maximum-value @max
                :on-value-change #(dispatch-sync [:throttle/set (Math/floor %)])
                :step 1
                :value @speed}]
       ])))

(defn app-root []
  (fn []
    [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
     [text {:style {:font-size 20 :font-weight "100" :margin-bottom 20 :text-align "center"}}
      (str (js/Date.))]
     [image {:source logo-img
             :style  {:width 40 :height 40 :margin-bottom 20}}]

     [endpoint]
     [gyro]
     [wheels]
     [view  {:style {:flex-direction "row"}}
      [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                            :on-press #(dispatch-sync [:init-websocket])}
       [text {:style {:text-align "center" :font-weight "bold"}}
        "Reconnect"]]]]))

(defn loops []
  (go-loop []
    (let [readystate (subscribe [:get-state :net :ready-state])]
      (when (some? readystate)
        (case @readystate
          :closed  (dispatch [:init-websocket])
          nil))
      (<! (timeout 100))
      (dispatch [:update-readystate]))
    (recur))
  (go-loop []
    (let [message (<! comms/messages)
          ws (subscribe [:get-state :net :ws])]
      (try
        (some->> message
                 (clj->js)
                 (.stringify js/JSON)
                 (.send @ws))
        (catch :default e (warn e)))
      (<! (timeout 50)))
    (recur)))

(defn hooks []
  (gyro/hooks))

(defn init []
  (dispatch-sync [:initialize-db])
  (loops)
  (hooks)
  (dispatch [:init-websocket])
  (.registerComponent app-registry "Controller" #(r/reactify-component app-root)))
