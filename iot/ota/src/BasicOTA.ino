#include <ESP8266WiFi.h>
#include <ESP8266mDNS.h>
#include <WiFiUdp.h>
#include <ArduinoOTA.h>
#include "WebSocket.h"
#include <functional>
#include <sstream>

const char* ssid = "4G-Gateway-8EA8";
const char* password = "FJ320M36723";
WebSocket* ws;

void setupOTA() {
  WiFi.mode(WIFI_AP);
  WiFi.softAP("racing-m", "12345678",5,0);

  // Port defaults to 8266
  // ArduinoOTA.setPort(8266);

  // Hostname defaults to esp8266-[ChipID]
  ArduinoOTA.setHostname("racing-m");

  // No authentication by default
  // ArduinoOTA.setPassword((const char *)"123");

  //ArduinoOTA.onStart([]() {
  //  Serial.println("Start");
  //});
  //ArduinoOTA.onEnd([]() {
  //  Serial.println("\nEnd");
  //});
  //ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
  //  Serial.printf("Progress: %u%%\r", (progress / (total / 100)));
  //});
  //ArduinoOTA.onError([](ota_error_t error) {
  //  Serial.printf("Error[%u]: ", error);
  //  if (error == OTA_AUTH_ERROR) Serial.println("Auth Failed");
  //  else if (error == OTA_BEGIN_ERROR) Serial.println("Begin Failed");
  //  else if (error == OTA_CONNECT_ERROR) Serial.println("Connect Failed");
  //  else if (error == OTA_RECEIVE_ERROR) Serial.println("Receive Failed");
  //  else if (error == OTA_END_ERROR) Serial.println("End Failed");
  //});
  ArduinoOTA.begin();
  //Serial.println("Ready");
  //Serial.print("IP address: ");
  //Serial.println(WiFi.localIP());
}

void setupMotor() {
  pinMode(0, OUTPUT);
  pinMode(5, OUTPUT);
  analogWrite(5, 0);
  digitalWrite(0, LOW);
}

void setMotor(int pin, int speed) {
  analogWrite(pin, speed);
}

void setup() {
  Serial.begin(4800);
  setupMotor();
  setupOTA();
  ws = new WebSocket(8080, [](JsonObject& root) {
    const int pos = atoi(root["wheels"]);
    if (pos > 0) {
      Serial.write(pos);
      Serial.flush();
    }
    const int speed = atoi(root["speed"]) - 1023;
    digitalWrite(0, speed < 0 ? HIGH : LOW);
    setMotor(5, abs(speed));
  });
}

void loop() {
  ws->handle();
  ArduinoOTA.handle();
}
