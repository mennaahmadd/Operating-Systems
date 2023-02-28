

#include <Arduino_FreeRTOS.h>
#define ENA 9
#define motorRight_1 8 //MOVES THE RIGHT WHEELS FORWARD
#define motorRight_2 7 //MOVES THE RIGHT WHEELS BACKWARD
#define ENB 3
#define motorLeft_3 5 //MOVES THE LEFT WHEELS BACKWARD
#define motorLeft_4 4 //MOVES THE LEFT WHEELS FORWARD
#define WarningLed 2
#define ldrLed 10
int right ;
int left;
//int leftSensor;        
int ldrSensor;
bool StoppedFlag=false;
void vTask1(void*pvParameters);
void vTask2(void*pvParamaters); 
TaskHandle_t xTask2Handle = NULL;
void setup() {
  Serial.begin(9600);
  pinMode(motorRight_1,OUTPUT);
  pinMode(motorRight_2,OUTPUT);
  pinMode(motorLeft_3,OUTPUT);
  pinMode(motorLeft_4,OUTPUT);  
  pinMode(ENA,OUTPUT);
  pinMode(ENB,OUTPUT);  
  pinMode(WarningLed,OUTPUT);
  pinMode(ldrLed,OUTPUT);
//  pinMode(rightSensor,OUTPUT);
//  pinMode(leftSensor,OUTPUT);
  xTaskCreate(vTask1,"LANE TASK",128,NULL,2,NULL);
  xTaskCreate(vTask2,"LDR TASK",150,NULL,1,&xTask2Handle);

}
void FORWARD()
{
digitalWrite(motorRight_1,HIGH);
digitalWrite(motorRight_2,LOW);
digitalWrite(motorLeft_3,LOW);
digitalWrite(motorLeft_4,HIGH);
//PWM
analogWrite(ENA,80);
analogWrite(ENB,80);
}
void RIGHTSENSORDETECT()
{ 
  //Move Right
  //The left wheels are working only 
  //PWM
  analogWrite(ENB,150);
  digitalWrite(motorLeft_3,LOW);
  digitalWrite(motorLeft_4,HIGH);
  //STOP RIGHT MOTORS
  //PWM
  analogWrite(ENA,80);
  digitalWrite(motorRight_1,LOW);
  digitalWrite(motorRight_2,HIGH);
  
}

void LEFTSENSORDETECT()
{ 
   //Move Left
  //The right wheels are working only
  //PWM 
  analogWrite(ENA,150);
  digitalWrite(motorRight_1,HIGH);
  digitalWrite(motorRight_2,LOW);
  //STOP LEFT MOTORS
  //PWM
  analogWrite(ENB,80);
  digitalWrite(motorLeft_3,HIGH);
  digitalWrite(motorLeft_4,LOW);
  
}
void STOP(){
digitalWrite(motorRight_1,LOW);
digitalWrite(motorRight_2,LOW);
digitalWrite(motorLeft_3,LOW);
digitalWrite(motorLeft_4,LOW);
}

void vTask1(void* pvParamaters){

//TickType_t xLastWakeTime;
//const TickType_t xDelay600000= pdMS_TO_TICKS(600000);
//xLastWakeTime=xTaskGetTickCount();
UBaseType_t uxPriority ;
uxPriority = uxTaskPriorityGet(NULL);
while(1){
  right= digitalRead(A0);  
  left = digitalRead(A5);
  //Serial.println(right);
  Serial.println(left);
  if(StoppedFlag==false){
  if(right==1&& left==0){ 
    RIGHTSENSORDETECT();
    digitalWrite(WarningLed,HIGH);
    delay(100);
  } 
else if ( left==1 && right==0){
    LEFTSENSORDETECT();
    digitalWrite(WarningLed,HIGH);
    delay(100);
 }
 else if ( left==1&& right==1){
    STOP();
    digitalWrite(WarningLed,LOW);
    StoppedFlag=true;
    delay(100);
  }
else {
 FORWARD();
digitalWrite(WarningLed,LOW);
  }
}
else {
  vTaskPrioritySet(xTask2Handle,2);
  vTaskPrioritySet(NULL, 1);
}

}
}
void vTask2(void*pvParameters){
//  TickType_t xLastWakeTime;
//  const TickType_t xDelay600000= pdMS_TO_TICKS(1000);
//  xLastWakeTime=xTaskGetTickCount();
  UBaseType_t uxPriority ;
  uxPriority = uxTaskPriorityGet(NULL);
  while(1){
    ldrSensor=analogRead(A1);  
    if(ldrSensor>=0 && ldrSensor<=200){
       analogWrite(ldrLed,0);
     }
else if(ldrSensor>=201 && ldrSensor<=600){
       analogWrite(ldrLed,70);
      }
else {
       analogWrite(ldrLed,255);
     }
     //vTaskDelayUntil(&xLastWakeTime,pdMS_TO_TICKS(1000));
  }
}
void loop() {
/*Serial.println(rightSensor);
Serial.println(ldrSensor);
*/
 /*Serial.print("rightSensor: ");
  Serial.println(rightSensor);
  Serial.print("leftSensor: ");
  Serial.println(leftSensor);
 */
}
