/*
Copyright 2015 Olga Miller <olga.rgb@googlemail.com>

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

package om.sstvencoder.Modes.PDModes;

import android.graphics.Bitmap;

import om.sstvencoder.ImageFormats.Yuv;
import om.sstvencoder.ImageFormats.YuvImageFormat;
import om.sstvencoder.Modes.Mode;

public abstract class PD extends Mode {
    private final Yuv mYuv;

    protected int mColorScanSamples;
    protected double mColorScanDurationMs;

    private final int mSyncPulseSamples;
    private final double mSyncPulseFrequency;

    private final int mPorchSamples;
    private final double mPorchFrequency;

    public PD(Bitmap bitmap) {
        super(bitmap);

        mYuv = Yuv.createYuv(mBitmap, YuvImageFormat.YUV440P);

        mSyncPulseSamples = convertMsToSamples(20.0);
        mSyncPulseFrequency = 1200.0;

        mPorchSamples = convertMsToSamples(2.08);
        mPorchFrequency = 1500.0;
    }

    protected void writeEncodedLine() {
        addSyncPulse();
        addPorch();
        addYScan(mLine);
        addVScan(mLine);
        addUScan(mLine);
        addYScan(++mLine);
    }

    private void addSyncPulse() {
        for (int i = 0; i < mSyncPulseSamples; ++i)
            setTone(mSyncPulseFrequency);
    }

    private void addPorch() {
        for (int i = 0; i < mPorchSamples; ++i)
            setTone(mPorchFrequency);
    }

    private void addYScan(int y) {
        for (int i = 0; i < mColorScanSamples; ++i)
            setColorTone(mYuv.getY((i * mYuv.getWidth()) / mColorScanSamples, y));
    }

    private void addUScan(int y) {
        for (int i = 0; i < mColorScanSamples; ++i)
            setColorTone(mYuv.getU((i * mYuv.getWidth()) / mColorScanSamples, y));
    }

    private void addVScan(int y) {
        for (int i = 0; i < mColorScanSamples; ++i)
            setColorTone(mYuv.getV((i * mYuv.getWidth()) / mColorScanSamples, y));
    }
}
