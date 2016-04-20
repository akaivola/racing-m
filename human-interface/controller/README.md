# controller

React Native UI for interfacing with a ESP8266 websocket server hidden in a plastic monster truck with red and blue flashy lights.

## Usage

# initial steps

`re-natal upgrade`
`re-natal deps`
`re-natal use-android-device real`

# when nothing works

`rm -rf android/build\
  android/app/build \
  node_modules/react-native-sensor-manager/android/build`
`re-natal use-figwheel`

Run steps below

# figwheel

`re-natal use figwheel`
`lein figwheel android`

.. new terminal ..

`react-native run-android`

# Logging:

`adb reverse tcp:3449 tcp:3449`
`adb reverse tcp:8081 tcp:8081`
`adb logcat ReactNativeJS:I \*:S`


## License
```
       DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                    Version 2, December 2004

 Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>

 Everyone is permitted to copy and distribute verbatim or modified
 copies of this license document, and changing it is allowed as long
 as the name is changed.

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

  0. You just DO WHAT THE FUCK YOU WANT TO.
```
