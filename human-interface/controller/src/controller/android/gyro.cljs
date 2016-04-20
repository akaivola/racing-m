(ns controller.android.gyro
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [taoensso.timbre :refer-macros [info]]
            [re-frame.core :refer [dispatch]]
            [cljs.core.async :as a :refer [<! >! chan mix timeout sliding-buffer]]))

(def device-event-emitter (.-DeviceEventEmitter (js/require "react-native")))
(def sensor-manager (.-SensorManager (js/require "NativeModules")))

(defn hook-gyro! [millisec-delay listener]
  (do
    (.addListener device-event-emitter "Gyroscope" listener)
    (.startGyroscope sensor-manager millisec-delay)))

(defn hook-magnetometer! [millisec-delay listener]
  (do
    (.addListener device-event-emitter "Magnetometer" listener)
    (.startMagnetometer sensor-manager millisec-delay)))

(defn chan-listener []
  (let [output (chan (sliding-buffer 1))]
    [(fn [data]
       (go (>! output (js->clj data :keywordize-keys true))))
     output]))

(defn hooks []
  (let [[magneto-listener magneto-chan] (chan-listener)]
    (info "Hook magnetometer")
    (hook-magnetometer! 33 magneto-listener)

    (go-loop []
      (let [m (<! magneto-chan)]
        (dispatch [:set-state [:magneto] m])
        (<! (timeout 50))
        (recur)))))
