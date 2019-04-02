package edu.mtu.polofirstresponder;

import com.google.android.gms.maps.model.LatLng;

public class Marco  {

    //position
    private LatLng position;    //MARCO's current position

    //motors
    private int leftMotor;      //holds speed of devices left motor
    private int rightMotor;     //hold speed of devices right motor

    //sensors
    private double o2;     //sensor value for oxygen
    private double mq2;    //sensor value for smoke and flammable gasses
    private double mq5;    //sensor value for natural gas
    private double mq7;    //sensor value for carbon monoxide
    private double temp;   //sensor value for current temperature

    //URL
    private String url; //the URL of MARCO's YouTube Livestream

    /**
     * Create a Marco object for writing to the firebase.
     */
    public Marco(){

        //initialize MARCO motors and sensors
        url = "https://www.youtube.com/watch?v=qWFanYD3cgo";    //set the default URL
        position = new LatLng(0,0);                      //set the default MARCO position
        leftMotor = 0;                                          //set left motor to stop
        rightMotor = 0;                                         //set right motor to stop
        o2 = 0;                                                 //set default oxygen value
        mq2 = 0;                                                //set default smoke and flammable gasses value
        mq5 = 0;                                                //set default natural gas value
        mq7 = 0;                                                //set default carbon monoxide value
        temp = 0;                                               //set default temperature value
    }

    /**
     * Create a Marco object for writing to the firebase.
     * @param url - The URL for the YouTube livestream.
     * @param position - The MARCO device's current position.
     * @param leftMotor - The speed of the left side motor.
     * @param rightMotor - The speed of the right side motor.
     * @param o2 - The value of the oxygen gas sensor.
     * @param mq2 - The value of the smoke and flammable gasses sensors.
     * @param mq5 - The value of the natural gas sensor.
     * @param mq7 - The value of the carbon monoxide sensor.
     * @param temp - The value of the temperature sensor.
     */
    public Marco(String url, LatLng position, int leftMotor, int rightMotor, double o2, double mq2, double mq5, double mq7, double temp){

        //set MARCO motors and sensors
        this.position = position;       //set the position of the MARCO device
        this.leftMotor = leftMotor;     //set the left motor
        this.rightMotor = rightMotor;   //set the right motor
        this.o2 = o2;                   //set the oxygen value
        this.mq2 = mq2;                 //set the smoke and flammable gas value
        this.mq5 = mq5;                 //set the natural gas value
        this.mq7 = mq7;                 //set the carbon monoxide value
        this.url = url;                 //set the Marco Livestream URL
    }

    /*** Setter Functions ***/
    public void setLeftMotor(int leftMotor){this.leftMotor = leftMotor;}        //set the left side motor
    public void setRightMotor(int rightMotor){this.rightMotor = rightMotor;}    //set the right side motor
    public void setO2(double o2){this.o2 = o2;}                                 //set the oxygen sensor value
    public void setMq2(double mq2){this.mq2 = mq2;}                             //set the smoke and flammable gasses sensor value
    public void setMq5(double mq5){this.mq5 = mq5;}                             //set the natural gas sensor value
    public void setMq7(double mq7){this.mq7 = mq7;}                             //set the carbon monoxide sensor value
    public void setTemp(double temp){this.temp = temp;}                         //set the temperature sensor value
    public void setURL(String url){this.url = url;}                             //set the URL for the YouTube Livestream

    /*** Getter Functions ***/
    public int getLeftMotor(){return leftMotor;}    //get the left motor speed value
    public int getRightMotor(){return rightMotor;}  //get the right motor speed value
    public double getO2(){return o2;}               //get the o2 sensor value
    public double getMq2(){return mq2;}             //get the smoke and flammable gasses sensor value
    public double getMq5(){return mq5;}             //get the natural gas sensor value
    public double getMq7(){return mq7;}             //get the carbon monoxide sensor value
    public double getTemp(){return temp;}           //get the temperature sensor value
    public String getURL(){return url;}             //get the URL for the YouTube Livestream
}
