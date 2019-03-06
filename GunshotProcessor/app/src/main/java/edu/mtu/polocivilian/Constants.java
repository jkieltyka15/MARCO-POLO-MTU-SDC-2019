package edu.mtu.polocivilian;

import android.media.AudioFormat;

public class Constants {
    /// How many samples are made by the AudioRecord per second
    /// All Android Phones support 44100, but most support other rates.
    protected static final int RECORDER_SAMPLERATE = 44100;
    /// How long (in ms) the sample sent for processing will be
    protected static final int RECORDER_SAMPLE_DURATION_MS = 2000;

    /// Required configuration for AudioRecord
    ///     If RECORDER_AUDIO_ENCODING is changed then the
    //      short[] buffer will also need to be changed in AudioCollector / AudioProcessor
    protected static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    protected static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    /// An arbitrary value, just a number we can get back from permissions requests
    protected static final int PERMISSIONS_REQUEST_ALL = 7777;
}
