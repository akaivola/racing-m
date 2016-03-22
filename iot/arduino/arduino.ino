// digispark servo controller code
#include <SoftSerial.h>
#include <SoftRcPulseOut.h>

#define REFRESH_PERIOD_MS 20
#define NOW               1

#define RX_PIN 1
#define TX_PIN 2
SoftSerial serial = SoftSerial(1,2);

SoftRcPulseOut myservo;
int pos = 90;
int limits[2] = {60,120};
byte inByte;

void setup()
{
  pinMode(0, OUTPUT);

  myservo.attach(0);
  myservo.setMaximumPulse(2200);
  serial.begin(4800);
  pos = limits[0] + ((limits[1] - limits[0]) / 2);
  myservo.write(pos);
}

void loop()
{
  int p = (int) serial.read();
  pos = p >= limits[0] && p <= limits[1] ? p : pos;
  serial.println(pos);
  myservo.write(pos);
  delay(REFRESH_PERIOD_MS);
  SoftRcPulseOut::refresh(NOW);
}
