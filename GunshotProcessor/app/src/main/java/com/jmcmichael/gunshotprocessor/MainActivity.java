package com.jmcmichael.gunshotprocessor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {
    private static MainActivity INSTANCE;

    private AudioCollector recorder;
    private LocationManager locationManager;
    private FirebaseAuth mAuth;

    private Location location = null;
    private boolean setupComplete = false;

    AudioTrack audioTrack;
    boolean playing;


    protected static MainActivity getInstance() {
        return INSTANCE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        INSTANCE = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        recorder = AudioCollector.getInstance();

        String[] neededPermissions = checkPermissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (neededPermissions.length > 0) {
            ActivityCompat.requestPermissions(this,
                    neededPermissions,
                    Constants.PERMISSIONS_REQUEST_ALL);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // If we're not signed in, signin
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("AUTHENTICATION", "signInAnonymously:success");
                                ((TextView) findViewById(R.id.txtAuthStatus)).setText(R.string.auth_success);
                                configure();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("AUTHENTICATION", "signInAnonymously:failure", task.getException());
                                ((TextView) findViewById(R.id.txtAuthStatus)).setText(R.string.auth_failure);

                            }
                        }
                    });
        } else {
            ((TextView) findViewById(R.id.txtAuthStatus)).setText(R.string.auth_success);
            configure();
        }
    }

    public String[] checkPermissions(String... permissions) {
        ArrayList<String> lackPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                lackPermissions.add(permission);
            }
        }

        String[] res = new String[lackPermissions.size()];
        lackPermissions.toArray(res);

        return res;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean allVerified = true;

        if (requestCode == Constants.PERMISSIONS_REQUEST_ALL) {
            for (int grant : grantResults) {
                allVerified &= grant == PackageManager.PERMISSION_GRANTED;
            }
        }

        if (allVerified) {
            configure();
        }
    }

    /// Configuration doesn't happen if we don't have all our permissions,
    ///     So this will never cause a SecurityException
    @SuppressLint("MissingPermission")
    public void configure() {
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(recorder.getSamplesPerProcessingBuffer())
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(Constants.RECORDER_SAMPLERATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_DEFAULT)
                        .setEncoding(Constants.RECORDER_AUDIO_ENCODING)
                        .build())
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_NONE)
                .build();

        audioTrack.setVolume(1.0f);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        checkEnableButtons();
        setButtonHandlers();
    }

    private void checkEnableButtons() {
        if (setupComplete) {
            return;
        }

        setupComplete = this.location != null && mAuth.getCurrentUser() != null;

        if (setupComplete) {
            enableButtons(false);
        }
    }

    private void setButtonHandlers() {
        findViewById(R.id.btnStart).setOnClickListener(btnClick);
        findViewById(R.id.btnStop).setOnClickListener(btnClick);
        findViewById(R.id.btnPlay).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        findViewById(id).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart: {
                    enableButtons(true);
                    recorder.startRecording();
                    break;
                }
                case R.id.btnStop: {
                    enableButtons(false);
                    recorder.stopRecording();
                    break;
                }
                case R.id.btnPlay: {
                    if (playing) {
                        audioTrack.stop();
                    }
                    Log.d("RECORDING", recorder.getLastRecording().toString());
                    audioTrack.write(recorder.getLastRecording(), 0, recorder.getSamplesPerProcessingBuffer());
                    audioTrack.play();
                    playing = true;
                }
            }
        }
    };

    public void updateUI(GunshotDocument gunshot) {
        DecimalFormat format = new DecimalFormat("##0.000000");
        ((TextView) findViewById(R.id.txtTimestamp)).setText(gunshot.timestamp.toDate().toString());
        ((TextView) findViewById(R.id.txtLatitude)).setText(format.format(gunshot.location.getLatitude()));
        ((TextView) findViewById(R.id.txtLongitude)).setText(format.format(gunshot.location.getLongitude()));
        ((TextView) findViewById(R.id.txtResult)).setText(gunshot.is_gunshot ? "GUNSHOT DETECTED" : "NO GUNSHOT DETECTED");
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LOCATION", "Location Changed to " + location);
        this.location = location;

        checkEnableButtons();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("LOCATION", "Disabled Provider: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("LOCATION", "Enabled provider: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("LOCATION", "Status Changed: " + provider + ": status=" + status + " extras=" + extras);
    }
}
