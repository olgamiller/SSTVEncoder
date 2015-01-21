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
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import om.sstvencoder.Modes.ModeSize;

public class CropView extends ImageView {
    private ModeSize mModeSize;
    private Paint mPaint, mRectPaint, mBorderPaint;
    private RectF mInputRect;
    private BitmapRegionDecoder mRegionDecoder;
    private float mPreviousX;
    private float mPreviousY;
    private int mActivePointerId;
    private ScaleGestureDetector mScaleDetector;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = 1.0f / detector.getScaleFactor();
            RectF rect = new RectF(mInputRect);
            float dx = 0.5f * mInputRect.width() * (1.0f - scale);
            float dy = 0.5f * mInputRect.height() * (1.0f - scale);
            rect.inset(dx, dy);
            int max = Math.max(mRegionDecoder.getWidth(), mRegionDecoder.getHeight());
            if (Math.min(rect.width(), rect.height()) < 64.0f ||
                    Math.max(rect.width(), rect.height()) > 2.0f * max)
                return true;
            mInputRect = rect;
            return true;
        }
    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mActivePointerId = MotionEvent.INVALID_POINTER_ID;
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mRectPaint = new Paint();
        mRectPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.BLACK);
    }

    public void setModeSize(ModeSize size) {
        mModeSize = size;
        if (mRegionDecoder != null) {
            resetInputRect();
            invalidate();
        }
    }

    private void resetInputRect() {
        float iw = mModeSize.getWidth();
        float ih = mModeSize.getHeight();
        float ow = mRegionDecoder.getWidth();
        float oh = mRegionDecoder.getHeight();
        if (iw * oh > ow * ih) {
            mInputRect = new RectF(0.0f, 0.0f, (iw * oh) / ih, oh);
            mInputRect.offset((ow - (iw * oh) / ih) / 2.0f, 0.0f);
        } else {
            mInputRect = new RectF(0.0f, 0.0f, ow, (ih * ow) / iw);
            mInputRect.offset(0.0f, (oh - (ih * ow) / iw) / 2.0f);
        }
    }

    public void setBitmapStream(InputStream stream) {
        try {
            mRegionDecoder = BitmapRegionDecoder.newInstance(stream, true);
            resetInputRect();
            invalidate();
        } catch (IOException ignore) {
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        mScaleDetector.onTouchEvent(e);

        switch (MotionEventCompat.getActionMasked(e)) {
            case MotionEvent.ACTION_DOWN: {
                int pointerIndex = MotionEventCompat.getActionIndex(e);
                mPreviousX = MotionEventCompat.getX(e, pointerIndex);
                mPreviousY = MotionEventCompat.getY(e, pointerIndex);
                mActivePointerId = MotionEventCompat.getPointerId(e, 0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int pointerIndex = MotionEventCompat.findPointerIndex(e, mActivePointerId);
                float x = MotionEventCompat.getX(e, pointerIndex);
                float y = MotionEventCompat.getY(e, pointerIndex);
                float dx = (mInputRect.width() * (mPreviousX - x)) / getWidth();
                float dy = (mInputRect.height() * (mPreviousY - y)) / getHeight();
                dx = Math.max(mInputRect.width() * 0.1f, mInputRect.right + dx) - mInputRect.right;
                dy = Math.max(mInputRect.height() * 0.1f, mInputRect.bottom + dy) - mInputRect.bottom;
                dx = Math.min(mRegionDecoder.getWidth() - mInputRect.width() * 0.1f, mInputRect.left + dx) - mInputRect.left;
                dy = Math.min(mRegionDecoder.getHeight() - mInputRect.height() * 0.1f, mInputRect.top + dy) - mInputRect.top;
                mInputRect.offset(dx, dy);
                mPreviousX = x;
                mPreviousY = y;
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = MotionEventCompat.getActionIndex(e);
                int pointerId = MotionEventCompat.getPointerId(e, pointerIndex);
                if (pointerId == mActivePointerId) {
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mPreviousX = MotionEventCompat.getX(e, newPointerIndex);
                    mPreviousY = MotionEventCompat.getY(e, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(e, newPointerIndex);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (mRegionDecoder == null)
            return;
        Pair<Rect, Rect> pair = getVisibleCanvasAndImageRect(getWidth(), getHeight());
        Bitmap bitmap = mRegionDecoder.decodeRegion(pair.first, getBitmapOptions());
        canvas.drawRect(getModeRect(), mBorderPaint);
        canvas.drawBitmap(bitmap, null, pair.second, mPaint);
        bitmap.recycle();
        drawModeRect(canvas);
    }

    private Pair<Rect, Rect> getVisibleCanvasAndImageRect(int width, int height) {
        RectF mode = new RectF(getModeRect());
        RectF image = new RectF(mInputRect);
        image.left -= mode.left * mInputRect.width() / mode.width();
        image.top -= mode.top * mInputRect.height() / mode.height();
        image.right -= (mode.right - getWidth()) * mInputRect.width() / mode.width();
        image.bottom -= (mode.bottom - getHeight()) * mInputRect.height() / mode.height();
        return getValidCanvasAndImageRect(image, width, height);
    }

    private Pair<Rect, Rect> getValidCanvasAndImageRect(RectF image, int width, int height) {
        RectF canvas = new RectF(0.0f, 0.0f, width, height);
        if (image.left < 0.0f) {
            canvas.left -= image.left * canvas.width() / image.width();
            image.left = 0.0f;
        }
        if (image.top < 0.0f) {
            canvas.top -= image.top * canvas.height() / image.height();
            image.top = 0.0f;
        }
        if (image.right > mRegionDecoder.getWidth()) {
            canvas.right -= (image.right - mRegionDecoder.getWidth()) * canvas.width() / image.width();
            image.right = mRegionDecoder.getWidth();
        }
        if (image.bottom > mRegionDecoder.getHeight()) {
            canvas.bottom -= (image.bottom - mRegionDecoder.getHeight()) * canvas.height() / image.height();
            image.bottom = mRegionDecoder.getHeight();
        }
        return new Pair<>(getPixelRect(image), getIntRect(canvas));
    }

    private void drawModeRect(Canvas canvas) {
        Rect rect = getModeRect();
        mRectPaint.setColor(Color.BLUE);
        canvas.drawRect(rect, mRectPaint);
        rect.inset(-1, -1);
        mRectPaint.setColor(Color.GREEN);
        canvas.drawRect(rect, mRectPaint);
        rect.inset(-1, -1);
        mRectPaint.setColor(Color.RED);
        canvas.drawRect(rect, mRectPaint);
    }

    private Rect getIntRect(RectF rect) {
        return new Rect((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom);
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
        Pair<Rect, Rect> pair = getValidCanvasAndImageRect(new RectF(mInputRect), mModeSize.getWidth(), mModeSize.getHeight());
        Bitmap bitmap = mRegionDecoder.decodeRegion(pair.first, getBitmapOptions());
        Bitmap result = Bitmap.createBitmap(mModeSize.getWidth(), mModeSize.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bitmap, null, pair.second, mPaint);
        bitmap.recycle();
        return result;
    }

    private Rect getModeRect() {
        Rect rect;
        int iw = mModeSize.getWidth();
        int ih = mModeSize.getHeight();

        int ow = (8 * getWidth()) / 10;
        int oh = (8 * getHeight()) / 10;

        if (iw * oh < ow * ih) {
            rect = new Rect(0, 0, (iw * oh) / ih, oh);
            rect.offset((getWidth() - (iw * oh) / ih) / 2, (getHeight() - oh) / 2);
        } else {
            rect = new Rect(0, 0, ow, (ih * ow) / iw);
            rect.offset((getWidth() - ow) / 2, (getHeight() - (ih * ow) / iw) / 2);
        }
        return rect;
    }
}
