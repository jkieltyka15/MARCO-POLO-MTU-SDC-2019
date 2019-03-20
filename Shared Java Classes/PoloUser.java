import com.google.android.gms.maps.model.LatLng;

public class PoloUser{

    //the type of account associated with the user
    public static final int CIVILIAN = 0;
    public static final int FIRST_RESPONDER = 1;
    int type;

    String userID;      //the id of the current user
    LatLng position;    //the user's current position
    int gunshot;        //the status of if a gunshot is heard (0 = no gunshot)

    /**
     * Construct a PoloUser object that can be written to Firebase.
     * @param type - The type of account to associate with the user.
     * @param userID - The username of the user (anonymous ID or email ID)
     */
    public PoloUser(int type, String userID){

        this.type = type;       //set the account type
        this.userID = userID;   //set the userID

        //set default location and gunshot values
        position = new LatLng(0,0);
        gunshot = 0;
    }

    /**
     * Construct a PoloUser object that can be written to Firebase.
     * @param type - The type of account to associate with the user.
     * @param userID - The username of the user (anonymous ID or email ID)
     * @param position - The current position of the user.
     */
    public PoloUser(int type, String userID, LatLng position){

        this.type = type;           //set the account type
        this.userID = userID;       //set the userID
        this.position = position;   //set the user's current position

        gunshot = 0;    //set default gunshot value (no gunshot detected)
    }

    /*** Setter Functions ***/
    public void setType(int type){this.type = type;}                        //set the account type to associate with the user
    public void setUserID(String userID){this.userID = userID;}             //set the id for the user (anonymous or email)
    public void setPosition(LatLng position){this.position = position;}     //set the user's position
    public void setGunshot(int gunshot){this.gunshot = gunshot;}            //set the gunshot status

    /*** Getter Functions ***/
    public int getType(){return type;}              //return the account type to associate with the user
    public String getUserID(){return userID;}       //return the user id (anonymous or email)
    public LatLng getPosition(){return position;}   //return the user's position
    public int getGunshot(){return gunshot;}        //return the gunshot status (0 = no gunshot)
}
