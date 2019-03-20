package edu.mtu.polofirstresponder;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    //lists for getting data from Firebase
    ArrayList<PoloUser> firstResponders;
    ArrayList<PoloUser> civilians;
    ArrayList<PoloUser> marcos;

    //map data members
    private GoogleMap mGoogleMap; //the situation overlook map
    private MapView mMapView;

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

        //initialize the Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //setup listener for firebase real-time database changes for First Responders
        database.getReference("FirstResponder").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                firstResponders = dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<PoloUser>>(){});

                /*** Update Map Markers ***/
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        //setup listener for firebase real-time database changes for Civilians
        database.getReference("Civilians").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                civilians = dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<PoloUser>>(){});

                /*** Update Map Markers ***/
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
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
