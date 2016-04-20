(ns controller.android.gyro)

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

(defn console-listener [data]
  (.dir js/console data))

(defn hooks []
  (do
    (hook-gyro! 50 console-listener)
    (hook-magnetometer! 50 console-listener)))
