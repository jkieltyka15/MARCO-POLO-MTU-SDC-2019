package edu.mtu.polocivilian;

<<<<<<< HEAD
import android.location.Location;


import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;

public class Gunshot {
    private LatLng position;    //the location of where the gunshot was detected
    private int threatLvl;  //the amount of time that has passed since shot detected(2 = 0 to 1 minute; 1 = 1 to 5 minutes; 0 > 5 minutes)
    private Timestamp timestamp; //time gunshot took place
=======
import com.google.android.gms.maps.model.LatLng;

public class Gunshot {
    private LatLng position;    //the location of where the gunshot was detected
    private int threatLvl;      //the amount of time that has passed since shot detected(2 = 0 to 1 minute; 1 = 1 to 5 minutes; 0 > 5 minutes)

>>>>>>> parent of a85d00a... MAJOR UPDATE
    /**
     * Construct a gunshot class that is used to read and write to Firestore.
     */
    public Gunshot(){

<<<<<<< HEAD
        this.position = new LatLng(MainActivity.getInstance().getLocation().getLatitude(),MainActivity.getInstance().getLocation().getLongitude());   //set the location of gunshot detection
        threatLvl = 2; //set the gunshot threat level to shot detected
        timestamp = Timestamp.now();
=======
        this.position = new LatLng(0,0);   //set the location of gunshot detection
        threatLvl = 2;                            //set the gunshot threat level to shot detected
>>>>>>> parent of a85d00a... MAJOR UPDATE
    }

    /**
     * Construct a gunshot class that is used to read and write to Firestore.
     * @param position - The location where the gunshot detection occurred.
     */
<<<<<<< HEAD
    public Gunshot(LatLng  position){
=======
    public Gunshot(LatLng position){
>>>>>>> parent of a85d00a... MAJOR UPDATE

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

<<<<<<< HEAD
    public Gunshot(LatLng  position, int threatLvl,Timestamp timestamp){

        this.timestamp = timestamp; //set time of gunshot
        this.position = position;   //set the location of gunshot detection
        this.threatLvl = threatLvl; //set the gunshot threat level to the desired level
    }

=======
>>>>>>> parent of a85d00a... MAJOR UPDATE
    /*** Setter Functions ***/
    public void setPosition(LatLng position){this.position = position;}     //set the gunshot detection location
    public void setThreatLvl(int threatLvl){this.threatLvl = threatLvl;}    //set the gunshot threat level

    /*** Getter Functions ***/
    public LatLng getPosition(){return position;}   //get the gunshot detection location
    public int getThreatLvl(){return threatLvl;}    //get the gunshot threat level
}
