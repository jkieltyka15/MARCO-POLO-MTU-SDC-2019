package edu.mtu.polofirstresponder;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.location.LocationManager.GPS_PROVIDER;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    //map data members
    private GoogleMap mGoogleMap;               //the situation overlook map
    private static Map<String, Marker> markers;  //keep track of google markers

    //view
    private View mView;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return mView = inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){

        super.onViewCreated(view, savedInstanceState);

        //Google Maps view
        MapView mMapView;

        //setup and display the map
        mMapView = mView.findViewById(R.id.map);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;                                 //initialize the Google map
        LocationManager locationManager;                        //used for getting user's current location
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);     //set the view to satellite map

        //initialize the map marker arraylist
        markers = new HashMap<>();

        //initialize the Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //retrieve all civilian users
        db.collection("Civilians")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                //Cycle through all documents that were changed and update the map
                                for (QueryDocumentSnapshot doc : task.getResult()) {

                                    //check to see if the doc is available
                                    if (doc != null) {

                                        //create the PoloUser that is associated with this account
                                        Map<String, Double> position = (Map<String, Double>) doc.get("position");
                                        PoloUser tmp = new PoloUser(doc.getLong("type").intValue(),
                                                doc.get("userID", String.class),
                                                new LatLng(position.get("latitude"), position.get("longitude")));
                                        tmp.setGunshot(doc.getLong("gunshot").intValue());

                                        //place the Google Maps marker and add it to the HashMap
                                        switch (tmp.getGunshot()) {

                                            //no gunshot detected
                                            default:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
                                                break;

                                            //gunshot was detected five minutes ago
                                            case 1:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
                                                break;

                                            //gunshot was detected 1 minute ago
                                            case 2:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
                                                break;

                                            //gunshot has been detected
                                            case 3:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
                                                break;
                                        }
                                    }
                                }
                            }
                            //null pointer exception received
                            catch(Exception nullRef){
                                Log.w(TAG, "POJO Conversion failed.", nullRef);
                            }
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //retrieve all First Responders
        db.collection("FirstResponders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                //Cycle through all documents that were changed and update the map
                                for (QueryDocumentSnapshot doc : task.getResult()) {

                                    //check to see if the doc is available
                                    if (doc != null) {

                                        //do not display the current user as a marker on the map
                                        if (!doc.get("userID", String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                                            //create the PoloUser that is associated with this account
                                            Map<String, Double> position = (Map<String, Double>) doc.get("position");
                                            PoloUser tmp = new PoloUser(doc.getLong("type").intValue(),
                                                    doc.get("userID", String.class),
                                                    new LatLng(position.get("latitude"), position.get("longitude")));
                                            tmp.setGunshot(doc.getLong("gunshot").intValue());

                                            //place the Google Maps marker and add it to the HashMap
                                            switch (tmp.getGunshot()) {

                                                //no gunshot detected
                                                default:
                                                    markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                            .position(tmp.getPosition())
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
                                                    break;

                                                //gunshot was detected 5 minutes ago
                                                case 1:
                                                    markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                            .position(tmp.getPosition())
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))));
                                                    break;

                                                //gunshot was detected 1 minute ago
                                                case 2:
                                                    markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                            .position(tmp.getPosition())
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
                                                    break;

                                                //gunshot has been detected
                                                case 3:
                                                    markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                            .position(tmp.getPosition())
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                            //null pointer exception received
                            catch(Exception nullRef){
                                Log.w(TAG, "POJO Conversion failed.", nullRef);
                            }
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //retrieve all MARCOs
        db.collection("MARCOs")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                //Cycle through all documents that were changed and update the map
                                for (QueryDocumentSnapshot doc : task.getResult()) {

                                    //check to see if the doc is available
                                    if (doc != null) {

                                        //create the PoloUser that is associated with this account
                                        Map<String, Double> position = (Map<String, Double>) doc.get("position");
                                        Marco tmp = new Marco(doc.get("userID", String.class),
                                                new LatLng(position.get("latitude"), position.get("longitude")),
                                                doc.getLong("leftMotor").intValue(), doc.getLong("rightMotor").intValue(),
                                                doc.getLong("o2").intValue(), doc.getLong("mq2").intValue(),
                                                doc.getLong("mq5").intValue(), doc.getLong("mq7").intValue());
                                        tmp.setGunshot(doc.getLong("gunshot").intValue());

                                        if (markers.containsKey(doc.getId())) {
                                            markers.get(doc.getId()).remove();
                                        }

                                        //place the Google Maps marker and add it to the HashMap
                                        switch (tmp.getGunshot()) {

                                            //no gunshot detected
                                            default:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                                        .title(tmp.getUserID())
                                                        .snippet("Oxygen: " + Integer.toString(tmp.getO2()) + " Smoke: " + Integer.toString(tmp.getMq2())
                                                                + " Gas: " + Integer.toString(tmp.getMq5()) +  " C0: " + Integer.toString(tmp.getMq7()))));
                                                break;

                                            //gunshot was recently detected
                                            case 1:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                                        .title(tmp.getUserID())
                                                        .snippet("Oxygen: " + Integer.toString(tmp.getO2()) +  " Smoke: " + Integer.toString(tmp.getMq2())
                                                                + " Gas: " + Integer.toString(tmp.getMq5()) +  " C0: " + Integer.toString(tmp.getMq7()))));
                                                break;

                                            //gunshot has been detected
                                            case 2:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                                        .title(tmp.getUserID())
                                                        .snippet("Oxygen: " + Integer.toString(tmp.getO2()) +  " Smoke: " + Integer.toString(tmp.getMq2())
                                                                + " Gas: " + Integer.toString(tmp.getMq5()) +  " C0: " + Integer.toString(tmp.getMq7()))));
                                                break;
                                        }
                                    }
                                }
                            }
                            //null pointer exception received
                            catch(Exception nullRef){
                                Log.w(TAG, "POJO Conversion failed.", nullRef);
                            }
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //monitor all changes for civilians
        db.collection("Civilians")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {

                        //check the status of the listen
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        try {
                            //Cycle through all documents that were changed and update the map
                            for (QueryDocumentSnapshot doc : value) {

                                //check to see if the doc is available
                                if (doc != null) {

                                    //create the PoloUser that is associated with this account
                                    Map<String, Double> position = (Map<String, Double>) doc.get("position");
                                    PoloUser tmp = new PoloUser(doc.getLong("type").intValue(),
                                            doc.get("userID", String.class),
                                            new LatLng(position.get("latitude"), position.get("longitude")));
                                    tmp.setGunshot(doc.getLong("gunshot").intValue());

                                    if (markers.containsKey(doc.getId())) {
                                        markers.get(doc.getId()).remove();
                                    }

                                    //place the Google Maps marker and add it to the HashMap
                                    switch (tmp.getGunshot()) {

                                        //no gunshot detected
                                        default:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(tmp.getPosition())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
                                            break;

                                        //gunshot was detected five minutes ago
                                        case 1:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(tmp.getPosition())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
                                            break;

                                        //gunshot was detected 1 minute ago
                                        case 2:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(tmp.getPosition())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
                                            break;

                                        //gunshot has been detected
                                        case 3:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(tmp.getPosition())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
                                            break;
                                    }
                                }
                            }
                        }
                        //null pointer exception received
                        catch(Exception nullRef){
                            Log.w(TAG, "POJO Conversion failed.", nullRef);
                        }
                    }
                });

        //monitor all changes for first responders
        db.collection("FirstResponders")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {

                        //check the status of the listen
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        try {
                            //Cycle through all documents that were changed and update the map
                            for (QueryDocumentSnapshot doc : value) {

                                //check to see if the doc is available
                                if (doc != null) {

                                    //do not display the current user as a marker on the map
                                    if (!doc.get("userID", String.class).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                                        //create the PoloUser that is associated with this account
                                        Map<String, Double> position = (Map<String, Double>) doc.get("position");
                                        PoloUser tmp = new PoloUser(doc.getLong("type").intValue(),
                                                doc.get("userID", String.class),
                                                new LatLng(position.get("latitude"), position.get("longitude")));
                                        tmp.setGunshot(doc.getLong("gunshot").intValue());

                                        if (markers.containsKey(doc.getId())) {
                                            markers.get(doc.getId()).remove();
                                        }

                                        //place the Google Maps marker and add it to the HashMap
                                        switch (tmp.getGunshot()) {

                                            //no gunshot detected
                                            default:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
                                                break;

                                            //gunshot was detected 5 minutes ago
                                            case 1:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))));
                                                break;

                                            //gunshot was detected 1 minute ago
                                            case 2:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
                                                break;

                                            //gunshot has been detected
                                            case 3:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(tmp.getPosition())
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))));
                                                break;
                                        }
                                    }
                                }
                            }
                        }
                        //null pointer exception received
                        catch(Exception nullRef){
                            Log.w(TAG, "POJO Conversion failed.", nullRef);
                        }
                    }
                });

        //monitor all changes for MARCOs
        db.collection("MARCOs")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {

                        //check the status of the listen
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        try {
                            //Cycle through all documents that were changed and update the map
                            for (QueryDocumentSnapshot doc : value) {

                                //check to see if the doc is available
                                if (doc != null) {

                                    //create the PoloUser that is associated with this account
                                    Map<String, Double> position = (Map<String, Double>) doc.get("position");
                                    Marco tmp = new Marco(doc.get("userID", String.class),
                                            new LatLng(position.get("latitude"), position.get("longitude")),
                                            doc.getLong("leftMotor").intValue(), doc.getLong("rightMotor").intValue(),
                                            doc.getLong("o2").intValue(), doc.getLong("mq2").intValue(),
                                            doc.getLong("mq5").intValue(), doc.getLong("mq7").intValue());
                                    tmp.setGunshot(doc.getLong("gunshot").intValue());

                                    if (markers.containsKey(doc.getId())) {
                                        markers.get(doc.getId()).remove();
                                    }

                                    //place the Google Maps marker and add it to the HashMap
                                    switch (tmp.getGunshot()) {

                                        //no gunshot detected
                                        default:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(tmp.getPosition())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                                    .title(tmp.getUserID())
                                                    .snippet("Oxygen: " + Integer.toString(tmp.getO2()) + " Smoke: " + Integer.toString(tmp.getMq2())
                                                            + " Gas: " + Integer.toString(tmp.getMq5()) +  " C0: " + Integer.toString(tmp.getMq7()))));
                                            break;

                                        //gunshot was recently detected
                                        case 1:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(tmp.getPosition())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                                    .title(tmp.getUserID())
                                                    .snippet("Oxygen: " + Integer.toString(tmp.getO2()) +  " Smoke: " + Integer.toString(tmp.getMq2())
                                                            + " Gas: " + Integer.toString(tmp.getMq5()) +  " C0: " + Integer.toString(tmp.getMq7()))));
                                            break;

                                        //gunshot has been detected
                                        case 2:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(tmp.getPosition())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                                    .title(tmp.getUserID())
                                                    .snippet("Oxygen: " + Integer.toString(tmp.getO2()) +  " Smoke: " + Integer.toString(tmp.getMq2())
                                                            + " Gas: " + Integer.toString(tmp.getMq5()) +  " C0: " + Integer.toString(tmp.getMq7()))));
                                            break;
                                    }
                                }
                            }
                        }
                        //null pointer exception received
                        catch(Exception nullRef){
                            Log.w(TAG, "POJO Conversion failed.", nullRef);
                        }
                    }
                });


        //check to see if location service is currently permitted
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {

            //show user location
            googleMap.setMyLocationEnabled(true);

            //set the map to zoom in on the users location
            locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17);
            mGoogleMap.animateCamera(cameraUpdate);
        }

        //user has not permitted location, alert user to add location services permission
        else {
            Toast.makeText(getActivity(), "Please Enable Location Services", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkPermission(String permission)
    {
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
