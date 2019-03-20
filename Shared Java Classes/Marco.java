package edu.mtu.polofirstresponder;

import com.google.android.gms.maps.model.LatLng;

public class Marco extends PoloUser {

    private static final int MARCO = 2; //identifier for MARCO type of account

    //motors
    private int leftMotor;      //holds speed of devices left motor
    private int rightMotor;     //hold speed of devices right motor

    //sensors
    private int o2;     //sensor value for oxygen
    private int mq2;    //sensor value for smoke and flammable gasses
    private int mq5;    //sensor value for natural gas
    private int mq7;    //sensor value for carbon monoxide

    /**
     * Create a Marco object for writing to the firebase.
     * @param marcoID - The unique ID for the MARCO device.
     */
    public Marco(String marcoID){

        super(MARCO, marcoID);  //call super constructor for MARCO device

        //initialize MARCO motors and sensors
        leftMotor = 0;  //set left motor to stop
        rightMotor = 0; //set right motor to stop
        o2 = 0;         //set default oxygen value
        mq2 = 0;        //set default smoke and flammable gasses value
        mq5 = 0;        //set default natural gas value
        mq7 = 0;        //set default carbon monoxide value
    }

    /**
     * Create a Marco object for writing to the firebase.
     * @param userID - The unique ID for the MARCO device.
     * @param position - The MARCO device's current position.
     * @param leftMotor - The speed of the left side motor.
     * @param rightMotor - The speed of the right side motor.
     * @param o2 - The value of the oxygen gas sensor.
     * @param mq2 - The value of the smoke and flammable gasses sensors.
     * @param mq5 - The value of the natural gas sensor.
     * @param mq7 - The value of the carbon monoxide sensor.
     */
    public Marco(String userID, LatLng position, int leftMotor, int rightMotor, int o2, int mq2, int mq5, int mq7){

        super(MARCO, userID, position);   //call super constructor for MARCO device

        //set MARCO motors and sensors
        this.leftMotor = leftMotor;     //set the left motor
        this.rightMotor = rightMotor;   //set the right motor
        this.o2 = o2;                   //set the oxygen value
        this.mq2 = mq2;                 //set the smoke and flammable gas value
        this.mq5 = mq5;                 //set the natural gas value
        this.mq7 = mq7;                 //set the carbon monoxide value
    }

    /*** Setter Functions ***/
    public void setLeftMotor(int leftMotor){this.leftMotor = leftMotor;}        //set the left side motor
    public void setRightMotor(int rightMotor){this.rightMotor = rightMotor;}    //set the right side motor
    public void setO2(int o2){this.o2 = o2;}                                    //set the oxygen sensor value
    public void setMq2(int mq2){this.mq2 = mq2;}                                //set the smoke and flammable gasses sensor value
    public void setMq5(int mq5){this.mq5 = mq5;}                                //set the natural gas sensor value
    public void setMq7(int mq7){this.mq7 = mq7;}                                //set the carbon monoxide sensor value

    /*** Getter Functions ***/
    public int getLeftMotor(){return leftMotor;}    //get the left motor speed value
    public int getRightMotor(){return rightMotor;}  //get the right motor speed value
    public int getO2(){return o2;}                  //get the o2 sensor value
    public int getMq2(){return mq2;}                //get the smoke and flammable gasses sensor value
    public int getMq5(){return mq5;}                //get the natural gas sensor value
    public int getMq7(){return mq7;}                //get the carbon monoxide sensor value
}
