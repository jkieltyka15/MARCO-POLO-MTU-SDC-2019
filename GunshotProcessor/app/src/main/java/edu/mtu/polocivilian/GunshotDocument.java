package edu.mtu.polocivilian;

import com.google.firebase.Timestamp;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class GunshotDocument {

    public Timestamp timestamp;
    public int sample_rate;
    public boolean is_gunshot;
    public String audio;
    public GeoPoint location;

    public GunshotDocument(Timestamp timestamp,
                           int sample_rate,
                           boolean is_gunshot,
                           GeoPoint location,
                           String audio) {
        this.timestamp = timestamp;
        this.sample_rate = sample_rate;
        this.is_gunshot = is_gunshot;
        this.location = location;
        //this.audio = audio; *do not want to send this audio anymore
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("timestamp", timestamp);
        result.put("sample_rate", sample_rate);
        result.put("is_gunshot", is_gunshot);
        result.put("location", location);
        //result.put("audio", audio);

        return result;
    }
}