package edu.mtu.polocivilian;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;


public class AudioProcessor implements Runnable {
    private short[] buffer;
    private int sample_rate;

    public AudioProcessor(int sample_rate, @NonNull short[] buffer) {
        this.sample_rate = sample_rate;
        this.buffer = buffer;

    }

    @Override
    public void run() {
        Timestamp timestamp = Timestamp.now();
        boolean is_gunshot = MainActivity.getInstance().shouldOverride() ? MainActivity.getInstance().getOverrideValue() : YourClass.getInstance().run(sample_rate, buffer);
        Location loc = MainActivity.getInstance().getLocation();
        GeoPoint location = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        //needed for polo user class only
        LatLng latlng = new LatLng(loc.getLatitude(),loc.getLongitude());

        byte[] byte_buffer = Utils.short_to_byte(buffer);
        String audio_blob = Base64.encodeToString(byte_buffer, Base64.NO_WRAP);

        //The below code is a duplicate of shit but I don't want to break anything this close to competition



//   ***BELOW SHOULD BE DELETED AFTER TESTING NEW POLOUSER CLASS / FIREBASE UPLOAD***
//This creates a gunshot document that we dont need but the user iterface is updated with the values
        //in the gunshot variable below. In order to update the UI we need to make sure our new variable
        //has the necessary values and the appropriate pathway
        GunshotDocument gunshot = new GunshotDocument(
                timestamp, sample_rate, is_gunshot,
                location, audio_blob);

        MainActivity.getInstance().updateUI(gunshot);

        //using the polouser class to set an ID and type for the phone
        //we have duplicates of the position and are spending a lot of time getting them in the correct data type
        PoloUser polouser = new PoloUser(0,"Anonymous",latlng);
        //results from GS processor return boolean, we want to add a 3 option based on the timestamp
        //for now we will use this statement to deal with the boolean
        if (!is_gunshot) {
            polouser.setGunshot(0);
        }
        else {
            polouser.setGunshot(1);
            FirebaseFirestore db= FirebaseFirestore.getInstance();
            Map<String, Object> userResult = new HashMap<>();
            userResult.put("Gunshot Value", polouser.gunshot);

            db.collection("Gunshot").document()
                    .set(polouser.gunshot)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FIREBASE_STORAGE", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("FIREBASE_STORAGE", "Error writing document", e);
                        }
                    });

        }



        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userResult = new HashMap<>();
            userResult.put("Type", polouser.type);
            userResult.put("ID", polouser.userID);
            userResult.put("Gunshot Value", polouser.gunshot);

            db.collection("Civilians").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .set(polouser.position)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("FIREBASE_STORAGE", "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("FIREBASE_STORAGE", "Error writing document", e);
                        }
                    });





       /* FirebaseFirestore.getInstance()
                .collection("Civilian")
                .add(gunshot.toMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("FIREBASE_STORAGE", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FIREBASE_STORAGE", "Error adding document", e);
                    }
                });*/
    }
}



