/**
 * Generic design for the user structure for sending data regarding a POLO user to the firebase. As
 * well as providing a structure for receiving data regarding a POLO user from the firebase.
 */
public class PoloUser {

    //user information
    public static final char responder = 0; //constant for responder user type
    public static final char civilian = 1;  //constant for civilian user type
    private char type;                      //user is either responder or civilian
    private String id;                      //users ID

    //user location information
    private double lat;
    private double lon;

    //gunshot information
    private char gunshot = 0;   //placeholder for if a gunshot was detected and how long ago it was

    /**
     * Construct a PoloUser object.
     * @param type - The type of account (responder or civilian).
     * @param id - The username of the account (anonymous or email).
     */
    public PoloUser(char type, String id) {

        setType(type);          //set the user type
        setId(id);              //set the username/id
        setLat(0);              //set default latitude value
        setLon(0);              //set default longitude value
        setGunshot((char)0);    //set gunshot to no triggered
    }

    /**
     *
     * @param type - The type of account (responder or civilian).
     * @param id - The username of the account (anonymous or email).
     * @param lat - The latitude of the user.
     * @param lon - The longitude of the user.
     */
    public PoloUser(char type, String id, double lat, double lon) {

        setType(type);          //set the user type
        setId(id);              //set the username/id
        setLat(lat);            //set default latitude value
        setLon(lon);            //set default longitude value
        setGunshot((char)0);    //set gunshot to no triggered
    }

    /*** Setter Methods ***/
    public void setType(char type){this.type = type;}               //set user type
    public void setId(String id){this.id = id;}                     //set user id
    public void setLat(double lat){this.lat = lat;}                 //set user's latitude
    public void setLon(double lon){this.lon = lon;}                 //set user's longitude
    public void setGunshot(char gunshot){this.gunshot = gunshot;}   //get the gunshot status

    /*** Getter Methods ***/
    public char getType(){return type;}             //get the user type
    public String getId(){return id;}               //get the user id
    public double getLat(){return lat;}             //get the user's latitude
    public double getLon(){return lon;}             //get the user's longitude
    public char getGunshot(){return gunshot;}       //get the user's gunshot status
}