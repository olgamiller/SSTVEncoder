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
import android.graphics.Color;

//WRAASE SC2-180
@ModeSize(getWidth = 320, getHeight = 256)
public class Wraase extends Mode {
    private final int mColorScanSamples;

    private final int mSyncPulseSamples;
    private final double mSyncPulseFrequency;

    private final int mPorchSamples;
    private final double mPorchFrequency;

    public Wraase(Bitmap bitmap) {
        super(bitmap);

        mVISCode = 55;

        mColorScanSamples = convertMsToSamples(235.0);

        mSyncPulseSamples = convertMsToSamples(5.5225);
        mSyncPulseFrequency = 1200.0;

        mPorchSamples = convertMsToSamples(0.5);
        mPorchFrequency = 1500.0;
    }

    protected void writeEncodedLine() {
        addSyncPulse();
        addPorch();
        addRedScan(mLine);
        addGreenScan(mLine);
        addBlueScan(mLine);
    }

    private void addSyncPulse() {
        for (int i = 0; i < mSyncPulseSamples; ++i)
            setTone(mSyncPulseFrequency);
    }

    private void addPorch() {
        for (int i = 0; i < mPorchSamples; ++i)
            setTone(mPorchFrequency);
    }

    private void addRedScan(int y) {
        for (int i = 0; i < mColorScanSamples; ++i)
            setColorTone(Color.red(mBitmap.getPixel(i * mBitmap.getWidth() / mColorScanSamples, y)));
    }

    private void addGreenScan(int y) {
        for (int i = 0; i < mColorScanSamples; ++i)
            setColorTone(Color.green(mBitmap.getPixel(i * mBitmap.getWidth() / mColorScanSamples, y)));
    }

    private void addBlueScan(int y) {
        for (int i = 0; i < mColorScanSamples; ++i)
            setColorTone(Color.blue(mBitmap.getPixel(i * mBitmap.getWidth() / mColorScanSamples, y)));
    }
}
