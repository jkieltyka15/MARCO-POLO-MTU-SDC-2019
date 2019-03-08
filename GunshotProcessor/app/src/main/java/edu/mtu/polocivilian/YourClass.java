package edu.mtu.polocivilian;

public class YourClass {
    private static YourClass INSTANCE = new YourClass();

    public static YourClass getInstance() {
        return INSTANCE;
    }

    public boolean run(int sample_rate, short[] audio) {
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
        boolean gunshot = false;
        /* This is the main portion of the gunshot classification. It checks the values of the microphone if they are
         * greater than a certain threshold. If so perform a FFT and then determine if any of those values are
         * greater than a threshold. If they are it classifies it as a gunshot and breaks out of the loop.
         *
         */
        for (int k = 0; k < audioComplex.length && !gunshot; k++) {
            //Determine if there is a sound significant enough to investigate
            //Buffer contains values from -32,000 to +32,000. set our threshold to 80%
            if (audioComplex[k].abs() > 25600) {
                //for the FFT audioComplex must be a power of 2. With our current sample rate we
                //end up with an audioComplex length of 65532, manually add the values to get to
                //a length that is a power of 2.
                //START: these values are manually made and will change is record duration is changed!
                audioComplex[65532] = audioComplex[65531];
                audioComplex[65533] = audioComplex[65531];
                audioComplex[65534] = audioComplex[65531];
                audioComplex[65535] = audioComplex[65531];
                //END
                System.out.println("FFT Analysis Triggered");
                Complex[] audioFFT = FFT.fft(audioComplex);
                System.out.println("FFT Complete, beginning loop");


                //only going to the 5000th bin in the FFT, everything else has minimal probability of being a gunshot
                for (int j = 0; j < 5000; j++) {
                    System.out.println("FFT loop analysis iteration: " +j);
                    //Sound has enough power to be a gunshot by fft
                    //28 million is just a ratio (0.8 to 900 as 26500 is to 28 million
                    //This might not be the right threshold
                    if (audioFFT[j].abs() > 28000000) {
                        gunshot = true;
                        return gunshot;
                    }
                }

            }
        }
        return gunshot;
    }
}
    //The result contains the mirrored imaginary portion of the FFT that we do not need, we can delete
    //this later if necessary.

