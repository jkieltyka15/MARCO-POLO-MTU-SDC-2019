package edu.mtu.polocivilian;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


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

//below is the new stuff we added. we can delete the gunshot document after we get everything presented.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Civilians").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(new LatLng(location.getLatitude(),location.getLongitude()));

        if (is_gunshot) {

            db.collection("Gunshots").document().set(new Gunshot(new LatLng(location.getLatitude(), location.getLongitude())));

        }
    }
}



