#include <Arduino.h>

class SerialServo {
 public:
  SerialServo();
  void setup();
  void write(int pos);
};
