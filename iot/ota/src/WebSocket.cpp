#include "WebSocket.h"
#include <WebSocketsServer.h>
#include <Hash.h>

WebSocketsServer* webSocket;
WebSocket* me;

String parseJson(char * unparsedJson) {
  StaticJsonBuffer<100> jsonBuffer;
  JsonObject& root = jsonBuffer.parseObject(unparsedJson);
  // const char * jotain = root["jotain"];
  if (root.success()) {
    me->onJsonCallBack(root);
    return "ok";
  } else {
    return "ko";
  }
}

void webSocketEvent(uint8_t num, WStype_t type, uint8_t * payload, size_t length) {
  switch(type) {
  case WStype_DISCONNECTED:
    // flash leds?
    Serial.printf("[%u] Disconnected!\n", num);
    break;
  case WStype_CONNECTED:
    // flash leds?
    webSocket->sendTXT(num, "Connected");
    break;
  case WStype_TEXT:
    webSocket->sendTXT(num, "" + parseJson((char *) payload));
    // send data to all connected clients
    // webSocket.broadcastTXT("message here");
    break;
  case WStype_BIN:
    //Serial.printf("[%u] get binary lenght: %u\n", num, length);
    //hexdump(payload, length);

    // send message to client
    // webSocket.sendBIN(num, payload, lenght);
    break;
  }
}

WebSocket::WebSocket(int port, JSON_CALLBACK(fn)) {
  onJsonCallBack = fn;
  me = this;
  webSocket = new WebSocketsServer(port);
  webSocket->begin();
  webSocket->onEvent(webSocketEvent);
}

void WebSocket::handle() {
  webSocket->loop();
}
