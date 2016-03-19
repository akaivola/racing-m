(ns hello-world.android.core
  (:require-macros
   [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [cljs.core.async :refer [<! timeout]]
            [hello-world.handlers]
            [hello-world.subs]
            [taoensso.timbre :refer-macros [spy info debug]]))

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
        endpoint-answer (subscribe [:send-websocket] [wheels-raw])
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

(defn wheels []
  (let [wheels-position     (subscribe [:wheels/position])
        wheels-raw-position (subscribe [:get-state :wheels :raw])
        min                 (subscribe [:get-state :wheels :min])
        max                 (subscribe [:get-state :wheels :max])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-weight "bold"}} "Wheel angle " @wheels-position]
       [text {:style {:font-weight "bold"}} "Raw angle " @wheels-raw-position]

       [slider {:style {:width 240
                        :height 30
                        :margin 10}
                :minimum-value @min
                :maximum-value @max
                :on-value-change #(dispatch-sync [:wheels/update-raw (Math/floor %)])
                :step 1
                :value @wheels-raw-position}]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(dispatch-sync [:wheels/zero])}
        [text {:style {:text-align "center" :font-weight "bold"}} "Zero position"]]])))

(defn app-root []
  (fn []
    [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
     [text {:style {:font-size 20 :font-weight "100" :margin-bottom 20 :text-align "center"}}
      (str (js/Date.))]
     [image {:source logo-img
             :style  {:width 40 :height 40 :margin-bottom 20}}]

     [endpoint]
     [wheels]]))

(defn loops []
  (go-loop []
    (let [readystate (subscribe [:get-state :net :ready-state])
          answer? (subscribe [:send-websocket] [(subscribe [:get-state :wheels :raw])])]
      (when (and (some? readystate) (some? answer?))
        (case @readystate
          :closed (dispatch [:init-websocket])
          :open (when-not @answer?
                  (do
                    (info "reinitializing dropped connection")
                    (dispatch [:init-websocket])))
          nil))
      (<! (timeout 1000))
      #_(dispatch [:update-readystate]))
    (recur)))

(defn init []
  (dispatch-sync [:initialize-db])
  (loops)
  (dispatch [:init-websocket])
  (.registerComponent app-registry "HelloWorld" #(r/reactify-component app-root)))
