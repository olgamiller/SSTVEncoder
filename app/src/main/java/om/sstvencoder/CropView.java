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
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import om.sstvencoder.Modes.ModeSize;

public class CropView extends ImageView {
    private ModeSize mModeSize;
    private RectF mInputRect;
    private BitmapRegionDecoder mRegionDecoder;
    private Bitmap mBitmap;
    private float mPreviousX;
    private float mPreviousY;
    private ScaleGestureDetector mScaleDetector;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = 1.0f / detector.getScaleFactor();
            float width = mInputRect.width() * scale;
            float height = mInputRect.height() * scale;
            if (width < 64.0f || height < 64.0f)
                return true;
            if (width > mRegionDecoder.getWidth() || height > mRegionDecoder.getHeight())
                return true;
            float dx = (mInputRect.width() - width) / 2.0f;
            float dy = (mInputRect.height() - height) / 2.0f;
            mInputRect.inset(dx, dy);
            return true;
        }
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void setModeSize(ModeSize size) {
        mModeSize = size;
        if (mBitmap != null)
            setImageBitmap(getBitmap());
    }

    public void setBitmapStream(InputStream stream) {
        try {
            mRegionDecoder = BitmapRegionDecoder.newInstance(stream, true);
            mInputRect = new RectF(0.0f, 0.0f, mRegionDecoder.getWidth(), mRegionDecoder.getHeight());
            update();
        } catch (IOException ignore) {
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mScaleDetector.onTouchEvent(e);
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float dx = (mInputRect.width() * (mPreviousX - x)) / getWidth();
                float dy = (mInputRect.height() * (mPreviousY - y)) / getHeight();
                dx = Math.max(0.0f, mInputRect.left + dx) - mInputRect.left;
                dy = Math.max(0.0f, mInputRect.top + dy) - mInputRect.top;
                dx = Math.min(mRegionDecoder.getWidth(), mInputRect.right + dx) - mInputRect.right;
                dy = Math.min(mRegionDecoder.getHeight(), mInputRect.bottom + dy) - mInputRect.bottom;
                mInputRect.offset(dx, dy);
                update();
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    public void update() {
        Rect rect = new Rect((int) mInputRect.left, (int) mInputRect.top, (int) mInputRect.right, (int) mInputRect.bottom);
        mBitmap = mRegionDecoder.decodeRegion(rect, getBitmapOptions());
        setImageBitmap(mBitmap);
    }

    private BitmapFactory.Options getBitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        int sx = Math.round(mInputRect.width() / mModeSize.getWidth());
        int sy = Math.round(mInputRect.height() / mModeSize.getHeight());
        int scale = Math.max(1, Math.max(sx, sy));
        options.inSampleSize = Integer.highestOneBit(scale);
        return options;
    }

    public Bitmap getBitmap() {
        return scaleBitmap(mBitmap, mModeSize.getWidth(), mModeSize.getHeight());
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
