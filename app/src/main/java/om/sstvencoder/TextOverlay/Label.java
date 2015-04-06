/*
Copyright 2015 Olga Miller <olga.rgb@googlemail.com>

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

package om.sstvencoder.TextOverlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import om.sstvencoder.Utility;

class Label {

    private interface IShadow {
        public void draw(Canvas canvas);
    }

    private class Shadow implements IShadow {
        @Override
        public void draw(Canvas canvas) {
            final float r = 10.0f;
            final float expansion = 2.0f;
            final RectF bounds = new RectF(
                    mBounds.left - expansion,
                    mBounds.top - expansion,
                    mBounds.right + expansion,
                    mBounds.bottom + expansion);

            int alpha = mPaint.getAlpha();
            int color = mPaint.getColor();
            Paint.Style style = mPaint.getStyle();

            mPaint.setColor(Color.LTGRAY);
            mPaint.setAlpha(100);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(bounds, r, r, mPaint);
            mPaint.setColor(Color.GREEN);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect(bounds, r, r, mPaint);

            //reset
            mPaint.setColor(color);
            mPaint.setAlpha(alpha);
            mPaint.setStyle(style);
        }
    }

    private class ShadowNull implements IShadow {
        @Override
        public void draw(Canvas canvas) {
        }
    }

    private class Geometry {
        float mX, mY, mW, mH;

        Geometry(float x, float y, float w, float h) {
            mX = x;
            mY = y;
            mW = w;
            mH = h;
        }

        public void offset(float x, float y) {
            mX += x;
            mY += y;
        }
    }

    private String mText;
    private Paint mPaint;
    private float mTextSize, mTextSizeFactor;
    private RectF mBounds;
    private Geometry mVertical, mHorizontal, mCurrent;
    private IShadow mShadow;

    public Label(float x, float y, float w, float h) {
        mCurrent = new Geometry(x, y, w, h);
        if (w < h)
            mVertical = mCurrent;
        else
            mHorizontal = mCurrent;
        mShadow = new ShadowNull();
        mText = "";
        mBounds = new RectF();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mTextSize = 2.0f;
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public LabelSettings getSettings() {
        LabelSettings settings = new LabelSettings();
        settings.Text = mText;
        settings.TextSize = mTextSize;
        return settings;
    }

    public void loadSettings(LabelSettings settings) {
        mText = settings.Text;
        mTextSize = settings.TextSize;
        setTextSizeFactor();
        adjustBounds();
    }

    public boolean contains(float x, float y) {
        return mBounds.contains(x, y);
    }

    public void drag() {
        mShadow = new Shadow();
    }

    public void drop() {
        mShadow = new ShadowNull();
    }

    public void move(float x, float y) {
        mCurrent.offset(x, y);
        if (mCurrent.mW > mCurrent.mH) {
            if (mVertical != null) {
                mVertical.mX = mHorizontal.mX - (mHorizontal.mW - mVertical.mW) / 2.0f;
                mVertical.mY = (mVertical.mH - mHorizontal.mH) / 2.0f + mHorizontal.mY;
            }
        } else if (mHorizontal != null) {
            mHorizontal.mX = (mHorizontal.mW - mVertical.mW) / 2.0f + mVertical.mX;
            mHorizontal.mY = mVertical.mY - (mVertical.mH - mHorizontal.mH) / 2.0f;
        }
        adjustBounds();
    }

    public void draw(Canvas canvas) {
        mShadow.draw(canvas);
        mPaint.setTextSize(mTextSize * mTextSizeFactor);
        canvas.drawText(mText, mCurrent.mX, mCurrent.mY, mPaint);
    }

    public void draw(Canvas canvas, Rect src, Rect dst) {
        float factor = (dst.height() / (float) src.height());
        mPaint.setTextSize(mTextSize * mTextSizeFactor * factor);
        canvas.drawText(mText, (mCurrent.mX - src.left) * factor, (mCurrent.mY - src.top) * factor, mPaint);
    }

    public void update(float w, float h) {
        setCurrentGeometry(w, h);
        setTextSizeFactor();
        adjustBounds();
    }

    private void setCurrentGeometry(float w, float h) {
        if (mVertical == null)
            mVertical = new Geometry(mHorizontal.mX - (mHorizontal.mW - w) / 2.0f, (h - mHorizontal.mH) / 2.0f + mHorizontal.mY, w, h);
        else if (mHorizontal == null)
            mHorizontal = new Geometry((w - mVertical.mW) / 2.0f + mVertical.mX, mVertical.mY - (mVertical.mH - h) / 2.0f, w, h);
        mCurrent = w < h ? mVertical : mHorizontal;
    }

    private void setTextSizeFactor() {
        Rect bounds = Utility.getEmbeddedRect((int) mCurrent.mW, (int) mCurrent.mH, 320, 240);
        mTextSizeFactor = 0.1f * bounds.height();
    }

    private void adjustBounds() {
        mBounds.set(getTextBounds(mText, mTextSize * mTextSizeFactor));
        mBounds.offset(mCurrent.mX, mCurrent.mY);
    }

    private Rect getTextBounds(String text, float textSize) {
        Rect bounds = new Rect();
        mPaint.setTextSize(textSize);
        mPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }
}
