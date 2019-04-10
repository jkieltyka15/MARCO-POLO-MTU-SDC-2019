package edu.mtu.polocivilian;

import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class AudioCollector {
    private static final AudioCollector INSTANCE = new AudioCollector(
            Constants.RECORDER_SAMPLE_DURATION_MS,
            Constants.RECORDER_SAMPLERATE);

    private int recording_ms;
    private int sample_rate;

    private AudioRecord recorder = null;
    private int samples_per_processing_buffer;
    private boolean is_recording;

    private short[] last_recording;

    private double latitude;
    private double longitude;
    private int threatLvl;

    public static AudioCollector getInstance() {
        return INSTANCE;
    }


    private final AudioRecord.OnRecordPositionUpdateListener listener = new AudioRecord.OnRecordPositionUpdateListener() {

        @Override
        public void onMarkerReached(AudioRecord recorder) {
            Log.e("AUDIO_RECORDER", "INVALID STATE REACHED, THIS IS NOT CONFIGURED TO HIT MARKERS");
        }

        @Override
        public void onPeriodicNotification(AudioRecord recorder) {
            final short[] buffer = new short[samples_per_processing_buffer+4];
            recorder.read(buffer, 0, samples_per_processing_buffer);

            last_recording = buffer;

            if (MainActivity.getInstance().shouldOverride()) {

                if (MainActivity.getInstance().getOverrideValue()) {
                    latitude = MainActivity.getInstance().getLocation().getLatitude();
                    longitude = MainActivity.getInstance().getLocation().getLongitude();
                    //Update the location in the Firestore
                    try {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("Gunshots").document().set(new Gunshot(new LatLng(latitude, longitude)));
                    } catch (Exception nullRef) {
                        /* do nothing */
                    }
                }
            }
            else {
                //Run the last audio recording through FFT via Your Class
                YourClass.getInstance().run(last_recording);
            }
        }
    };

    private AudioCollector(int recording_ms, int sample_rate) {
        // 2x because we're storing as bytes
        this.recording_ms = recording_ms;
        this.sample_rate = sample_rate;
        this.samples_per_processing_buffer = sample_rate * recording_ms / 1000;
    }

    public void startRecording() {
        recorder = new AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(Constants.RECORDER_AUDIO_ENCODING)
                        .setChannelMask(Constants.RECORDER_CHANNELS)
                        .setSampleRate(sample_rate)
                        .build())
                .setBufferSizeInBytes(samples_per_processing_buffer * 3)
                .build();

        recorder.setPositionNotificationPeriod(samples_per_processing_buffer);
        recorder.setRecordPositionUpdateListener(listener);

        recorder.startRecording();
        is_recording = true;
    }

    public void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            is_recording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    public boolean isRecording() {
        return is_recording;
    }

    public int getRecordingMs() {
        return recording_ms;
    }

    public int getSampleRate() {
        return sample_rate;
    }

    public int getSamplesPerProcessingBuffer() {
        return samples_per_processing_buffer;
    }

    public void setSampleRate(int sample_rate) {
        boolean was_recording = isRecording();
        if (was_recording) {
            stopRecording();
        }

        this.sample_rate = sample_rate;
        this.samples_per_processing_buffer = sample_rate / recording_ms;

        if (was_recording) {
            startRecording();
        }
    }

    public void setRecordingMs(int recording_ms) {
        boolean was_recording = isRecording();
        if (was_recording) {
            stopRecording();
        }

        this.recording_ms = recording_ms;
        this.samples_per_processing_buffer = sample_rate / recording_ms;

        if (was_recording) {
            startRecording();
        }
    }

    public short[] getLastRecording() {
        return last_recording;
    }
}
