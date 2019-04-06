package com.example.controller;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.noob.noobcameraflash.managers.NoobCameraManager;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    public final String ACTION_USB_PERMISSION = "com.example.controller.USB_PERMISSION";
    Button startButton, forwardButton, rightButton, leftButton, backButton, clearButton, stopButton;
    TextView textView;
    EditText editText;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    boolean shutdown = false;
    Thread sensorMonitorThread;
    Thread sensorQueryThread;
    String databuf = "";

    Lock lock = new ReentrantLock();

    FirebaseFirestore db;

    private double latitude = -600;    //latitude of the MARCO (-600 is an invalid value used as a flag)
    private double longitude = -600;   //longitude of the MARCO (-600 is an invalid value used as a flag)

    //lock before modifying these variables
    double mq2 = 0, mq5 = 0, mq7 = 0, o2 = 0, temp = 0;

    // Runnable that will listen for changes on the sensor values
    private class sensorChangeDetectorWorker implements Runnable {
        private double prevMq2, prevMq5, prevMq7, prevO2, prevTemp;
        private DocumentReference doc = db.collection("MARCOs").document("MARCO1"); //TODO: recommend switching document name to current user UID for multiple MARCOs

        @Override
        public void run() {

            lock.lock();
            try {
                // Initialize previous values
                prevMq2 = mq2;
                prevMq5 = mq5;
                prevMq7 = mq7;
                prevO2 = o2;
                prevTemp = temp;
            } finally {
                lock.unlock();
            }

            while (!shutdown) {
                // If values have changed push to Firebase
                lock.lock();
                try {
                    if (mq2 != prevMq2)
                        doc.update("mq2", mq2);
                    if (mq5 != prevMq5)
                        doc.update("mq5", mq5);
                    if (mq7 != prevMq7)
                        doc.update("mq7", mq7);
                    if (o2 != prevO2)
                        doc.update("o2", o2);
                    if (temp != prevTemp)
                        doc.update("temp", temp);

                    // Update the previous values
                    prevMq2 = mq2;
                    prevMq5 = mq5;
                    prevMq7 = mq7;
                    prevO2 = o2;
                    prevTemp = temp;
                } finally {
                    lock.unlock();
                }

                // Sleep for 3 sec. Adjust this if you want to change frequency of updates
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class sensorQueryWorker implements Runnable {
        @Override
        public void run() {
            while (!shutdown) {
                onClickSend("123ot".getBytes());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    FirebaseAuth mAuth;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String recData;
            try {
                recData = new String(arg0, "UTF-8");
                databuf += recData;
                int index = databuf.indexOf('*');
                if (index != -1) {
                    String data = databuf.substring(0, index);
                    databuf = databuf.substring(index + 1);
                    tvAppend(textView, data);
                    char sensor = data.charAt(0);
                    double value = Double.parseDouble(data.substring(2));
                    lock.lock();
                    try {
                        switch (sensor) {
                            case 'o':
                                o2 = value;
                                break;
                            case 't':
                                temp = value;
                                break;
                            case '1':
                                mq2 = value;
                                break;
                            case '2':
                                mq5 = value;
                                break;
                            case '3':
                                mq7 = value;
                                break;
                            default:
                                break;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            tvAppend(textView, "Serial Connection Opened!\n");

                            sensorQueryThread = new Thread(new sensorQueryWorker());
                            sensorQueryThread.start();

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(stopButton);

            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        startButton = (Button) findViewById(R.id.buttonStart);

        forwardButton = (Button) findViewById(R.id.buttonForward);
        backButton = (Button) findViewById(R.id.buttonBack);
        leftButton = (Button) findViewById(R.id.buttonLeft);
        rightButton = (Button) findViewById(R.id.buttonRight);
        stopButton = (Button) findViewById(R.id.buttonStop);
        clearButton = (Button) findViewById(R.id.buttonClear);

        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        mAuth = FirebaseAuth.getInstance();
        new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //current user is signed in
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }

                //user is currently not signed in
                else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    mAuth.signOut();                                                                        //sign out the Firebase user
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));   //start the login activity
                }
            }
        };

        db = FirebaseFirestore.getInstance();

        // This starts the thread that monitors sensor values (and sends to Firebase)
        sensorMonitorThread = new Thread(new sensorChangeDetectorWorker());
        sensorMonitorThread.start();

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickSend("x".getBytes());

            }

        });

        forwardButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onClickSend("123ot".getBytes());
                    Log.d("Pressed", "Button pressed");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    dir = "x";
//                    onClickSend("x".getBytes());
//                    Log.d("Released", "Button released");
                }
                return false;
            }
        });

        leftButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onClickSend("a".getBytes());
                    Log.d("Pressed", "Button pressed");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    onClickSend("x".getBytes());
                    Log.d("Released", "Button released");
                }
                return false;
            }
        });
        backButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onClickSend("s".getBytes());
                    Log.d("Pressed", "Button pressed");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    onClickSend("x".getBytes());
                    Log.d("Released", "Button released");
                }
                return false;
            }
        });
        rightButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    onClickSend("d".getBytes());
                    Log.d("Pressed", "Button pressed");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    onClickSend("x".getBytes());
                    Log.d("Released", "Button released");
                }
                return false;
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickClear(v);
            }
        });

        //set up the camera for turning the flashlight on and off
        try {
            NoobCameraManager.getInstance().init(this);
        } catch (Exception e){
            /* do nothing */
        }

        /* TODO: This works for now, but will fail once there are multiple MARCOs, I would suggest changing this to listen only to the document associated with this MARCO (use UID) */
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
                            try {

                                //check to see if the doc is available
                                if (doc != null) {
                                    Double lm, rm;
                                    byte left = 0x5A, right = 0x5A;
                                    if ((lm = doc.getDouble("leftMotor")) != null) {
                                        left = (byte) ((int) ((double) lm) - 90);
                                    }
                                    if ((rm = doc.getDouble("rightMotor")) != null) {
                                        right = (byte) ((int) ((double) rm) - 90);
                                    }


                                    //check to see if the flashlight is available for use
                                    if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {

                                        try {

                                            //check to see what the flashlight state should be
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                //turn the light on
                                                if(doc.getBoolean("light")){
                                                    NoobCameraManager.getInstance().turnOnFlash();
                                                }

                                                //turn the light off
                                                else{
                                                    NoobCameraManager.getInstance().turnOffFlash();
                                                }
                                            }
                                        } catch (Exception nullRef) {
                                            /* do nothing */
                                        }
                                    }

                                    Log.d("MOTORS", "s" + left + right);
                                    onClickSend("s".getBytes());
                                    byte[] bytes = {left, right};
                                    onClickSend(bytes);
                                }
                            }
                            //null pointer exception received
                            catch (Exception nullRef) {
                                Log.w(TAG, "POJO Conversion failed.", nullRef);
                            }
                        }
                    }
                });

        //Check to see if location service is currently permitted
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED
                && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);  //used to get the combined network and gps data for better accuracy
            LocationRequest mLocationRequest = new LocationRequest();                                                         //initialize the location request to be used with the FusedLocationProviderClient

            // update location every second
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);   //set the location service to the highest accuracy possible

            //called when the FusedLocationProviderClient has a location update
            LocationCallback mLocationCallback = new LocationCallback() {

                //location update received
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    List<Location> locationList = locationResult.getLocations();
                    if (locationList.size() > 0) {

                        //The last location in the list is the newest
                        Location location = locationList.get(locationList.size() - 1);
                        Map<String, Object> position = new HashMap<>();
                        position.put("latitude", latitude = location.getLatitude());
                        position.put("longitude", longitude = location.getLongitude());

                        //Update the location in the Firestore
                        try {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("MARCOs").document("MARCO1").update("position", position);
                        } catch (
                                Exception nullRef) {
                            /* do nothing */
                        }
                    }
                }
            };
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());    //get updates to the user's current location
        }
    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        // sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        textView.setEnabled(bool);

    }

    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }


    }

    public void onClickSend(byte[] bytes) {
        serialPort.write(bytes);
        tvAppend(textView, "\nData Sent : " + Arrays.toString(bytes) + "\n");

    }

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
        tvAppend(textView, "\nSerial Connection Closed! \n");

    }

    public void onClickClear(View view) {
        textView.setText(" ");
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }

    @Override
    public void onDestroy() {
        mAuth.signOut();

        // Shuts down and joins the sensor thread when this activity is destroyed
        shutdown = true;
        try {
            sensorMonitorThread.join();
            sensorQueryThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void logout(View view) {
        mAuth.signOut();
        Log.d("LOGOUT", "ping");
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

}
