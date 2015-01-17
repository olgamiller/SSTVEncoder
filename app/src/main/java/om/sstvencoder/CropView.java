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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import om.sstvencoder.Modes.ModeSize;

public class CropView extends ImageView {
    private ModeSize mNativeSize;
    private Bitmap mBitmap;

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setModeSize(ModeSize size) {
        mNativeSize = size;
        if (mBitmap != null)
            setImageBitmap(getBitmap());
    }

    public void setBitmapStream(InputStream stream) {
        int bufferBytes = 128 * 1024;
        if (!stream.markSupported())
            stream = new BufferedInputStream(stream, bufferBytes);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        stream.mark(bufferBytes);
        BitmapFactory.decodeStream(new BufferedInputStream(stream), null, options);
        int scale = Math.max(1, Math.max(options.outWidth / 320, options.outHeight / 256));
        options.inSampleSize = Integer.highestOneBit(scale);
        options.inJustDecodeBounds = false;
        try {
            stream.reset();
            mBitmap = BitmapFactory.decodeStream(stream, null, options);
            setImageBitmap(getBitmap());
        } catch (IOException ignore) {
        }
    }

    public Bitmap getBitmap() {
        return scaleBitmap(mBitmap, mNativeSize.getWidth(), mNativeSize.getHeight());
    }

    private Bitmap scaleBitmap(Bitmap bmp, int ow, int oh) {
        Bitmap result = Bitmap.createBitmap(ow, oh, Bitmap.Config.ARGB_8888);
        int iw = bmp.getWidth();
        int ih = bmp.getHeight();
        Rect rect;

        if (iw * oh < ow * ih) {
            rect = new Rect(0, 0, (iw * oh) / ih, oh);
            rect.offsetTo((ow - (iw * oh) / ih) / 2, 0);
        } else {
            rect = new Rect(0, 0, ow, (ih * ow) / iw);
            rect.offsetTo(0, (oh - (ih * ow) / iw) / 2);
        }

        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bmp, null, rect, new Paint(Paint.FILTER_BITMAP_FLAG));

        return result;
    }
}
