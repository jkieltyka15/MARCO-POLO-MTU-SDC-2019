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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.ContentValues.TAG;
import static android.location.LocationManager.GPS_PROVIDER;

public class MainActivity extends Activity {
    public final String ACTION_USB_PERMISSION = "com.example.controller.USB_PERMISSION";
    Button startButton, forwardButton,rightButton, leftButton,backButton, clearButton, stopButton;
    TextView textView;
    EditText editText;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    boolean shutdown = false;
    Thread sensorThread;
    String databuf = "";
    //TODO: ANYTHING THAT MODIFIES THE SENSOR VALUES MUST FIRST CALL LOCK.LOCK().
    //TODO: MODIFICATION SHOULD BE WITHIN A TRY BLOCK AND FOLLOWED BY A FINALLY BLOCK THAT CALLS LOCK.UNLOCK()
    //TODO: A RACE CONDITION WILL EXIST OTHERWISE
    //TODO: RACE CONDITION = BAD
    Lock lock = new ReentrantLock();
    String dir = "x"; // to start off the commands

    FirebaseFirestore db;
    double leftMotor = 0;
    double rightMotor = 0;

    //TODO: LOCK BEFORE MODIFYING ANY OF THESE VARIABLES
    double mq2 = 0, mq5 = 0, mq7 = 0, o2 = 0, temp = 0;

    // Runnable that will listen for changes on the sensor values
    private class sensorChangeDetectorWorker implements Runnable {
        private double prevMq2, prevMq5, prevMq7, prevO2, prevTemp;
        private DocumentReference doc = db.collection("MARCOs").document("MARCO1");

        @Override
        public void run() {
            //TODO: SEE THIS FOR HOW TO IMPLEMENT THE LOCK
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

            while(!shutdown) {
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

    FirebaseAuth mAuth;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String recData;
            try {
                recData = new String(arg0, "UTF-8");
                databuf += recData;
                //textView.setText("");
                //textView.setText(" ");
                int index = databuf.indexOf('*');
                if(index != -1) {
                    String data = databuf.substring(0, index);
                    databuf = databuf.substring(index+1);
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
                            tvAppend(textView,"Serial Connection Opened!\n");

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
        sensorThread = new Thread(new sensorChangeDetectorWorker());
        sensorThread.start();

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                dir = "x";
                onClickSend(v);

            }

        });


        forwardButton.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dir = "123ot";
                    onClickSend(v);
                    Log.d("Pressed", "Button pressed");
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    dir = "x";
//                    onClickSend(v);
//                    Log.d("Released", "Button released");
                    // TODO Auto-generated method stub
                }
                return false;
            }
        });

        leftButton.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dir = "a";
                    onClickSend(v);
                    Log.d("Pressed", "Button pressed");
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    dir = "x";
                    onClickSend(v);
                    Log.d("Released", "Button released");
                    // TODO Auto-generated method stub
                }
                return false;
            }
        });
        backButton.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dir = "s";
                    onClickSend(v);
                    Log.d("Pressed", "Button pressed");
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    dir = "x";
                    onClickSend(v);
                    Log.d("Released", "Button released");
                    // TODO Auto-generated method stub
                }
                return false;
            }
        });
        rightButton.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    dir = "d";
                    onClickSend(v);
                    Log.d("Pressed", "Button pressed");
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    dir = "x";
                    onClickSend(v);
                    Log.d("Released", "Button released");
                    // TODO Auto-generated method stub
                }
                return false;
            }
        });


        /**
         * the following code works for sending data its just its just commented out
         * for the testing of sending two commands on ButtonPress and ButtonRelease events (Action_down)
         * and (Action_UP). can explain what is happening if you have any concerns.
         */

//        forwardButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v){
//                dir = "w";
//                onClickSend(v);
//            }
//        });
//        forwardButton.setOnTouchListener(new View.OnTouchListener(){
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch(event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        // PRESSED
//                        return true; // if you want to handle the touch event
//                    case MotionEvent.ACTION_UP:
//                        // RELEASED
//                        return true; // if you want to handle the touch event
//                }
//                return false;
//            }
//        });
//        leftButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v){
//                dir = "a";
//                onClickSend(v);
//            }
//        });
//        backButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v){
//                dir = "s";
//                onClickSend(v);
//            }
//        });
//        rightButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v){
//                dir = "d";
//
//                onClickSend(v);
//            }
//        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                onClickClear(v);
            }
        });

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
                                    if((lm = doc.getDouble("leftMotor")) != null) {
                                        leftMotor = lm;
                                    } else {
                                        leftMotor = 0;
                                    }
                                    if((rm = doc.getDouble("rightMotor")) != null) {
                                        rightMotor = rm;
                                    } else {
                                        rightMotor = 0;
                                    }

                                    Log.d("MOTORVAL", String.format("Left: %f, Right: %f", leftMotor, rightMotor));

                                }
                            }
                            //null pointer exception received
                            catch (Exception nullRef) {
                                Log.w(TAG, "POJO Conversion failed.", nullRef);
                            }
                        }
                    }
                });

        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED
                && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED) {

            //get user's current location
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(GPS_PROVIDER);

            //set a listener to always get the updated location
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {

                boolean update = true;  //used as a flag for updating the location to firebase

                /**
                 * Update the location of the current user in Firestore
                 *
                 * @param location - The user's current location
                 */
                @Override
                public void onLocationChanged(Location location) {

                    if (update) {

                        //Update the location for PoloUser
                        Map<String, Object> position = new HashMap<>();
                        if(location != null) {
                            position.put("latitude", location.getLatitude());
                            position.put("longitude", location.getLongitude());

                            //Update the location in the Firestore
                            try {
                                db.collection("MARCOs").document("MARCO1").update("position", position);
                            } catch (Exception nullRef) {
                                /* do nothing */
                            }
                        }

                        update = false;

                        //only update the location every 3 seconds
                        new CountDownTimer(3000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                /* do nothing */
                            }

                            public void onFinish() {
                                update = true;
                            }
                        }.start();
                    }

                }

                @Override
                public void onProviderDisabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                    // TODO Auto-generated method stub
                }
            });

            //Update the location in the Firestore
            Map<String, Object> position = new HashMap<>();
            if(location != null) {
                position.put("latitude", location.getLatitude());
                position.put("longitude", location.getLongitude());
                FirebaseFirestore db = FirebaseFirestore.getInstance(); //initialize the Firestore
                try {
                    db.collection("MARCOs").document("MARCO1").update("position", position);
                    Log.d("POSITION", String.format("lat: %f, long: %f", (double) position.get("latitude"), (double) position.get("longitude")));
                } catch (Exception nullRef) {
                    /* do nothing */
                    Log.d("POSITION", "Null ref");
                }
            }
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

    public void onClickSend(View view) {
        String string = editText.getText().toString();
        String temp = dir;
        //String string = command.toString();
        serialPort.write(temp.getBytes());
        tvAppend(textView, "\nData Sent : " + temp + "\n");

    }

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
        tvAppend(textView,"\nSerial Connection Closed! \n");

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
    public void onDestroy(){
        mAuth.signOut();

        // Shuts down and joins the sensor thread when this activity is destroyed
        shutdown = true;
        try{
            sensorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void logout(View view){
        mAuth.signOut();
        Log.d("LOGOUT", "ping");
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

}
