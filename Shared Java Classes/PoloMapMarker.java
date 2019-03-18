import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Class for associating POLO Users with Google Maps markers.
 */
public class PoloMapMarker {

    private MarkerOptions marker;   //the actual map marker
    private PoloUser user;          //the POLO user associated with that marker


    /**
     * Construct a PoloMapMarker Object for using with Google Maps.
     * @param user - The POLO user to be associated with the marker.
     * @param marker - The actual Google Maps marker.
     */
    public PoloMapMarker(PoloUser user, MarkerOptions marker){

        setUser(user);          //set the user for which the marker should associate with
        setMarker(marker);      //set the actual map marker
    }

    /*** Setter Methods ***/
    public void setUser(PoloUser user){this.user = user;}               //set the user for which the marker is associated with
    public void setMarker(MarkerOptions marker){this.marker = marker;}  //set the Google Maps marker

    /*** Getter Methods ***/
    public PoloUser getUser(){return user;}             //retrieve the user for which the marker is associated with
    public MarkerOptions getMarker(){return marker;}    //retrieve the Google Maps marker
}
