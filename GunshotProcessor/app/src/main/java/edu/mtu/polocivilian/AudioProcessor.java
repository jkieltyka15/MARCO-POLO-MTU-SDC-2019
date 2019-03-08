package edu.mtu.polocivilian;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
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

        byte[] byte_buffer = Utils.short_to_byte(buffer);
        String audio_blob = Base64.encodeToString(byte_buffer, Base64.NO_WRAP);


        GunshotDocument gunshot = new GunshotDocument(
                timestamp, sample_rate, is_gunshot,
                location, audio_blob);

        MainActivity.getInstance().updateUI(gunshot);

        FirebaseFirestore.getInstance()
                .collection("processedAudio")
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
                });
    }
}



