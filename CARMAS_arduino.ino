

#include <Wire.h>
#include <VL53L0X.h>
#include <MPU6050.h>
#include <SoftwareSerial.h>
SoftwareSerial BT (10, 11); //11rxd
MPU6050 mpu;
VL53L0X sensor;
VL53L0X sensor2;
double danex[30];  
double daney[30];
double danez[30];
int licznik = 0;
int l_loop = 0;
double danex2[30];  
double daney2[30];
double danez2[30];
int state = 0 ; 

int left,right,distance,duration,middle1,middle2; 
#define HIGH_ACCURACY

#define trigPin1 2
#define echoPin1 3

#define trigPin2 4
#define echoPin2 5

#define trigPin3 8
#define echoPin3 9

#define trigPin4 12
#define echoPin4 13


void setup()
{
  
    pinMode(A3, OUTPUT);
  pinMode(A2, OUTPUT);
  digitalWrite(A2, LOW);
  digitalWrite(A3, LOW);

  delay(500);
  Serial.begin(115200);
  Serial.print("asdasdasd");
  BT.begin(9600);
  Wire.begin();
  pinMode(A3, OUTPUT);
  pinMode(A2, OUTPUT);
  digitalWrite(A2, LOW);
  digitalWrite(A3, LOW);

  delay(500);
  Wire.begin();


  pinMode(A2, INPUT);
  delay(150);
  
  sensor.init(true);

  Serial.println("01");
  delay(100);
  sensor.setAddress((uint8_t)22);
  

  pinMode(A3, INPUT);
    delay(150);
  sensor2.init(true);
 
  delay(100);
  sensor2.setAddress((uint8_t)25);
  

  //sensor.init();
  //sensor.setTimeout(500);

#if defined LONG_RANGE

  sensor.setSignalRateLimit(0.1);
  sensor.setVcselPulsePeriod(VL53L0X::VcselPeriodPreRange, 18);
  sensor.setVcselPulsePeriod(VL53L0X::VcselPeriodFinalRange, 14);
  sensor2.setSignalRateLimit(0.1);
  sensor2.setVcselPulsePeriod(VL53L0X::VcselPeriodPreRange, 18);
  sensor2.setVcselPulsePeriod(VL53L0X::VcselPeriodFinalRange, 14);
#endif

#if defined HIGH_SPEED
  // reduce timing budget to 20 ms (default is about 33 ms)
  sensor.setMeasurementTimingBudget(20000);
  sensor2.setMeasurementTimingBudget(20000);
#elif defined HIGH_ACCURACY
  sensor.setMeasurementTimingBudget(200000);
  sensor2.setMeasurementTimingBudget(200000);
#endif

  while(!mpu.begin(MPU6050_SCALE_2000DPS, MPU6050_RANGE_2G))
        {
          Serial.println("Error");
          delay(500);
      }
    pinMode(7, OUTPUT);  
    pinMode(6, OUTPUT); 
    //pinMode(A3,OUTPUT);
   // pinMode(A2,OUTPUT);
    pinMode(trigPin1, OUTPUT);
    pinMode(echoPin1, INPUT);
    pinMode(trigPin2, OUTPUT);
    pinMode(echoPin2, INPUT);
    pinMode(trigPin3, OUTPUT);
    pinMode(echoPin3, INPUT);
    pinMode(trigPin4, OUTPUT);
    pinMode(echoPin4, INPUT);
}

