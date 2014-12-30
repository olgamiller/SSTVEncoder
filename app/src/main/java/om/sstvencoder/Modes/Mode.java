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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public abstract class Mode {
    private final int mSampleRate;

    protected Bitmap mBitmap;
    protected int mVISCode;
    private int mLine;

    private short[] mAudioBuffer;
    private AudioTrack mAudioTrack;
    private double mRunningIntegral;
    private int mBufferPos;

    protected Mode() {
        mSampleRate = 44100;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void init(){
        initAudio();
        mRunningIntegral = 0.0;
        mLine = 0;
        sendCalibrationHeader();
    }

    public boolean process() {
        if (mLine < mBitmap.getHeight()) {
            resetBuffer();
            writeEncodedLine(mLine++);
            playBuffer();
            return true;
        } else {
            return false;
        }
    }

    public void finish(){
        destroyAudio();
    }

    protected abstract void writeEncodedLine(int y);

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
        return (int) (durationMs * mSampleRate / 1e3);
    }

    protected Bitmap scaleBitmap(Bitmap bmp, int ow, int oh) {
        Bitmap result = Bitmap.createBitmap(ow, oh, Bitmap.Config.ARGB_8888);
        int iw = bmp.getWidth();
        int ih = bmp.getHeight();
        RectF rect;

        if (iw * oh < ow * ih) {
            rect = new RectF(0, 0, (iw * oh) / ih, oh);
            rect.offsetTo((ow - (iw * oh) / ih) / 2, 0);
        } else {
            rect = new RectF(0, 0, ow, (ih * ow) / iw);
            rect.offsetTo(0, (oh - (ih * ow) / iw) / 2);
        }

        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bmp, null, rect, new Paint(Paint.FILTER_BITMAP_FLAG));

        return result;
    }

    protected void setTone(double frequency) {
        mRunningIntegral += 2.0 * frequency * Math.PI / (double) mSampleRate;
        mRunningIntegral %= 2.0 * Math.PI;
        mAudioBuffer[mBufferPos++] = (short) (Math.sin(mRunningIntegral) * 32768.0);
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

    private void initAudio() {
        mAudioBuffer = new short[mSampleRate];//1 second of buffer
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, mAudioBuffer.length * 2,
                AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    private void destroyAudio() {
        mAudioTrack.stop();
        mAudioTrack.release();
        mAudioBuffer = null;
    }
}
