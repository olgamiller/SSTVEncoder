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
import android.graphics.ImageFormat;

import om.sstvencoder.ImageFormats.Yuv;

public class Robot36 extends Mode {
    private final Yuv mYuv;

    private final int mLumaScanSamples;
    private final int mChrominanceScanSamples;

    private final int mSyncPulseSamples;
    private final double mSyncPulseFrequency;

    private final int mSyncPorchSamples;
    private final double mSyncPorchFrequency;

    private final int mPorchSamples;
    private final double mPorchFrequency;

    private final int mSeparatorSamples;
    private final double mEvenSeparatorFrequency;
    private final double mOddSeparatorFrequency;

    public Robot36(Bitmap bitmap) {
        mBitmap = scaleBitmap(bitmap, 320, 240);
        mYuv = Yuv.createYuv(mBitmap, ImageFormat.NV21);
        mVISCode = 8;

        mLumaScanSamples = convertMsToSamples(88.0);
        mChrominanceScanSamples = convertMsToSamples(44.0);

        mSyncPulseSamples = convertMsToSamples(9.0);
        mSyncPulseFrequency = 1200.0;

        mSyncPorchSamples = convertMsToSamples(3.0);
        mSyncPorchFrequency = 1500.0;

        mPorchSamples = convertMsToSamples(1.5);
        mPorchFrequency = 1900.0;

        mSeparatorSamples = convertMsToSamples(4.5);
        mEvenSeparatorFrequency = 1500.0;
        mOddSeparatorFrequency = 2300.0;
    }

    protected void writeEncodedLine(int y) {
        addSyncPulse();
        addSyncPorch();
        addYScan(y);

        if (y % 2 == 0) {
            addSeparator(mEvenSeparatorFrequency);
            addPorch();
            addVScan(y);
        } else {
            addSeparator(mOddSeparatorFrequency);
            addPorch();
            addUScan(y);
        }
    }

    private void addSyncPulse() {
        for (int i = 0; i < mSyncPulseSamples; ++i)
            setTone(mSyncPulseFrequency);
    }

    private void addSyncPorch() {
        for (int i = 0; i < mSyncPorchSamples; ++i)
            setTone(mSyncPorchFrequency);
    }

    private void addSeparator(double separatorFrequency) {
        for (int i = 0; i < mSeparatorSamples; ++i)
            setTone(separatorFrequency);
    }

    private void addPorch() {
        for (int i = 0; i < mPorchSamples; ++i)
            setTone(mPorchFrequency);
    }

    private void addYScan(int y) {
        for (int i = 0; i < mLumaScanSamples; ++i)
            setColorTone(mYuv.getY((i * mYuv.getWidth()) / mLumaScanSamples, y));
    }

    private void addUScan(int y) {
        for (int i = 0; i < mChrominanceScanSamples; ++i)
            setColorTone(mYuv.getU((i * mYuv.getWidth()) / mChrominanceScanSamples, y));
    }

    private void addVScan(int y) {
        for (int i = 0; i < mChrominanceScanSamples; ++i)
            setColorTone(mYuv.getV((i * mYuv.getWidth()) / mChrominanceScanSamples, y));
    }
}

