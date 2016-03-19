#include "SerialServo.h"

SerialServo::SerialServo() {
}

void write(int pos) {
  Serial.write(pos);
  Serial.write('\n');
  Serial.flush();
}

void SerialServo::setup() {
  Serial.begin(9600);
}

void SerialServo::write(int pos) {
  write(pos);
}
