#include <Servo.h>
#include <SPI.h>
#include <WiFi.h>

// SERVO
Servo myservo;                // Servo instance
int pos = 0;                  // Orientation/Position in degrees

// WIFI SHIELD
char ssid[] = "";            // Name for your network
int status = WL_IDLE_STATUS;
WiFiServer server(5556);
WiFiClient clientSocket;

// PINS
int outPWMUP = 2;
int outPWMDOWN = 3;
int right = 8;
int left = 9;
int enable1 = 5;
int enable2 = 6;

// OTHER
String mail;                  // String to be sent
double measureDistance[4];
int angle1 = 0;
int angle2 = 90;
int angle3 = 180;
int angle4 = 270;

int up = 0;
int down = 0;

/* SETUP */
void setup() {
  Serial.begin(9600);

  // Servo
  myservo.attach(13);
  myservo.write(0);

  // Sensor 1
  pinMode(21, INPUT);
  pinMode(14, OUTPUT);
  // Sensor 2
  pinMode(20, INPUT);
  pinMode(15, OUTPUT);
  // Sensor 3
  pinMode(19, INPUT);
  pinMode(16, OUTPUT);
  // Sensor 4
  pinMode(18, INPUT);
  pinMode(17, OUTPUT);
  // Initial trigger
  digitalWrite(14, LOW);
  digitalWrite(15, LOW);
  digitalWrite(16, LOW);
  digitalWrite(17, LOW);

  // Car steer
  pinMode(outPWMUP, OUTPUT);
  pinMode(outPWMDOWN, OUTPUT);
  pinMode(right, OUTPUT);
  pinMode(left, OUTPUT);
  pinMode(enable1, OUTPUT);
  pinMode(enable2, OUTPUT);
  digitalWrite(enable1, HIGH);
  digitalWrite(enable2, HIGH);

  // Check for the presence of the WiFi shield.
  if (WiFi.status() == WL_NO_SHIELD)
  {
    // If no shield, print message and exit setup.
    Serial.println("WiFi shield not present");
    status = WL_NO_SHIELD;
    return;
  }

  // Attempt to connect to the open network
  while ( status != WL_CONNECTED) {
    Serial.print("Attempting to connect to open network: ");
    Serial.println(ssid);
    status = WiFi.begin(ssid);
    delay(1000);
    if (status != WL_CONNECTED) {
      Serial.println("Couldn't connect, try again");
    }
  }

  // Once connected
  Serial.println("Connected to the network");
  // Start TCP server
  server.begin();
  // Print connection info
  printWifiStatus();
  Serial.println("Listening on port 5556");
}

/* MAIN LOOP */
void loop() {
  // Check connection
  if (status != WL_CONNECTED){return;}
  // Active wait for client connection
  bool doneConnect = false;
  while (!doneConnect) {
    WiFiClient client = server.available();
    if (client) {
      doneConnect = true;
      clientSocket = client;
    }
  }
  Serial.println("Connected to client socket!");

  // Instruction-execute loop
  bool live = true;
  while (live) {
    // Read a byte.
    if (clientSocket.available()) {
      clientSocket.read(); //Read /n
      char c = clientSocket.read();
      // Kill-connection or other instruction
      if (c == 'K') {
        // ACK
        clientSocket.println('K');
        // Shut down car
        analogWrite(outPWMUP, 0);
        analogWrite(outPWMDOWN, 0);
        analogWrite(right, 0);
        analogWrite(left, 0);
        live = false;
      } else {
        executeInstruction(c);
      }
    }
  }
  clientSocket.stop();
}

