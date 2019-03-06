package edu.mtu.polocivilian;

import android.support.annotation.NonNull;

public class Utils {

    public static byte[] short_to_byte(@NonNull short[] input) {
        byte[] output = new byte[input.length * 2];

        for(int i = 0; i < input.length; i++)
        {
            output[i*2]     = (byte) (input[i] & 0x00FF);
            output[i*2 + 1] = (byte) ((input[i] & 0xFF00) >> 8);
        }

        return output;
    }
}
