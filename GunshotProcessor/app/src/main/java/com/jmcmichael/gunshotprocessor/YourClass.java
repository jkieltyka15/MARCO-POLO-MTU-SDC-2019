package com.jmcmichael.gunshotprocessor;

public class YourClass {
    private static YourClass INSTANCE = new YourClass();

    public static YourClass getInstance() {
        return INSTANCE;
    }

    public boolean run(int sample_rate, short[] audio) {
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
        /* This i the main portion of the gunshot classification. It checks the values of the microphone if they are
         * greater than a certain threshold. If so perform a FFT and then determine if any of those values are
         * greater than a threshold. If they are it classifies it as a gunshot and breaks out of the loop.
         *
         */
        for (int k = 0; k < audioComplex.length && !gunshot; k++) {
            //Determine if there is a sound significant enough to investigate
            if (audioComplex[k].abs() > 0.8) {
                Complex[] audioFFT = FFT.fft(audioComplex);

                for (int j = 0; j < audioFFT.length; j++) {
                    //Sound has enough power to be a gunshot by fft
                    if (audioFFT[j].abs() > 1000) {
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

