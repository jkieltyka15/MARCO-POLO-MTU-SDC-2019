package edu.mtu.polofirstresponder;

import com.google.android.gms.maps.model.Marker;

/**
 * Class for associating POLO Users with Google Maps markers.
 */
public class PoloMapMarker {

    private Marker marker;   //the actual map marker
    private PoloUser user;   //the POLO user associated with that marker

    public PoloMapMarker(){}    //required for firebase

    /**
     * Construct a PoloMapMarker Object for using with Google Maps.
     * @param user - The POLO user to be associated with the marker.
     * @param marker - The actual Google Maps marker.
     */
    public PoloMapMarker(PoloUser user, Marker marker){

        setUser(user);          //set the user for which the marker should associate with
        setMarker(marker);      //set the actual map marker
    }

    /*** Setter Methods ***/
    public void setUser(PoloUser user){this.user = user;}        //set the user for which the marker is associated with
    public void setMarker(Marker marker){this.marker = marker;}  //set the Google Maps marker

    /*** Getter Methods ***/
    public PoloUser getUser(){return user;}      //retrieve the user for which the marker is associated with
    public Marker getMarker(){return marker;}    //retrieve the Google Maps marker
}
