/*
Copyright 2014 Olga Miller <olga.rgb@googlemail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package om.sstvencoder.Modes;

import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.lang.reflect.Constructor;

public abstract class Mode {
    private final int mSampleRate;

    protected Bitmap mBitmap;
    protected int mVISCode;
    protected int mLine;

    private short[] mAudioBuffer;
    private AudioTrack mAudioTrack;
    private double mRunningIntegral;
    private int mBufferPos;

    public static Mode Create(Class<? extends Mode> modeClass, Bitmap bitmap) {
        Mode mode = null;

        if (bitmap != null && modeClass.isAnnotationPresent(ModeSize.class)) {
            ModeSize size = modeClass.getAnnotation(ModeSize.class);

            if (bitmap.getWidth() == size.getWidth() && bitmap.getHeight() == size.getHeight()) {
                try {
                    Constructor constructor = modeClass.getConstructor(Bitmap.class);
                    mode = (Mode) constructor.newInstance(bitmap);
                } catch (Exception ignore) {
                }
            }
        }

        return mode;
    }

    protected Mode(Bitmap bitmap) {
        mSampleRate = 44100;
        mBitmap = bitmap;
    }

    public void init() {
        initAudio();
        mRunningIntegral = 0.0;
        mLine = 0;
        sendCalibrationHeader();
    }

    public boolean process() {
        if (mLine < mBitmap.getHeight()) {
            resetBuffer();
            writeEncodedLine();
            ++mLine;
            playBuffer();
            return true;
        } else {
            return false;
        }
    }

    // Note that also Bitmap will be recycled here
    public void finish() {
        destroyAudio();
        destroyBitmap();
    }

    protected abstract void writeEncodedLine();

    private void sendCalibrationHeader() {
        int leaderToneSamples = convertMsToSamples(300.0);
        double leaderToneFrequency = 1900.0;

        int breakSamples = convertMsToSamples(10.0);
        double breakFrequency = 1200.0;

        int visBitSamples = convertMsToSamples(30.0);
        double visBitSSFrequency = 1200.0;
        double[] visBitFrequency = new double[]{1300.0, 1100.0};

        resetBuffer();

        for (int i = 0; i < leaderToneSamples; ++i)
            setTone(leaderToneFrequency);

        for (int i = 0; i < breakSamples; ++i)
            setTone(breakFrequency);

        for (int i = 0; i < leaderToneSamples; ++i)
            setTone(leaderToneFrequency);

        for (int i = 0; i < visBitSamples; ++i)
            setTone(visBitSSFrequency);

        int parity = 0;
        for (int pos = 0; pos < 7; ++pos) {
            int bit = (mVISCode >> pos) & 1;
            parity ^= bit;
            for (int i = 0; i < visBitSamples; ++i)
                setTone(visBitFrequency[bit]);
        }

        for (int i = 0; i < visBitSamples; ++i)
            setTone(visBitFrequency[parity]);

        for (int i = 0; i < visBitSamples; ++i)
            setTone(visBitSSFrequency);

        playBuffer();
    }

    protected int convertMsToSamples(double durationMs) {
        return (int) Math.round(durationMs * mSampleRate / 1e3);
    }

    protected void setTone(double frequency) {
        mRunningIntegral += 2.0 * frequency * Math.PI / (double) mSampleRate;
        mRunningIntegral %= 2.0 * Math.PI;
        mAudioBuffer[mBufferPos++] = (short) (Math.sin(mRunningIntegral) * Short.MAX_VALUE);
    }

    protected void setColorTone(int color) {
        double blackFrequency = 1500.0;
        double whiteFrequency = 2300.0;
        setTone(color * (whiteFrequency - blackFrequency) / 255.0 + blackFrequency);
    }

    protected void resetBuffer() {
        mBufferPos = 0;
    }

    protected void playBuffer() {
        mAudioTrack.write(mAudioBuffer, 0, mBufferPos);
    }

    protected void drainBuffer() {
        for (int i = 0; i < mAudioBuffer.length; ++i)
            mAudioBuffer[i] = 0;
        mAudioTrack.write(mAudioBuffer, 0, mAudioBuffer.length);
    }

    private void initAudio() {
        mAudioBuffer = new short[(5 * mSampleRate) / 2]; // 2.5 seconds of buffer
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, mAudioBuffer.length * 2,
                AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    private void destroyAudio() {
        drainBuffer();
        mAudioTrack.stop();
        mAudioTrack.release();
        mAudioBuffer = null;
    }

    private void destroyBitmap() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