void loop()
{

  if(BT.available()>0){
    state = BT.read();
    Serial.println(state);
   
    }

   if(state==49){
    
       
        int jd  = sensor.readRangeSingleMillimeters();
        if (sensor.timeoutOccurred()) { Serial.print(" TIMEOUT"); }
        int jd2 = sensor2.readRangeSingleMillimeters();
        if (sensor2.timeoutOccurred()) { Serial.print(" TIMEOUT"); }

        if(jd>2000){
          jd =1500;}
          if(jd2>2000){
          jd2 =1500;}

      
       middle1 = getDistance(trigPin1,echoPin1);


        BT.print(1);
        BT.print(" ");
        BT.print(jd2/10);
        //BT.print(left);
        BT.print(" ");
        BT.print(middle1);
        BT.print(" ");
        BT.print(jd/10);

       delay(20);
       left = getDistance(trigPin2,echoPin2);
       delay(40); 
       right = getDistance(trigPin4,echoPin4);
       delay(40); 
       middle2 = getDistance(trigPin3,echoPin3);

        BT.print(2);
        BT.print(" ");
        BT.print(left);
        BT.print(" ");
        BT.print(middle2);
        BT.print(" ");
        BT.print(right);   
        Serial.print(jd2/10);
        Serial.print(" ");
        Serial.print(middle1);
        Serial.print(" ");
        Serial.print(jd/10);
        Serial.print(" ");
        Serial.print(left);
        Serial.print(" ");
        Serial.print(middle2);
        Serial.print(" ");
        Serial.print(right);
        Serial.println();
        delay(30);
 }else if(state == 48){
     // digitalWrite(Led,LOW);
        Serial.println();
        Serial.print(" oczekiwanie ");
        Serial.println();
        delay(400); 
 }else if(state == 50){
       Serial.println();
        Serial.print(" oczekiwanie ");
        Serial.println();
        delay(400); 
      //digitalWrite(Led,HIGH);   
 }else if(state == 51){
/////////////////////////////////////////////////////////////////////////////
       
      /////////////////////////////////////////////////////////////////////////////
        digitalWrite(7,HIGH);
        float temp = mpu.readTemperature();
        Vector rawAccel = mpu.readRawAccel();
        Vector normAccel = mpu.readNormalizeAccel();
      
        danex[licznik] = normAccel.XAxis;
        daney[licznik] = normAccel.YAxis; 
        danez[licznik] = normAccel.ZAxis;
      
      
      
        digitalWrite(6,HIGH);
        digitalWrite(7,LOW); //niebieski on 
        float temp2 = mpu.readTemperature();
        rawAccel = mpu.readRawAccel();
        normAccel = mpu.readNormalizeAccel();
        danex2[licznik] = normAccel.XAxis;
        daney2[licznik] = normAccel.YAxis; 
        danez2[licznik] = normAccel.ZAxis;
      /////////////////////////////////////////////////////////////////////////////
        Serial.print(" Temp1= ");
        Serial.println(temp);
        
        Serial.print(" Xnorm = ");
        Serial.print(danex[licznik]);
        Serial.print(" Ynorm = ");
        Serial.print(daney[licznik]);
        Serial.print(" Znorm = ");
        Serial.println(danez[licznik]);
      /////////////////////////////////////////////////////////////////////////////
        Serial.print(" Temp2= ");
        Serial.println(temp);
        
        Serial.print(" Xnorm2 = ");
        Serial.print(danex2[licznik]);
        Serial.print(" Ynorm2 = ");
        Serial.print(daney2[licznik]);
        Serial.print(" Znorm2 = ");
        Serial.println(danez2[licznik]);
      /////////////////////////////////////////////////////////////////////////////
      
        digitalWrite(6,LOW);
        digitalWrite(7,LOW);
        delay(100);
        if(licznik>2 || (licznik>1 && l_loop>0)){
          if(fabs(danex[licznik]-danex[licznik-1])>15 || fabs(danex[licznik]-danex[licznik-2])>15 || fabs(daney[licznik]-daney[licznik-1])>15 || fabs(daney[licznik]-daney[licznik-2])>15 ){
              Serial.println(" ");
              Serial.print("bum");
              BT.print("bum");
              Serial.println(" ");
            }else{
               BT.print("as");
            }
         }
        
         licznik++;
            if(licznik>=30){
              l_loop++;
              licznik=0;
              }
           }
  
}

int getDistance(int trigPin,int echoPin)
{
digitalWrite(trigPin, LOW);
delayMicroseconds(2);
digitalWrite(trigPin, HIGH);
delayMicroseconds(10);
digitalWrite(trigPin, LOW);
duration = pulseIn(echoPin, HIGH);
distance = (duration/2) / 29.1;
return distance;                        
}
