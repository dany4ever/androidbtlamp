/*
  Multicolor Lamp (works with Amarino and the MultiColorLamp Android app)
 
 - based on the Amarino Multicolor Lamp tutorial
 - receives custom events from Amarino changing color accordingly
 
 author: Bonifaz Kaufmann - December 2009
 */

#include <MeetAndroid.h>
#include <Servo.h> 

// declare MeetAndroid so that you can call functions with it
MeetAndroid meetAndroid;

// we need 3 PWM pins to control the leds
int blueLed = 9;
int redLed = 10;  
int greenLed = 11;  

//Servo myservo;  // create servo object to control a servo **NOT used**
// a maximum of eight servo objects can be created 
int pos = 0;

// Range Finder Variables
const int pingPin = 7;
int tooClose = 5;
int tooCloseLed =5;
int tooFar = 7;
int tooFarLed = 6;

// reading variable sensor
int sensor = 4;

void setup()  
{
  // use the baud rate your bluetooth module is configured to 
  // not all baud rates are working well, i.e. ATMEGA168 works best with 57600
  Serial.begin(115200); 

  // register callback functions, which will be called when an associated event occurs.
  meetAndroid.registerFunction(blue, 'b');
  meetAndroid.registerFunction(red, 'r');
  meetAndroid.registerFunction(green, 'g');

  // set all color leds as output pins
  pinMode(blueLed, OUTPUT);
  pinMode(redLed, OUTPUT);
  pinMode(greenLed, OUTPUT);
  // just set all leds to high so that we see they are working well
  digitalWrite(blueLed, HIGH);
  digitalWrite(redLed, HIGH);
  digitalWrite(greenLed, HIGH);

  // myservo.attach(11);  // attaches the servo on pin 11 to the servo object 
  
  //range finder setup
  pinMode(tooCloseLed, OUTPUT);
  pinMode(tooFarLed, OUTPUT);
  
  //
  pinMode(sensor, INPUT);
  

}

void loop()

{
  ////////////////////Meet Android, Recieve Events///////////////////////
  meetAndroid.receive(); 
  //////////////////Range Finder Loop Setup///////////////////////

  // establish variables for duration of the ping, 
  // and the distance result in inches and centimeters:
  long duration, inches, cm;

  // The PING))) is triggered by a HIGH pulse of 2 or more microseconds.
  // Give a short LOW pulse beforehand to ensure a clean HIGH pulse:
  pinMode(pingPin, OUTPUT);
  digitalWrite(pingPin, LOW);
  delayMicroseconds(2);
  digitalWrite(pingPin, HIGH);
  delayMicroseconds(5);
  digitalWrite(pingPin, LOW);

  // The same pin is used to read the signal from the PING))): a HIGH
  // pulse whose duration is the time (in microseconds) from the sending
  // of the ping to the reception of its echo off of an object.
  pinMode(pingPin, INPUT);
  duration = pulseIn(pingPin, HIGH);

  // convert the time into a distance
  inches = microsecondsToInches(duration);
  cm = microsecondsToCentimeters(duration);

  Serial.print(inches);
  Serial.print("in, ");
  Serial.print(cm);
  Serial.print("cm");
  Serial.println();
  Serial.print("Too close distance is");
  Serial.println(tooClose);
  
  if(inches < tooClose)
  {
  digitalWrite(tooCloseLed, HIGH);
  digitalWrite(tooFarLed, LOW);
  }
  else if(inches > tooFar)
  {
    digitalWrite(tooCloseLed, LOW);
    digitalWrite(tooFarLed, HIGH);
  }
  
  
  ////////////////////////Send Sensor Data to Android////////////
  // read input pin and send result to Android
  meetAndroid.send(analogRead(sensor));
  
  
  
  //delay the loop to give android and arduino a chance to process the info
  delay(100);
  
  

}


////////////////////Meet Android, Recieve Events///////////////////////

//dims blue light
void blue(byte flag, byte numOfValues)
{
  analogWrite(blueLed, meetAndroid.getInt());
}

//dims red light
void red(byte flag, byte numOfValues)
{
  analogWrite(redLed, meetAndroid.getInt());
}

//dims green light
void green(byte flag, byte numOfValues)
{
  analogWrite(greenLed, meetAndroid.getInt());
}


/////////////////Range Finder Pulsing///////////////////////


long microsecondsToInches(long microseconds)
{
  // According to Parallax's datasheet for the PING))), there are
  // 73.746 microseconds per inch (i.e. sound travels at 1130 feet per
  // second).  This gives the distance travelled by the ping, outbound
  // and return, so we divide by 2 to get the distance of the obstacle.
  // See: http://www.parallax.com/dl/docs/prod/acc/28015-PING-v1.3.pdf
  return microseconds / 74 / 2;
}

long microsecondsToCentimeters(long microseconds)
{
  // The speed of sound is 340 m/s or 29 microseconds per centimeter.
  // The ping travels out and back, so to find the distance of the
  // object we take half of the distance travelled.
  return microseconds / 29 / 2;
}



//////////////////////////Servo///////////////////////////////////


/*void servosweep()
 
 {  
 for(pos = 0; pos < 180; pos += 1)  // goes from 0 degrees to 180 degrees 
 {                                  // in steps of 1 degree 
 myservo.write(pos);              // tell servo to go to position in variable 'pos' 
 delay(15);                       // waits 15ms for the servo to reach the position 
 } 
 for(pos = 180; pos>=1; pos-=1)     // goes from 180 degrees to 0 degrees 
 {                                
 myservo.write(pos);              // tell servo to go to position in variable 'pos' 
 delay(15);                       // waits 15ms for the servo to reach the position 
 } 
 
 }*/




