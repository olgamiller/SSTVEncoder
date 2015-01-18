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
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import om.sstvencoder.Modes.ModeSize;

public class CropView extends ImageView {
    private ModeSize mModeSize;
    private Paint mPaint;
    private RectF mInputRect;
    private BitmapRegionDecoder mRegionDecoder;
    private float mPreviousX;
    private float mPreviousY;
    private ScaleGestureDetector mScaleDetector;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = 1.0f / detector.getScaleFactor();
            RectF rect = new RectF(mInputRect);
            float dx = 0.5f * mInputRect.width() * (1.0f - scale);
            float dy = 0.5f * mInputRect.height() * (1.0f - scale);
            rect.inset(dx, dy);
            if (rect.width() < 64.0f || rect.width() < 64.0f)
                return true;
            if (rect.width() > mRegionDecoder.getWidth() ||
                    rect.height() > mRegionDecoder.getHeight())
                mInputRect = new RectF(0.0f, 0.0f, mRegionDecoder.getWidth(), mRegionDecoder.getHeight());
            else
                mInputRect = rect;
            return true;
        }
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }

    public void setModeSize(ModeSize size) {
        mModeSize = size;
        invalidate();
    }

    public void setBitmapStream(InputStream stream) {
        try {
            mRegionDecoder = BitmapRegionDecoder.newInstance(stream, true);
            mInputRect = new RectF(0.0f, 0.0f, mRegionDecoder.getWidth(), mRegionDecoder.getHeight());
            invalidate();
        } catch (IOException ignore) {
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
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
                invalidate();
        }
        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (mRegionDecoder == null)
            return;
        Bitmap bitmap = mRegionDecoder.decodeRegion(getPixelRect(mInputRect), getBitmapOptions());
        canvas.drawBitmap(bitmap, null, getAspectRatioScaledRect(mInputRect, getWidth(), getHeight()), mPaint);
    }

    private Rect getPixelRect(RectF rect) {
        return new Rect((int) rect.left, (int) rect.top, (int) rect.right - 1, (int) rect.bottom - 1);
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
        Bitmap bitmap = mRegionDecoder.decodeRegion(getPixelRect(mInputRect), getBitmapOptions());

        int iw = bitmap.getWidth();
        int ih = bitmap.getHeight();
        int ow = mModeSize.getWidth();
        int oh = mModeSize.getHeight();
        Rect rect = getAspectRatioScaledRect(iw, ih, ow, oh);

        Bitmap result = Bitmap.createBitmap(ow, oh, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bitmap, null, rect, mPaint);

        return result;
    }

    private Rect getAspectRatioScaledRect(RectF in, int ow, int oh) {
        return getAspectRatioScaledRect((int) in.width(), (int) in.height(), ow, oh);
    }

    private Rect getAspectRatioScaledRect(int iw, int ih, int ow, int oh) {
        Rect rect;
        if (iw * oh < ow * ih) {
            rect = new Rect(0, 0, (iw * oh) / ih, oh);
            rect.offsetTo((ow - (iw * oh) / ih) / 2, (oh - (ih * oh) / ih) / 2);
        } else {
            rect = new Rect(0, 0, ow, (ih * ow) / iw);
            rect.offsetTo((ow - (iw * ow) / iw) / 2, (oh - (ih * ow) / iw) / 2);
        }
        return rect;
    }
}
