package edu.mtu.afrlcompetitionbutton;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static MainActivity INSTANCE;

    private PermissionManager permissionManager;            //used for checking permissions

    private FirebaseAuth mAuth;

    //assigning values for debugging
    private Location location = null;
    private double latitude = -600;
    private double longitude = -600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        INSTANCE = this;

        permissionManager = new PermissionManager() {
        };
        permissionManager.checkAndRequestPermissions(this);


        System.out.print("stuck after on create");
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously();
        System.out.print("Instance from Firebase");
        //Check to see if location service is currently permitted



        setContentView(R.layout.activity_main);
        final Button button = findViewById(R.id.button_id);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Gunshot gunshot = new Gunshot(new LatLng(latitude, longitude));
                    //Update the location in the Firestore
                    try {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Gunshots").document().set(gunshot);
                    } catch (Exception nullRef) {
                        /* do nothing */
                    }
                }

            }
        });

        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED
                && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED) {
            System.out.print("Permission to get FINE Location");
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);  //used to get the combined network and gps data for better accuracy
            LocationRequest mLocationRequest = new LocationRequest();                                                         //initialize the location request to be used with the FusedLocationProviderClient

            // update location every 1 second
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);   //set the location service to the highest accuracy possible

            //called when the FusedLocationProviderClient has a location update
            LocationCallback mLocationCallback = new LocationCallback() {

                //location update received
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    List<Location> locationList = locationResult.getLocations();
                    if (locationList.size() > 0) {

                        //The last location in the list is the newest
                        location = locationList.get(locationList.size() - 1);
                        Map<String, Object> position = new HashMap<>();
                        position.put("latitude", latitude = location.getLatitude());
                        position.put("longitude", longitude = location.getLongitude());

                        //Update the location in the Firestore
                        try {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Civilians").document(mAuth.getCurrentUser().getUid()).set(position);
                        } catch (
                                Exception nullRef) {
                            /* do nothing */
                        }
                    }
                }
            };
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());    //get updates to the user's current location
        }
    }
    protected static MainActivity getInstance() {return INSTANCE; }
    public Location getLocation() {
        return location;
    }
}



