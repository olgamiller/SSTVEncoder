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

package om.sstvencoder;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;

public class Yuv {
    private final byte[] mYuv;
    private final int mFormat;
    private final int mWidth;
    private final int mHeight;

    public Yuv(Bitmap bitmap, int format) {
        mFormat = format;
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        switch (format) {
            case ImageFormat.YV12:
                mYuv = new byte[(3 * mWidth * mHeight) / 2];
                fillArrayWithYV12(bitmap);
                return;
            case ImageFormat.NV21:
                mYuv = new byte[(3 * mWidth * mHeight) / 2];
                fillArrayWithNV21(bitmap);
                return;
            case ImageFormat.YUY2:
                mYuv = new byte[2 * mWidth * mHeight];
                fillArrayWithYUY2(bitmap);
                return;
            default:
                throw new IllegalArgumentException("Only support ImageFormat.YV12, ImageFormat.NV21 and ImageFormat.YUY2");
        }
    }

    private void fillArrayWithYV12(Bitmap bitmap) {
        int pos = 0;

        for (int h = 0; h < mHeight; ++h)
            for (int w = 0; w < mWidth; ++w)
                mYuv[pos++] = (byte) convertToY(bitmap.getPixel(w, h));

        for (int h = 0; h < mHeight; h += 2) {
            for (int w = 0; w < mWidth; w += 2) {
                int u0 = convertToU(bitmap.getPixel(w, h));
                int u1 = convertToU(bitmap.getPixel(w + 1, h));
                int u2 = convertToU(bitmap.getPixel(w, h + 1));
                int u3 = convertToU(bitmap.getPixel(w + 1, h + 1));
                mYuv[pos++] = (byte) ((u0 + u1 + u2 + u3) / 4);
            }
        }

        for (int h = 0; h < mHeight; h += 2) {
            for (int w = 0; w < mWidth; w += 2) {
                int v0 = convertToV(bitmap.getPixel(w, h));
                int v1 = convertToV(bitmap.getPixel(w + 1, h));
                int v2 = convertToV(bitmap.getPixel(w, h + 1));
                int v3 = convertToV(bitmap.getPixel(w + 1, h + 1));
                mYuv[pos++] = (byte) ((v0 + v1 + v2 + v3) / 4);
            }
        }
    }

    private void fillArrayWithNV21(Bitmap bitmap) {
        int pos = 0;

        for (int h = 0; h < mHeight; ++h)
            for (int w = 0; w < mWidth; ++w)
                mYuv[pos++] = (byte) convertToY(bitmap.getPixel(w, h));

        for (int h = 0; h < mHeight; h += 2) {
            for (int w = 0; w < mWidth; w += 2) {
                int v0 = convertToV(bitmap.getPixel(w, h));
                int v1 = convertToV(bitmap.getPixel(w + 1, h));
                int v2 = convertToV(bitmap.getPixel(w, h + 1));
                int v3 = convertToV(bitmap.getPixel(w + 1, h + 1));
                mYuv[pos++] = (byte) ((v0 + v1 + v2 + v3) / 4);
                int u0 = convertToU(bitmap.getPixel(w, h));
                int u1 = convertToU(bitmap.getPixel(w + 1, h));
                int u2 = convertToU(bitmap.getPixel(w, h + 1));
                int u3 = convertToU(bitmap.getPixel(w + 1, h + 1));
                mYuv[pos++] = (byte) ((u0 + u1 + u2 + u3) / 4);
            }
        }
    }

    private void fillArrayWithYUY2(Bitmap bitmap) {
        int pos = 0;

        for (int h = 0; h < mHeight; ++h) {
            for (int w = 0; w < mWidth; w += 2) {
                mYuv[pos++] = (byte) convertToY(bitmap.getPixel(w, h));
                int u0 = convertToU(bitmap.getPixel(w, h));
                int u1 = convertToU(bitmap.getPixel(w + 1, h));
                mYuv[pos++] = (byte) ((u0 + u1) / 2);
                mYuv[pos++] = (byte) convertToY(bitmap.getPixel(w + 1, h));
                int v0 = convertToV(bitmap.getPixel(w, h));
                int v1 = convertToV(bitmap.getPixel(w + 1, h));
                mYuv[pos++] = (byte) ((v0 + v1) / 2);
            }
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

    public int getY(int x, int y) {
        switch (mFormat) {
            case ImageFormat.YV12:
            case ImageFormat.NV21:
                return 255 & mYuv[mWidth * y + x];
            case ImageFormat.YUY2:
                return 255 & mYuv[2 * mWidth * y + 2 * x];
        }
        return 0;
    }

    public int getU(int x, int y) {
        switch (mFormat) {
            case ImageFormat.YV12:
                return 255 & mYuv[mWidth * mHeight + (mWidth >> 1) * (y >> 1) + (x >> 1)];
            case ImageFormat.NV21:
                return 255 & mYuv[mWidth * mHeight + mWidth * (y >> 1) + (x | 1)];
            case ImageFormat.YUY2:
                return 255 & mYuv[2 * mWidth * y + (((x & ~1) << 1) | 1)];
        }
        return 0;
    }

    public int getV(int x, int y) {
        switch (mFormat) {
            case ImageFormat.YV12:
                return 255 & mYuv[((5 * mWidth * mHeight) >> 2) + (mWidth >> 1) * (y >> 1) + (x >> 1)];
            case ImageFormat.NV21:
                return 255 & mYuv[mWidth * mHeight + mWidth * (y >> 1) + (x & ~1)];
            case ImageFormat.YUY2:
                return 255 & mYuv[2 * mWidth * y + ((x << 1) | 3)];
        }
        return 0;
    }

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
