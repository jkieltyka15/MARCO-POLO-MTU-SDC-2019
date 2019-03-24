package edu.mtu.polofirstresponder;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.HashMap;
import java.util.Map;

import static android.location.LocationManager.GPS_PROVIDER;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PermissionManager permissionManager;            //used for checking permissions
    private FirebaseAuth mAuth;                             //Firebase authenticator
    private static final String TAG = "Navigation";         //tag for logfile

    private PoloUser currentUser;               //current Firebase user

    //timers
    CountDownTimer oneMinute;       //countdown timer for one minute
    CountDownTimer fiveMinutes;     //countdown timer for five minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        permissionManager = new PermissionManager() {};
        permissionManager.checkAndRequestPermissions(this);

        //if the user is currently not signed in, go to the sign in page
        if ( FirebaseAuth.getInstance().getCurrentUser() == null ) {
            this.startActivity(new Intent( NavigationActivity.this, LoginActivity.class ));
            this.finish();
        }

        //Initialize Firebase and check to see the user's status
        mAuth = FirebaseAuth.getInstance();
        new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //current user is signed in
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }

                //user is currently not signed in
                else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    mAuth.signOut();                                                                        //sign out the Firebase user
                    startActivity(new Intent(NavigationActivity.this, LoginActivity.class));   //start the login activity
                }
            }
        };

        //default view is map
        FragmentManager fragmentManager = getSupportFragmentManager();     //used to determine what fragment should be displayed
        setTitle("Map");                                                   //change the title to map
        fragmentManager.beginTransaction()                                 //change view to map
                .replace(R.id.navBackground, new MapFragment())
                .commit();


        //check to see if location service is currently permitted
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED
                && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED) {

            //get user's current location
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);



            //set a listener to always get the updated location
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {

                boolean update = true;

                /**
                 * Update the location of the current user in Firestore
                 * @param location - The user's current location
                 */
                @Override
                public void onLocationChanged(Location location) {

                    if(update) {
                        //Update the location for PoloUser
                        currentUser.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                        Map<String, Object> position = new HashMap<>();
                        position.put("latitude", location.getLatitude());
                        position.put("longitude", location.getLongitude());

                        //Update the location in the Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance(); //initialize the Firestore
                        db.collection("FirstResponders").document(currentUser.getUserID()).update("position", position);

                        update = false;

                        //only update the location every ten seconds
                        new CountDownTimer(10000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                /* do nothing */
                            }

                            public void onFinish() {
                                update = true;
                            }
                        }.start();
                    }

                }
                @Override
                public void onProviderDisabled(String provider) {
                    // TODO Auto-generated method stub
                }
                @Override
                public void onProviderEnabled(String provider) {
                    // TODO Auto-generated method stub
                }
                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                    // TODO Auto-generated method stub
                }
            });

            //create the current PoloUser
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            currentUser = new PoloUser(PoloUser.FIRST_RESPONDER, mAuth.getCurrentUser().getUid(),
                    new LatLng(location.getLatitude(), location.getLongitude()));

            //write to the Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("FirstResponders").document(currentUser.getUserID()).set(currentUser);

            //Update gunshot status to gunshot heard 1 minute ago in Firestore
            oneMinute = new CountDownTimer(60000, 1000) {

                public void onTick(long millisUntilFinished) {
                    /* do nothing */
                }

                public void onFinish() {

                    //Update the gunshot status to shot detected in the Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    currentUser.setGunshot(2);
                    db.collection("FirstResponders").document(currentUser.getUserID()).update("gunshot", currentUser.getGunshot());
                }
            };

            //Update gunshot status to gunshot heard 5 minutes ago in Firestore
            fiveMinutes = new CountDownTimer(300000, 1000) {

                public void onTick(long millisUntilFinished) {
                    /* do nothing */
                }

                public void onFinish() {

                    //Update the gunshot status to shot detected in the Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    currentUser.setGunshot(1);
                    db.collection("FirstResponders").document(currentUser.getUserID()).update("gunshot", currentUser.getGunshot());
                }
            };
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionManager.checkResult(requestCode,permissions,grantResults);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        FragmentManager fragmentManager = getSupportFragmentManager(); //used to determine what fragment should be displayed

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //go to the active shooter map fragment
        if (id == R.id.nav_map) {
            setTitle("Map");                                                                        //change the title to map
            fragmentManager.beginTransaction()                                                      //change view to map
                    .replace(R.id.navBackground, new MapFragment())
                    .commit();
        }

        //simulate a gunshot event
        else if(id == R.id.nav_gunshot){

            //Update the gunshot status to shot detected in the Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            currentUser.setGunshot(3);
            db.collection("FirstResponders").document(currentUser.getUserID()).update("gunshot", currentUser.getGunshot());

            //display toast notification that gunshot has been detected
            Context appContext = getApplicationContext();
            Toast gsMessage = Toast.makeText(appContext, "Gunshot Detected", Toast. LENGTH_SHORT);
            gsMessage.setGravity(Gravity.TOP, 0, 0);
            gsMessage.show();

            //reset the timers
            oneMinute.cancel();     //stop the one minute timer
            fiveMinutes.cancel();   //stop the five minute timer
            oneMinute.start();      //restart the one minute timer
            fiveMinutes.start();    //restart the five minute timer
        }

        //logout the current user
        else if (id == R.id.nav_logout) {
            setTitle("Log Out");                                                                    //change the title to log out
            mAuth.signOut();                                                                        //sign out the Firebase user
            startActivity(new Intent(NavigationActivity.this, LoginActivity.class));   //start the login activity
            this.finish();                                                                          //end the current activity
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
