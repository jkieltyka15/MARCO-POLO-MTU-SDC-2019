package edu.mtu.polocivilian;

import android.location.Location;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class YourClass {
    private static YourClass INSTANCE = new YourClass();

    public static YourClass getInstance() {
        return INSTANCE;
    }
    private double latitude = -600;    //latitude of the current user (-600 is an invalid value used as a flag)
    private double longitude = -600;
    private int threatLvl;


    public void run(short[] audio) {
        //our class is initialized using variable buffer as the audio source

        //Initializing gunshot variable and defaulting it to false
        /*This will need to be the voltage readings in the actual application
         * The complex class is telling to app to change the array to allow for complex values. We do this by creating
         * a complex array a populating it with the values from the microphone voltage for the real number and 0 for the
         * imaginary portion.
         */
        Complex[] audioComplex = new Complex[audio.length];

        for (int i = 0; i < audioComplex.length; i++) {
            audioComplex[i] = new Complex(audio[i], 0);
        }
        boolean triggeredFFT = false;
        /* This is the main portion of the gunshot classification. It checks the values of the microphone if they are
         * greater than a certain threshold. If so perform a FFT and then determine if any of those values are
         * greater than a threshold. If they are it classifies it as a gunshot and breaks out of the loop.
         *
         */
        for (int k = 0; k < audioComplex.length && !triggeredFFT; k++) {
            //Determine if there is a sound significant enough to investigate
            //Buffer contains values from -32,000 to +32,000. set our threshold to 80%
            if (audioComplex[k].abs() > 25600) {
                System.out.println(audioComplex);
                triggeredFFT = true;
                //for the FFT audioComplex must be a power of 2. With our current sample rate we
                //end up with an audioComplex length of 65532, manually add the values to get to
                //a length that is a power of 2.
                //START: these values are manually made and will change is record duration is changed!
                audioComplex[65532] = audioComplex[65531];
                audioComplex[65533] = audioComplex[65531];
                audioComplex[65534] = audioComplex[65531];
                audioComplex[65535] = audioComplex[65531];
                //END

                Complex[] audioFFT = FFT.fft(audioComplex);



                //only going from the 600 the 5000th bin in the FFT, everything else has minimal probability of being a gunshot
                //and we can eliminate false positives that occur at the 0 and low freq
                for (int j = 600; j < 5000; j++) {
                    //Sound has enough power to be a gunshot by fft
                    //28 million is just a ratio (0.8 to 26500 as 900 is to 28 million
                    /* this might not be the correct threshold or the right way to determine it. When
                    we determined the limit our mic threshold was 0.8 and 600 for the FFT with a max mic value of 1 from the mic.
                    When we do it here
                    our values have a max of 32,000 so we take .8 of that and then use the ratio of 0.8 to 26500
                    as 700/900/350 is to ___ to determine the threshold. If the FFT does not translate it the same
                    as it did in MATLAB  our ratio will not suffice to determine the threshold.
                    We would need to print the gunshot values from the mic for an actual gunshot and redetermine thresholds.
                     */
                    //900 limit is 28,000,000
                    //350 limit is 12,000,000
                    //700 limit is 23,000,000
                    if (audioFFT[j].abs() > 12000000) {
                        Gunshot is_Gunshot = new Gunshot();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Location location = is_Gunshot.getPosition();
                        Map<String, Object> position = new HashMap<>();
                        position.put("latitude", latitude = location.getLatitude());
                        position.put("longitude", longitude = location.getLongitude());
                        position.put("threatLvl", threatLvl = is_Gunshot.getThreatLvl());

                        db.collection("Gunshots").document().set(position);
                        MainActivity.getInstance().updateUI(is_Gunshot);
                        break;
                        }
                    }
                }
            }
        }
    }
    //The result contains the mirrored imaginary portion of the FFT that we do not need, we can delete
    //this later if necessary.

