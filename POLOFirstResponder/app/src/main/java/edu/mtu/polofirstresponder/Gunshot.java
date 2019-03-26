package edu.mtu.polofirstresponder;

import com.google.android.gms.maps.model.LatLng;

import java.security.Timestamp;
import java.util.Date;

public class Gunshot {

    private LatLng position;    //the location of where the gunshot was detected
    private int threatLvl;      //the amount of time that has passed since shot detected(2 = 0 to 1 minute; 1 = 1 to 5 minutes; 0 > 5 minutes)

    public Gunshot(){
        Timestamp th = new Timestamp();
        if(Timestamp th.getTimestamp().getTime() >= 5)
    }

}