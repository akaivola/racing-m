#include "Test.h"

int pos = 0;

Test::Test() {
}

void setupServo() {
  Serial.begin(9600);
}

void write(int pos) {
  Serial.write(pos);
  Serial.write('\n');
  Serial.flush();
}

void servoLoop() {
  for (pos = 30; pos <= 150; pos += 1) {
    write(pos);
    delay(16);
  }
  for (pos = 150; pos >= 30; pos -= 1) {
    write(pos);
    delay(16);
  }
}

void Test::setup() {
  setupServo();
}

void Test::loop() {
  servoLoop();
}
