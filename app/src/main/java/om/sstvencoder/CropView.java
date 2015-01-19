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

import java.io.IOException;
import java.io.InputStream;

import om.sstvencoder.Modes.ModeSize;

public class CropView extends ImageView {
    private ModeSize mModeSize;
    private Paint mPaint, mRectPaint;
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
            if (rect.width() < 64.0f || rect.width() < 64.0f)
                return true;

            int iw = mModeSize.getWidth();
            int ih = mModeSize.getHeight();
            int ow = mRegionDecoder.getWidth();
            int oh = mRegionDecoder.getHeight();

            if (rect.width() > ow && rect.height() > oh) {
                resetInputRect();
                return true;
            }
            if (rect.width() > ow || rect.height() > oh) {
                RectF tmp;
                if (iw * oh < ow * ih)
                    tmp = new RectF(0.0f, 0.0f, (iw * oh) / ih, oh);
                else
                    tmp = new RectF(0.0f, 0.0f, ow, (ih * ow) / iw);
                if (rect.width() > ow)
                    tmp.offset(0.0f, mInputRect.top);
                if (rect.height() > oh)
                    tmp.offset(mInputRect.left, 0.0f);
                mInputRect = tmp;
                return true;
            }
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
    }

    public void setModeSize(ModeSize size) {
        mModeSize = size;
        if (mRegionDecoder != null) {
            resetInputRect();
            invalidate();
        }
    }

    private void resetInputRect() {
        mInputRect = new RectF(getAspectRatioScaledRect(
                mModeSize.getWidth(), mModeSize.getHeight(),
                mRegionDecoder.getWidth(), mRegionDecoder.getHeight()));
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
                dx = Math.max(0.0f, mInputRect.left + dx) - mInputRect.left;
                dy = Math.max(0.0f, mInputRect.top + dy) - mInputRect.top;
                dx = Math.min(mRegionDecoder.getWidth(), mInputRect.right + dx) - mInputRect.right;
                dy = Math.min(mRegionDecoder.getHeight(), mInputRect.bottom + dy) - mInputRect.bottom;
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
        Bitmap bitmap = mRegionDecoder.decodeRegion(getPixelRect(mInputRect), getBitmapOptions());
        canvas.drawBitmap(bitmap, null, getModeRect(), mPaint);
        drawModeRect(canvas);
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

    private Rect getModeRect() {
        Rect rect;
        int iw = mModeSize.getWidth();
        int ih = mModeSize.getHeight();

        int ow = (8 * getWidth()) / 10;
        int oh = (8 * getHeight()) / 10;

        if (iw * oh < ow * ih) {
            rect = new Rect(0, 0, (iw * oh) / ih, oh);
            rect.offsetTo((getWidth() - (iw * oh) / ih) / 2, (getHeight() - (ih * oh) / ih) / 2);
        } else {
            rect = new Rect(0, 0, ow, (ih * ow) / iw);
            rect.offsetTo((getWidth() - (iw * ow) / iw) / 2, (getHeight() - (ih * ow) / iw) / 2);
        }
        return rect;
    }
}
