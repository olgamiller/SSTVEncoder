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
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import om.sstvencoder.Modes.ModeSize;

public class CropView extends ImageView {
    private ModeSize mModeSize;
    private final Paint mPaint, mRectPaint, mBorderPaint;
    private RectF mInputRect;
    private Rect mOutputRect;
    private BitmapRegionDecoder mRegionDecoder;
    private int mImageWidth, mImageHeight;
    private float mPreviousX, mPreviousY;
    private int mActivePointerId;
    private ScaleGestureDetector mScaleDetector;
    private Bitmap mBitmap;
    private boolean mSmallImage;
    private boolean mImageOK;
    private final Rect mCanvasDrawRect, mImageDrawRect;
    private int mOrientation;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            float newW = mInputRect.width() / scale;
            float newH = mInputRect.height() / scale;
            float dx = 0.5f * (mInputRect.width() - newW);
            float dy = 0.5f * (mInputRect.height() - newH);
            float max = 2.0f * Math.max(mImageWidth, mImageHeight);
            if (Math.min(newW, newH) >= 4.0f && Math.max(newW, newH) <= max)
                mInputRect.inset(dx, dy);
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

        mCanvasDrawRect = new Rect();
        mImageDrawRect = new Rect();

        mSmallImage = false;
        mImageOK = false;
    }

    public void setModeSize(ModeSize size) {
        mModeSize = size;
        mOutputRect = getModeRect(getWidth(), getHeight());
        if (mImageOK) {
            resetInputRect();
            invalidate();
        }
    }

    private void resetInputRect() {
        float iw = mModeSize.getWidth();
        float ih = mModeSize.getHeight();
        float ow = mImageWidth;
        float oh = mImageHeight;
        if (iw * oh > ow * ih) {
            mInputRect = new RectF(0.0f, 0.0f, (iw * oh) / ih, oh);
            mInputRect.offset((ow - (iw * oh) / ih) / 2.0f, 0.0f);
        } else {
            mInputRect = new RectF(0.0f, 0.0f, ow, (ih * ow) / iw);
            mInputRect.offset(0.0f, (oh - (ih * ow) / iw) / 2.0f);
        }
    }

    public void rotateImage(int orientation) {
        mOrientation += orientation;
        mOrientation %= 360;
        if (orientation == 90 || orientation == 270) {
            int tmp = mImageWidth;
            mImageWidth = mImageHeight;
            mImageHeight = tmp;
        }
        if (mImageOK) {
            resetInputRect();
            invalidate();
        }
    }

    public void setBitmapStream(InputStream stream) {
        mImageOK = false;
        mOrientation = 0;
        try {
            if (mRegionDecoder != null) {
                mRegionDecoder.recycle();
                mRegionDecoder = null;
            }
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
            int bufferBytes = 128 * 1024;
            if (!stream.markSupported())
                stream = new BufferedInputStream(stream, bufferBytes);
            stream.mark(bufferBytes);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new BufferedInputStream(stream), null, options);
            stream.reset();
            mImageWidth = options.outWidth;
            mImageHeight = options.outHeight;

            if (mImageWidth * mImageHeight < 1024 * 1024) {
                mBitmap = BitmapFactory.decodeStream(stream);
                mSmallImage = true;
            } else {
                mRegionDecoder = BitmapRegionDecoder.newInstance(stream, true);
                mSmallImage = false;
            }
            mImageOK = true;
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
                float dx = (mInputRect.width() * (mPreviousX - x)) / mOutputRect.width();
                float dy = (mInputRect.height() * (mPreviousY - y)) / mOutputRect.height();
                dx = Math.max(mInputRect.width() * 0.1f, mInputRect.right + dx) - mInputRect.right;
                dy = Math.max(mInputRect.height() * 0.1f, mInputRect.bottom + dy) - mInputRect.bottom;
                dx = Math.min(mImageWidth - mInputRect.width() * 0.1f, mInputRect.left + dx) - mInputRect.left;
                dy = Math.min(mImageHeight - mInputRect.height() * 0.1f, mInputRect.top + dy) - mInputRect.top;
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mOutputRect = getModeRect(w, h);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (!mImageOK)
            return;

        maximizeImageToCanvasRect();
        adjustCanvasAndImageRect(getWidth(), getHeight());
        canvas.drawRect(mOutputRect, mBorderPaint);
        drawBitmap(canvas);
        drawModeRect(canvas);
    }

    private void maximizeImageToCanvasRect() {
        mImageDrawRect.left = (int) (mInputRect.left - mOutputRect.left * mInputRect.width() / mOutputRect.width());
        mImageDrawRect.top = (int) (mInputRect.top - mOutputRect.top * mInputRect.height() / mOutputRect.height());
        mImageDrawRect.right = (int) (mInputRect.right - (mOutputRect.right - getWidth()) * mInputRect.width() / mOutputRect.width());
        mImageDrawRect.bottom = (int) (mInputRect.bottom - (mOutputRect.bottom - getHeight()) * mInputRect.height() / mOutputRect.height());
    }

    private void adjustCanvasAndImageRect(int width, int height) {
        mCanvasDrawRect.set(0, 0, width, height);
        if (mImageDrawRect.left < 0) {
            mCanvasDrawRect.left -= (mImageDrawRect.left * mCanvasDrawRect.width()) / mImageDrawRect.width();
            mImageDrawRect.left = 0;
        }
        if (mImageDrawRect.top < 0) {
            mCanvasDrawRect.top -= (mImageDrawRect.top * mCanvasDrawRect.height()) / mImageDrawRect.height();
            mImageDrawRect.top = 0;
        }
        if (mImageDrawRect.right > mImageWidth) {
            mCanvasDrawRect.right -= ((mImageDrawRect.right - mImageWidth) * mCanvasDrawRect.width()) / mImageDrawRect.width();
            mImageDrawRect.right = mImageWidth;
        }
        if (mImageDrawRect.bottom > mImageHeight) {
            mCanvasDrawRect.bottom -= ((mImageDrawRect.bottom - mImageHeight) * mCanvasDrawRect.height()) / mImageDrawRect.height();
            mImageDrawRect.bottom = mImageHeight;
        }
    }

    private void drawModeRect(Canvas canvas) {
        mRectPaint.setColor(Color.BLUE);
        canvas.drawRect(mOutputRect, mRectPaint);
        mRectPaint.setColor(Color.GREEN);
        drawRectInset(canvas, mOutputRect, -1);
        mRectPaint.setColor(Color.RED);
        drawRectInset(canvas, mOutputRect, -2);
    }

    private void drawRectInset(Canvas canvas, Rect rect, int inset) {
        canvas.drawRect(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset, mRectPaint);
    }

    private Rect getIntRect(RectF rect) {
        return new Rect((int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom);
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
        if (!mImageOK)
            return null;

        Bitmap result = Bitmap.createBitmap(mModeSize.getWidth(), mModeSize.getHeight(), Bitmap.Config.ARGB_8888);
        mImageDrawRect.set(getIntRect(mInputRect));
        adjustCanvasAndImageRect(mModeSize.getWidth(), mModeSize.getHeight());

        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.BLACK);
        drawBitmap(canvas);

        return result;
    }

    private void drawBitmap(Canvas canvas) {
        int w = mImageWidth;
        int h = mImageHeight;
        for (int i = 0; i < mOrientation / 90; ++i) {
            int tmp = w;
            w = h;
            h = tmp;
            mImageDrawRect.set(mImageDrawRect.top, h - mImageDrawRect.left, mImageDrawRect.bottom, h - mImageDrawRect.right);
            mCanvasDrawRect.set(mCanvasDrawRect.top, -mCanvasDrawRect.right, mCanvasDrawRect.bottom, -mCanvasDrawRect.left);
        }
        mImageDrawRect.sort();
        canvas.save();
        canvas.rotate(mOrientation);
        if (mSmallImage) {
            canvas.drawBitmap(mBitmap, mImageDrawRect, mCanvasDrawRect, mPaint);
        } else {
            Bitmap bitmap = mRegionDecoder.decodeRegion(mImageDrawRect, getBitmapOptions());
            canvas.drawBitmap(bitmap, null, mCanvasDrawRect, mPaint);
            bitmap.recycle();
        }
        canvas.restore();
    }

    private Rect getModeRect(int w, int h) {
        Rect rect;
        int iw = mModeSize.getWidth();
        int ih = mModeSize.getHeight();

        int ow = (9 * w) / 10;
        int oh = (9 * h) / 10;

        if (iw * oh < ow * ih) {
            rect = new Rect(0, 0, (iw * oh) / ih, oh);
            rect.offset((w - (iw * oh) / ih) / 2, (h - oh) / 2);
        } else {
            rect = new Rect(0, 0, ow, (ih * ow) / iw);
            rect.offset((w - ow) / 2, (h - (ih * ow) / iw) / 2);
        }
        return rect;
    }
}