/* READ 4 SENSORS */
void measure() {
  for (int control = 0; control < 5; control++) {
    unsigned long duration = 0;
    float distance = 0.0;
    if (control == 0) {
      // Log
      Serial.write("Starting measure Sensor1...\r\n");
      // Trigger
      digitalWrite(14, LOW);
      delayMicroseconds(2);
      digitalWrite(14, HIGH);
      delayMicroseconds(10);
      digitalWrite(14, LOW);
      // Measure time for pulse to return
      duration = pulseIn(21, HIGH);
      // Calculate distance (in cm) given time and sound speed
      distance = duration / 58.31;
      if(distance < 400){
        measureDistance[0] = distance;
      }else{
        measureDistance[0] = 0;
      }
      // Log
      Serial.write("Sensor1:\r\n");
      Serial.println(distance);
      delay(400);
    } else if (control == 1) {
      // Log
      Serial.write("Starting measure Sensor2...\r\n");
      // Trigger
      digitalWrite(15, LOW);
      delayMicroseconds(2);
      digitalWrite(15, HIGH);
      delayMicroseconds(10);
      digitalWrite(15, LOW);
      // Measure time for pulse to return
      duration = pulseIn(20, HIGH);
      // Calculate distance (in cm) given time and sound speed
      distance = duration / 58.31;
      if(distance < 400){
        measureDistance[1] = distance;
      }else{
        measureDistance[1] = 0;
      }
      // Log
      Serial.write("Sensor2:\r\n");
      Serial.println(distance);
      delay(400);
    } else if (control == 2) {
      // Log
      Serial.write("Starting measure Sensor3...\r\n");
      // Trigger
      digitalWrite(16, LOW);
      delayMicroseconds(2);
      digitalWrite(16, HIGH);
      delayMicroseconds(10);
      digitalWrite(16, LOW);
      // Measure time for pulse to return
      duration = pulseIn(19, HIGH);
      // Calculate distance (in cm) given time and sound speed
      distance = duration / 58.31;
      if(distance < 400){
        measureDistance[2] = distance;
      }else{
        measureDistance[2] = 0;
      }
      // Log
      Serial.write("Sensor3:\r\n");
      Serial.println(distance);
      delay(400);
    } else if (control == 3) {
      // Log
      Serial.write("Starting measure Sensor4...\r\n");
      // Trigger
      digitalWrite(17, LOW);
      delayMicroseconds(2);
      digitalWrite(17, HIGH);
      delayMicroseconds(10);
      digitalWrite(17, LOW);
      // Measure time for pulse to return
      duration = pulseIn(18, HIGH);
      // Calculate distance (in cm) given time and sound speed
      distance = duration / 58.31;
      if(distance < 400){
        measureDistance[3] = distance;
      }else{
        measureDistance[3] = 0;
      }
      // Log
      Serial.write("Sensor4:\r\n");
      Serial.println(distance);
      delay(400);
    }
    // Finished reading all 4 sensors
    if (control == 4){
      // Build string
      mail = String(measureDistance[0]) + "-" + String(angle1) + "/" + String(measureDistance[1]) + "-" + String(angle2) + "/" + String(measureDistance[2]) + "-" + String(angle3) + "/" + String(measureDistance[3]) + "-" + String(angle4);
      // Update sensor angles
      angle1 = angle1 + 10;
      angle2 = angle2 + 10;
      angle3 = angle3 + 10;
      angle4 = angle4 + 10;
      pos = pos + 10;
      // Set to original position if overspinned
      if (pos > 89){
        pos = 0;
        angle1 = 0;
        angle2 = 90;
        angle3 = 180;
        angle4 = 270;
      }
      // Spinn servo
      myservo.write(pos);
      delay(500);
    }
  }
}

/* INSTRUCTIONS EXECUTION */
void executeInstruction(char c) {
  // Log instruction
  Serial.print(c);
  switch (c) {
    case 'F':
      // FORWARD
      analogWrite(outPWMUP, 255);
      up = 1;
      break;
    case 'B':
      // BACKWARD
      analogWrite(outPWMDOWN, 255);
      down = 1;
      break;
    case 'S':
      // STOP
      if (up == 1) {
        analogWrite(outPWMUP, 0);
        up = 0;
      }
      if (down == 1) {
        analogWrite(outPWMDOWN, 0);
        down = 0;
      }
      break;
    case 'R':
      // RIGHT
      analogWrite(right, 255);
      break;
    case 'L':
      // LEFT
      analogWrite(left, 255);
      break;
    case 'T':
      // STRAINGTEN WHEELS
      analogWrite(right, 0);
      analogWrite(left, 0);
      break;
    case 'D':
      // DATA
      // Disable engines
      digitalWrite(enable1, LOW);
      digitalWrite(enable2, LOW);
      // Read sensors
      measure();
      // Send data
      clientSocket.println(mail);
      // Log
      Serial.println(mail);
      Serial.println();
      break;
    case 'U':
      // RESET SENSORS (no more data retrieval for now)
      angle1 = 0;
      angle2 = 90;
      angle3 = 180;
      angle4 = 270;
      // Reset servo
      myservo.write(0);
      // Re-enable engines
      digitalWrite(enable1, HIGH);
      digitalWrite(enable2, HIGH);
      break;
  }
}

/* PRINT WIFI STATUS */
void printWifiStatus() {
  // Log SSID
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());
  // Log IP
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);
  // Log signal strength
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
  Serial.println();
}
