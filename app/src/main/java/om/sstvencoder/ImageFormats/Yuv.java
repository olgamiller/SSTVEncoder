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

package om.sstvencoder.ImageFormats;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.YuvImage;

public abstract class Yuv {
    protected byte[] mYuv;
    protected final int mFormat;
    protected final int mWidth;
    protected final int mHeight;

    protected Yuv(Bitmap bitmap, int format) {
        mFormat = format;
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        convertBitmapToYuv(bitmap);
    }

    protected abstract void convertBitmapToYuv(Bitmap bitmap);

    public static Yuv createYuv(Bitmap bitmap, int format) {
        switch (format) {
            case YuvImageFormat.YV12:
                return new YV12(bitmap, format);
            case YuvImageFormat.NV21:
                return new NV21(bitmap, format);
            case YuvImageFormat.YUY2:
                return new YUY2(bitmap, format);
            case YuvImageFormat.YUV440P:
                return new YUV440P(bitmap, format);
            default:
                throw new IllegalArgumentException("Only support YV12, NV21, YUY2 and YUV440P");
        }
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public YuvImage getYuvImage() {
        return new YuvImage(mYuv, mFormat, mWidth, mHeight, null);
    }

    public abstract int getY(int x, int y);

    public abstract int getU(int x, int y);

    public abstract int getV(int x, int y);

    public static int convertToY(int color) {
        double R = Color.red(color);
        double G = Color.green(color);
        double B = Color.blue(color);
        return clamp(16.0 + (.003906 * ((65.738 * R) + (129.057 * G) + (25.064 * B))));
    }

    public static int convertToU(int color) {
        double R = Color.red(color);
        double G = Color.green(color);
        double B = Color.blue(color);
        return clamp(128.0 + (.003906 * ((-37.945 * R) + (-74.494 * G) + (112.439 * B))));
    }

    public static int convertToV(int color) {
        double R = Color.red(color);
        double G = Color.green(color);
        double B = Color.blue(color);
        return clamp(128.0 + (.003906 * ((112.439 * R) + (-94.154 * G) + (-18.285 * B))));
    }

    private static int clamp(double value) {
        return value < 0.0 ? 0 : (value > 255.0 ? 255 : (int) value);
    }
}
