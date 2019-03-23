package edu.mtu.polofirstresponder;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    //map data members
    private GoogleMap mGoogleMap; //the situation overlook map
    private Map<String, Marker> markers;    //keep track of google markers

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

        //initialize the map marker arraylist
        markers = new HashMap<String, Marker>();

        //initialize the Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        /*
         * Resource: https://firebase.google.com/docs/firestore/query-data/listen
         */
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

                                        //gunshot was recently detected
                                        case 1:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(tmp.getPosition())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
                                            break;

                                        //gunshot has been detected
                                        case 2:
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
                            Log.w(TAG, "POJO Conversion failed.", e);
                        }
                    }
                });

        //monitor all changes for civilians
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

                                        //gunshot was recently detected
                                        case 1:
                                            markers.put(doc.getId(), mGoogleMap.addMarker(new MarkerOptions()
                                                    .position(tmp.getPosition())
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))));
                                            break;

                                        //gunshot has been detected
                                        case 2:
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
                            Log.w(TAG, "POJO Conversion failed.", e);
                        }
                    }
                });

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

        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);     //set the view to satellite map

        //check to see if location service is currently permitted
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            googleMap.setMyLocationEnabled(true);                                 //show user location
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
