package edu.mtu.polofirstresponder;

import com.google.android.gms.maps.model.LatLng;

public class Gunshot {

    private LatLng position;    //the location of where the gunshot was detected
    private int threatLvl;      //the amount of time that has passed since shot detected(2 = 0 to 1 minute; 1 = 1 to 5 minutes; 0 > 5 minutes)

    /**
     * Construct a gunshot class that is used to read and write to Firestore.
     */
    public Gunshot(){

        this.position = new LatLng(0,0);   //set the location of gunshot detection
        threatLvl = 2;                            //set the gunshot threat level to shot detected
    }

    /**
     * Construct a gunshot class that is used to read and write to Firestore.
     * @param position - The location where the gunshot detection occurred.
     */
    public Gunshot(LatLng position){

        this.position = position;   //set the location of gunshot detection
        threatLvl = 2;              //set the gunshot threat level to shot detected
    }

    /**
     * Construct a gunshot class that is used to read and write to Firestore.
     * @param position - The location where the gunshot detection occurred.
     */
    public Gunshot(LatLng position, int threatLvl){

        this.position = position;   //set the location of gunshot detection
        this.threatLvl = threatLvl; //set the gunshot threat level to the desired level
    }

    /*** Setter Functions ***/
    public void setPosition(LatLng position){this.position = position;}     //set the gunshot detection location
    public void setThreatLvl(int threatLvl){this.threatLvl = threatLvl;}    //set the gunshot threat level

    /*** Getter Functions ***/
    public LatLng getPosition(){return position;}   //get the gunshot detection location
    public int getThreatLvl(){return threatLvl;}    //get the gunshot threat level
}