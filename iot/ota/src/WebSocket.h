#include <Arduino.h>
#include <functional>
#include <ArduinoJson.h>

#define JSON_CALLBACK(callback) void (*callback)(JsonObject&)

class WebSocket {
 public:
  WebSocket(int port, JSON_CALLBACK(fn));
  void handle();
  JSON_CALLBACK(onJsonCallBack);
};
