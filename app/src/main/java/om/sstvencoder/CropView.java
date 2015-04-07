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
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import om.sstvencoder.Modes.ModeSize;
import om.sstvencoder.TextOverlay.*;

public class CropView extends ImageView {

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mLongPress) {
                moveImage(distanceX, distanceY);
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mLongPress = false;
            if (!mInScale && mLabelHandler.dragLabel(e.getX(), e.getY())) {
                // Utility.vibrate(100, getContext());
                invalidate();
                mLongPress = true;
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!mLongPress) {
                sendLabelSettings(e.getX(), e.getY());
                return true;
            }
            return false;
        }
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (!mLongPress) {
                mInScale = true;
                return true;
            }
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleImage(detector.getScaleFactor());
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mInScale = false;
        }
    }

    private GestureDetectorCompat mDetectorCompat;
    private ScaleGestureDetector mScaleDetector;
    private boolean mLongPress, mInScale;
    private ModeSize mModeSize;
    private final Paint mPaint, mRectPaint, mBorderPaint;
    private RectF mInputRect;
    private Rect mOutputRect;
    private BitmapRegionDecoder mRegionDecoder;
    private int mImageWidth, mImageHeight;
    private Bitmap mCacheBitmap;
    private boolean mSmallImage;
    private boolean mImageOK;
    private final Rect mCanvasDrawRect, mImageDrawRect;
    private int mOrientation;
    private Rect mCacheRect;
    private int mCacheSampleSize;
    private final BitmapFactory.Options mBitmapOptions;
    private LabelHandler mLabelHandler;

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDetectorCompat = new GestureDetectorCompat(getContext(), new GestureListener());
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureListener());

        mBitmapOptions = new BitmapFactory.Options();

        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mRectPaint = new Paint();
        mRectPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint = new Paint();
        mBorderPaint.setColor(Color.BLACK);

        mCanvasDrawRect = new Rect();
        mImageDrawRect = new Rect();
        mCacheRect = new Rect();

        mSmallImage = false;
        mImageOK = false;

        mLabelHandler = new LabelHandler();
    }

    public void setModeSize(ModeSize size) {
        mModeSize = size;
        mOutputRect = Utility.getEmbeddedRect(getWidth(), getHeight(), mModeSize.getWidth(), mModeSize.getHeight());
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
            if (mCacheBitmap != null) {
                mCacheBitmap.recycle();
                mCacheBitmap = null;
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
                mCacheBitmap = BitmapFactory.decodeStream(stream);
                mSmallImage = true;
            } else {
                mRegionDecoder = BitmapRegionDecoder.newInstance(stream, true);
                mCacheRect.setEmpty();
                mSmallImage = false;
            }
            mImageOK = true;
            resetInputRect();
            invalidate();
        } catch (IOException ignore) {
        }
    }

    public void scaleImage(float scaleFactor) {
        float newW = mInputRect.width() / scaleFactor;
        float newH = mInputRect.height() / scaleFactor;
        float dx = 0.5f * (mInputRect.width() - newW);
        float dy = 0.5f * (mInputRect.height() - newH);
        float max = 2.0f * Math.max(mImageWidth, mImageHeight);
        if (Math.min(newW, newH) >= 4.0f && Math.max(newW, newH) <= max) {
            mInputRect.inset(dx, dy);
            invalidate();
        }
    }

    public void moveImage(float distanceX, float distanceY) {
        float dx = (mInputRect.width() * distanceX) / mOutputRect.width();
        float dy = (mInputRect.height() * distanceY) / mOutputRect.height();
        dx = Math.max(mInputRect.width() * 0.1f, mInputRect.right + dx) - mInputRect.right;
        dy = Math.max(mInputRect.height() * 0.1f, mInputRect.bottom + dy) - mInputRect.bottom;
        dx = Math.min(mImageWidth - mInputRect.width() * 0.1f, mInputRect.left + dx) - mInputRect.left;
        dy = Math.min(mImageHeight - mInputRect.height() * 0.1f, mInputRect.top + dy) - mInputRect.top;
        mInputRect.offset(dx, dy);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        if (mLongPress) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    mLabelHandler.moveLabel(e.getX(), e.getY());
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    mLabelHandler.dropLabel(e.getX(), e.getY());
                    invalidate();
                    mLongPress = false;
                    return true;
            }
        }
        boolean consumed = mScaleDetector.onTouchEvent(e);
        return mDetectorCompat.onTouchEvent(e) || consumed || super.onTouchEvent(e);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mOutputRect = Utility.getEmbeddedRect(w, h, mModeSize.getWidth(), mModeSize.getHeight());
        mLabelHandler.update(w, h);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (!mImageOK)
            return;

        maximizeImageToCanvasRect();
        adjustCanvasAndImageRect(getWidth(), getHeight());
        canvas.drawRect(mOutputRect, mBorderPaint);
        drawBitmap(canvas);
        mLabelHandler.drawLabels(canvas);
        drawModeRect(canvas);
    }

    private void maximizeImageToCanvasRect() {
        mImageDrawRect.left = Math.round(mInputRect.left - mOutputRect.left * mInputRect.width() / mOutputRect.width());
        mImageDrawRect.top = Math.round(mInputRect.top - mOutputRect.top * mInputRect.height() / mOutputRect.height());
        mImageDrawRect.right = Math.round(mInputRect.right - (mOutputRect.right - getWidth()) * mInputRect.width() / mOutputRect.width());
        mImageDrawRect.bottom = Math.round(mInputRect.bottom - (mOutputRect.bottom - getHeight()) * mInputRect.height() / mOutputRect.height());
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
        return new Rect(Math.round(rect.left), Math.round(rect.top), Math.round(rect.right), Math.round(rect.bottom));
    }

    private int getSampleSize() {
        int sx = Math.round(mInputRect.width() / mModeSize.getWidth());
        int sy = Math.round(mInputRect.height() / mModeSize.getHeight());
        int scale = Math.max(1, Math.max(sx, sy));
        return Integer.highestOneBit(scale);
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
        mLabelHandler.drawLabels(canvas, mOutputRect, new Rect(0, 0, mModeSize.getWidth(), mModeSize.getHeight()));

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
        if (!mSmallImage) {
            int sampleSize = getSampleSize();
            if (sampleSize < mCacheSampleSize || !mCacheRect.contains(mImageDrawRect)) {
                if (mCacheBitmap != null)
                    mCacheBitmap.recycle();
                int cacheWidth = mImageDrawRect.width();
                int cacheHeight = mImageDrawRect.height();
                while (cacheWidth * cacheHeight < (sampleSize * 1024 * sampleSize * 1024)) {
                    cacheWidth += mImageDrawRect.width();
                    cacheHeight += mImageDrawRect.height();
                }
                mCacheRect.set(
                        Math.max(0, ~(sampleSize - 1) & (mImageDrawRect.centerX() - cacheWidth / 2)),
                        Math.max(0, ~(sampleSize - 1) & (mImageDrawRect.centerY() - cacheHeight / 2)),
                        Math.min(mRegionDecoder.getWidth(), ~(sampleSize - 1) & (mImageDrawRect.centerX() + cacheWidth / 2 + sampleSize - 1)),
                        Math.min(mRegionDecoder.getHeight(), ~(sampleSize - 1) & (mImageDrawRect.centerY() + cacheHeight / 2 + sampleSize - 1)));
                mBitmapOptions.inSampleSize = mCacheSampleSize = sampleSize;
                mCacheBitmap = mRegionDecoder.decodeRegion(mCacheRect, mBitmapOptions);
            }
            mImageDrawRect.offset(-mCacheRect.left, -mCacheRect.top);
            mImageDrawRect.left /= mCacheSampleSize;
            mImageDrawRect.top /= mCacheSampleSize;
            mImageDrawRect.right /= mCacheSampleSize;
            mImageDrawRect.bottom /= mCacheSampleSize;
        }
        canvas.drawBitmap(mCacheBitmap, mImageDrawRect, mCanvasDrawRect, mPaint);
        canvas.restore();
    }

    private void sendLabelSettings(float x, float y) {
        LabelSettings settings = mLabelHandler.editLabelBegin(x, y, getWidth(), getHeight());
        ((MainActivity) getContext()).startEditTextActivity(settings);
    }

    public void loadLabelSettings(LabelSettings settings) {
        if (mLabelHandler.editLabelEnd(settings))
            invalidate();
    }
}
