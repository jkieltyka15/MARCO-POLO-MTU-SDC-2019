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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        //Google Maps view
        MapView mMapView;

        //setup and display the map
        mMapView = mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;                                         //initialize the Google map
        LocationManager locationManager;                                //used for getting user's current location
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);                //set the view to show satellite and indoor maps
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(true);    //allow user to pick the floor for an indoor map
        googleMap.getUiSettings().setCompassEnabled(true);              //show the compass to the user

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

                            //Cycle through all documents that were changed and update the map
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                try {

                                    //check to see if the doc is available
                                    if (doc != null) {

                                        //remove the marker from the map
                                        if (markers.containsKey(doc.getId())) {
                                            markers.get(doc.getId()).remove();
                                        }

                                        //add the marker to the map
                                        markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(doc.getDouble("latitude"), doc.getDouble("longitude")))
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                .zIndex(0)));
                                    }
                                }
                                //null pointer exception received
                                catch (Exception nullRef) {
                                    Log.w(TAG, "POJO Conversion failed.", nullRef);
                                }
                            }

                        } else {
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

                            //Cycle through all documents that were changed and update the map
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                try {

                                    //check to see if the doc is available
                                    if (doc != null) {

                                        //only add the marker if it is not the current user
                                        if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(doc.getId())) {

                                            //remove the marker from the map
                                            if (markers.containsKey(doc.getId())) {
                                                markers.get(doc.getId()).remove();
                                            }

                                            //add the marker to the map
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(doc.getDouble("latitude"), doc.getDouble("longitude")))
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
                                        }
                                    }
                                }
                                //null pointer exception received
                                catch (Exception nullRef) {
                                    Log.w(TAG, "POJO Conversion failed.", nullRef);
                                }
                            }
                        } else {
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

                            //Cycle through all documents that were changed and update the map
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                try {

                                    //check to see if the doc is available
                                    if (doc != null) {

                                        //get the position of the MARCO device
                                        Map<String, Double> position = (Map<String, Double>) doc.get("position");

                                        //check to see if the marker has already been displayed
                                        if (markers.containsKey(doc.getId())) {
                                            markers.get(doc.getId()).remove();
                                        }

                                        //place the marker on the map
                                        markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(position.get("latitude"), position.get("longitude")))
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                                .title("MARCO")
                                                .snippet("Temp: " + Double.toString(doc.getLong("temp").doubleValue())
                                                        + " O2: " + Double.toString(doc.getLong("o2").doubleValue())
                                                        + " Smoke: " + Double.toString(doc.getLong("mq2").doubleValue())
                                                        + " Gas: " + Double.toString(doc.getLong("mq5").doubleValue())
                                                        + " C0: " + Double.toString(doc.getLong("mq7").doubleValue()))));
                                    }
                                }
                                //null pointer exception received
                                catch (Exception nullRef) {
                                    Log.w(TAG, "POJO Conversion failed.", nullRef);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //retrieve all Gunshots
        db.collection("Gunshots")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            //Cycle through all documents that were changed and update the map
                            for (QueryDocumentSnapshot doc : task.getResult()) {

                                //check to see if the doc is available
                                if (doc != null) {
                                    try {

                                        //get the position of the MARCO device
                                        Map<String, Double> position = (Map<String, Double>) doc.get("position");

                                        //check to see if the marker has already been displayed
                                        if (markers.containsKey(doc.getId())) {
                                            markers.get(doc.getId()).remove();
                                        }

                                        //place the marker on the map
                                        switch (doc.getLong("threatLvl").intValue()) {

                                            //Gunshot detected over 5 minutes ago
                                            default:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(position.get("latitude"), position.get("longitude")))
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
                                                break;

                                            //Gunshot detected 1 to 5 minutes ago
                                            case 1:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(position.get("latitude"), position.get("longitude")))
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
                                                break;

                                            //Gunshot detected 0 to 1 minute ago
                                            case 2:
                                                markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(position.get("latitude"), position.get("longitude")))
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                                        .zIndex(1000000 + 1)));
                                                break;
                                        }
                                    }
                                    //null pointer exception received
                                    catch (Exception nullRef) {
                                        Log.w(TAG, "POJO Conversion failed.", nullRef);
                                    }
                                }
                            }
                        } else {
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

                        //Cycle through all documents that were changed and update the map
                        for (QueryDocumentSnapshot doc : value) {
                            try {

                                //check to see if the doc is available
                                if (doc != null) {

                                    //remove the marker from the map
                                    if (markers.containsKey(doc.getId())) {
                                        markers.get(doc.getId()).remove();
                                    }

                                    //add the marker to the map
                                    markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(doc.getDouble("latitude"), doc.getDouble("longitude")))
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                            .zIndex(0)));
                                }
                            }
                            //null pointer exception received
                            catch (Exception nullRef) {
                                Log.w(TAG, "POJO Conversion failed.", nullRef);
                            }
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

                        //Cycle through all documents that were changed and update the map
                        for (QueryDocumentSnapshot doc : value) {

                            //check to see if the doc is available
                            if (doc != null) {
                                try {

                                    //only add the marker if it is not the current user
                                    if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(doc.getId())) {

                                        //remove the marker from the map
                                        if (markers.containsKey(doc.getId())) {
                                            markers.get(doc.getId()).remove();
                                        }

                                        //add the marker to the map
                                        markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(doc.getDouble("latitude"), doc.getDouble("longitude")))
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));
                                    }
                                }
                                //null pointer exception received
                                catch (Exception nullRef) {
                                    Log.w(TAG, "POJO Conversion failed.", nullRef);
                                }
                            }
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

                        //Cycle through all documents that were changed and update the map
                        for (QueryDocumentSnapshot doc : value) {

                            //check to see if the doc is available
                            if (doc != null) {
                                try {

                                    //create the PoloUser that is associated with this account
                                    Map<String, Double> position = (Map<String, Double>) doc.get("position");

                                    //check to see if the info for MARCO was previously shown
                                    boolean isInfoWindowShown = false;
                                    if (markers.containsKey(doc.getId())) {
                                        isInfoWindowShown = markers.get(doc.getId()).isInfoWindowShown();   //check to see if MARCO info was being displayed
                                        markers.get(doc.getId()).remove();                                  //remove the marker from the map
                                    }

                                    //place the marker on the map
                                    markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(position.get("latitude"), position.get("longitude")))
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                            .title("MARCO")
                                            .snippet("Temp: " + Double.toString(doc.getLong("temp").doubleValue())
                                                    + " O2: " + Double.toString(doc.getLong("o2").doubleValue())
                                                    + " Smoke: " + Double.toString(doc.getLong("mq2").doubleValue())
                                                    + " Gas: " + Double.toString(doc.getLong("mq5").doubleValue())
                                                    + " C0: " + Double.toString(doc.getLong("mq7").doubleValue()))));

                                    //display MARCOs info if it was shown on the previous version of the marker
                                    if (isInfoWindowShown) {
                                        markers.get(doc.getId()).showInfoWindow();
                                    }

                                }
                                //null pointer exception received
                                catch (Exception nullRef) {
                                    Log.w(TAG, "POJO Conversion failed.", nullRef);
                                }
                            }
                        }
                    }
                });

        //monitor all changes for Gunshots
        db.collection("Gunshots")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {

                        //check the status of the listen
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        //Cycle through all documents that were changed and update the map
                        for (QueryDocumentSnapshot doc : value) {

                            //check to see if the doc is available
                            if (doc != null) {
                                try {
                                    //get the position of the MARCO device
                                    Map<String, Double> position = (Map<String, Double>) doc.get("position");

                                    //check to see if the marker has already been displayed
                                    if (markers.containsKey(doc.getId())) {
                                        markers.get(doc.getId()).remove();
                                    }

                                    //place the marker on the map
                                    switch (doc.getLong("threatLvl").intValue()) {

                                        //Gunshot detected over 5 minutes ago
                                        default:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(position.get("latitude"), position.get("longitude")))
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
                                            break;

                                        //Gunshot detected 1 to 5 minutes ago
                                        case 1:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(position.get("latitude"), position.get("longitude")))
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
                                            break;

                                        //Gunshot detected 0 to 1 minute ago
                                        case 2:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(position.get("latitude"), position.get("longitude")))
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                                    .zIndex(1000000 + 1)));
                                            break;
                                    }
                                }
                                //null pointer exception received
                                catch (Exception nullRef) {
                                    Log.w(TAG, "POJO Conversion failed.", nullRef);
                                }
                            }
                        }
                    }
                });

        //check to see if location service is currently permitted
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {

            //show user location
            googleMap.setMyLocationEnabled(true);

            //set the map to zoom in on the users location
            try {
                locationManager = (LocationManager) this.getContext().getSystemService(Context.LOCATION_SERVICE);
                Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);
                CameraUpdate cameraUpdate = CameraUpdateFactory
                        .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17);
                mGoogleMap.animateCamera(cameraUpdate);
            } catch (Exception nullRef) {
                /* do nothing */
            }
        }

        //user has not permitted location, alert user to add location services permission
        else {
            Toast.makeText(getActivity(), "Please Enable Location Services", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Check to see if the requested permission has been granted.
     *
     * @param permission - The permission to be checked.
     * @return If the permission was granted.
     */
    private boolean checkPermission(String permission) {
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
}
