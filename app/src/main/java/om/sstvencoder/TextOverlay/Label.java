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
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import om.sstvencoder.Utility;

class Label {

    private interface IDrawer {
        void draw(Canvas canvas);

        void draw(Canvas canvas, Rect src, Rect dst);
    }

    private class InsideDrawer implements IDrawer {
        @Override
        public void draw(Canvas canvas) {
            setPaintSettings();
            mPaint.setTextSize(mTextSize * mTextSizeFactor);
            canvas.drawText(mText, mCurrent.mX, mCurrent.mY, mPaint);
        }

        @Override
        public void draw(Canvas canvas, Rect src, Rect dst) {
            float factor = (dst.height() / (float) src.height());
            setPaintSettings();
            mPaint.setTextSize(mTextSize * mTextSizeFactor * factor);
            canvas.drawText(mText, (mCurrent.mX - src.left) * factor, (mCurrent.mY - src.top) * factor, mPaint);
        }

        private void setPaintSettings() {
            mPaint.setColor(mColor);
            mPaint.setAlpha(255);
            mPaint.setStyle(Paint.Style.FILL);
        }
    }

    private class OutsideDrawer implements IDrawer {
        @Override
        public void draw(Canvas canvas) {
            int color = mPaint.getColor();
            Paint.Style style = mPaint.getStyle();

            mPaint.setColor(mColor);
            mPaint.setAlpha(255);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mPath, mPaint);

            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);

            mPaint.setColor(color);
            mPaint.setStyle(style);
        }

        @Override
        public void draw(Canvas canvas, Rect src, Rect dst) {
        }
    }

    private interface IShadow {
        void setCircle(float x, float y, float r);

        void setInside(boolean inside);

        void draw(Canvas canvas);
    }

    private class Shadow implements IShadow {
        private float mX, mY, mR;
        private boolean mInside;

        @Override
        public void setCircle(float x, float y, float r) {
            mX = x;
            mY = y;
            mR = r;
        }

        @Override
        public void setInside(boolean inside) {
            mInside = inside;
        }

        @Override
        public void draw(Canvas canvas) {
            mPaint.setColor(Color.LTGRAY);
            mPaint.setAlpha(100);
            mPaint.setStyle(Paint.Style.FILL);
            drawShadow(canvas, 0.0f);

            mPaint.setAlpha(255);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.RED);
            drawShadow(canvas, 1.0f);
            mPaint.setColor(Color.GREEN);
            drawShadow(canvas, 0.0f);
            mPaint.setColor(Color.BLUE);
            drawShadow(canvas, -1.0f);
        }

        private void drawShadow(Canvas canvas, float dx) {
            if (mInside) {
                RectF bounds = new RectF(mBounds);
                bounds.inset(-4.0f - dx, -4.0f - dx);
                canvas.drawRoundRect(bounds, 10.0f, 10.0f, mPaint);
            } else {
                canvas.drawCircle(mX, mY, mR + dx, mPaint);
            }
        }
    }

    private class ShadowNull implements IShadow {
        @Override
        public void setCircle(float x, float y, float r) {
        }

        @Override
        public void setInside(boolean inside) {
        }

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

        public void set(float x, float y) {
            mX = x;
            mY = y;
        }
    }

    private String mText;
    private int mColor;
    private Paint mPaint;
    private float mTextSize, mTextSizeFactor;
    private RectF mBounds;
    private Geometry mVertical, mHorizontal, mCurrent;
    private IShadow mShadow;
    private IDrawer mDrawer;
    private Path mPath;

    public Label(float x, float y, float w, float h) {
        mCurrent = new Geometry(x, y, w, h);
        if (w < h)
            mVertical = mCurrent;
        else
            mHorizontal = mCurrent;
        mShadow = new ShadowNull();
        mDrawer = new InsideDrawer();

        mText = "";
        mTextSize = 2.0f;
        mColor = Color.BLACK;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        mBounds = new RectF();
    }

    public LabelSettings getSettings() {
        LabelSettings settings = new LabelSettings();
        settings.setText(mText);
        settings.TextSize = mTextSize;
        return settings;
    }

    public void loadSettings(LabelSettings settings) {
        mText = settings.getText();
        mTextSize = settings.TextSize;
        setTextSizeFactor();
        adjust();
    }

    public boolean contains(float x, float y) {
        return mBounds.contains(x, y);
    }

    public void draw(Canvas canvas) {
        mShadow.draw(canvas);
        mDrawer.draw(canvas);
    }

    public void draw(Canvas canvas, Rect src, Rect dst) {
        mDrawer.draw(canvas, src, dst);
    }

    public void drag() {
        mShadow = new Shadow();
        moveToBorder();
    }

    public void drop() {
        mShadow = new ShadowNull();
    }

    public void move(float x, float y) {
        mCurrent.offset(x, y);
        adjustGeometry();
        adjust();
    }

    private void adjustGeometry() {
        if (mCurrent.mW > mCurrent.mH) {
            if (mVertical != null) {
                mVertical.mX = mHorizontal.mX - (mHorizontal.mW - mVertical.mW) / 2.0f;
                mVertical.mY = (mVertical.mH - mHorizontal.mH) / 2.0f + mHorizontal.mY;
            }
        } else if (mHorizontal != null) {
            mHorizontal.mX = (mHorizontal.mW - mVertical.mW) / 2.0f + mVertical.mX;
            mHorizontal.mY = mVertical.mY - (mVertical.mH - mHorizontal.mH) / 2.0f;
        }
    }

    private void moveToBorder() {
        if (mDrawer instanceof OutsideDrawer) {
            float x, y;
            float m = getTextBounds("M", 2.0f * mTextSizeFactor).width();
            mBounds.set(getTextBounds(mText, mTextSize * mTextSizeFactor));
            mBounds.offset(mCurrent.mX, mCurrent.mY);

            if (mCurrent.mX + mBounds.width() < m)
                x = mCurrent.mX - mBounds.right + m / 2.0f;
            else if (mCurrent.mW - mCurrent.mX >= m)
                x = mCurrent.mX;
            else
                x = mCurrent.mW + mBounds.left - mCurrent.mX - m / 2.0f;

            if (mBounds.top + mBounds.height() < m)
                y = mCurrent.mY - mBounds.top - m / 2.0f;
            else if (mCurrent.mH - mBounds.top >= m)
                y = mCurrent.mY;
            else
                y = mCurrent.mH + mCurrent.mY - mBounds.bottom + m / 2.0f;

            mCurrent.set(x, y);
            adjustGeometry();
        }
        adjust();
    }

    public void update(float w, float h) {
        setCurrentGeometry(w, h);
        setTextSizeFactor();
        adjust();
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

    private void adjust() {
        mBounds.set(getTextBounds(mText, mTextSize * mTextSizeFactor));
        mBounds.offset(mCurrent.mX, mCurrent.mY);

        float m = getTextBounds("M", 2.0f * mTextSizeFactor).width();
        boolean leftOut = mBounds.right < m;
        boolean topOut = mBounds.bottom < m;
        boolean rightOut = mCurrent.mW - mBounds.left < m;
        boolean bottomOut = mCurrent.mH - mBounds.top < m;

        boolean inside = !(leftOut || topOut || rightOut || bottomOut);
        mShadow.setInside(inside);

        if (inside) {
            mDrawer = new InsideDrawer();
            mPath = null;
        } else {
            mDrawer = new OutsideDrawer();
            if (mPath == null)
                mPath = new Path();
            else
                mPath.reset();

            float half = m / 2.0f;
            float halfWidth = Math.min(Math.max((mBounds.left + mBounds.right) / 2.0f, half), mCurrent.mW - half);
            float halfHeight = Math.min(Math.max((mBounds.top + mBounds.bottom) / 2.0f, half), mCurrent.mH - half);

            if (leftOut) {
                if (topOut && (m - mBounds.right > m - mBounds.bottom))
                    setTopOut(m, half, halfWidth);
                else if (bottomOut && (m - mBounds.right > m - mCurrent.mH + mBounds.top))
                    setBottomOut(m, half, halfWidth);
                else
                    setLeftOut(m, half, halfHeight);
            } else if (topOut) {
                if (rightOut && (m - mBounds.bottom > m - mCurrent.mW + mBounds.left))
                    setRightOut(m, half, halfHeight);
                else
                    setTopOut(m, half, halfWidth);
            } else if (rightOut) {
                if (bottomOut && (m - mCurrent.mW + mBounds.left > m - mCurrent.mH + mBounds.top))
                    setBottomOut(m, half, halfWidth);
                else
                    setRightOut(m, half, halfHeight);
            } else {
                setBottomOut(m, half, halfWidth);
            }
        }
    }

    private Rect getTextBounds(String text, float textSize) {
        Rect bounds = new Rect();
        mPaint.setTextSize(textSize);
        mPaint.getTextBounds(text, 0, text.length(), bounds);
        return bounds;
    }

    private void setLeftOut(float m, float half, float halfHeight) {
        mPath.moveTo(0.0f, halfHeight - half);
        mPath.lineTo(0.0f, halfHeight + half);
        mPath.lineTo(half / 2.0f, halfHeight);
        mPath.lineTo(0.0f, halfHeight - half);
        mBounds.set(0.0f, halfHeight - half, m, halfHeight + half);
        mShadow.setCircle(0.0f, halfHeight, m);
    }

    private void setTopOut(float m, float half, float halfWidth) {
        mPath.moveTo(halfWidth - half, 0.0f);
        mPath.lineTo(halfWidth + half, 0.0f);
        mPath.lineTo(halfWidth, half / 2.0f);
        mPath.lineTo(halfWidth - half, 0.0f);
        mBounds.set(halfWidth - half, 0.0f, halfWidth + half, m);
        mShadow.setCircle(halfWidth, 0.0f, m);
    }

    private void setRightOut(float m, float half, float halfHeight) {
        mPath.moveTo(mCurrent.mW, halfHeight - half);
        mPath.lineTo(mCurrent.mW, halfHeight + half);
        mPath.lineTo(mCurrent.mW - half / 2.0f, halfHeight);
        mPath.lineTo(mCurrent.mW, halfHeight - half);
        mBounds.set(mCurrent.mW - m, halfHeight - half, mCurrent.mW, halfHeight + half);
        mShadow.setCircle(mCurrent.mW, halfHeight, m);
    }

    private void setBottomOut(float m, float half, float halfWidth) {
        mPath.moveTo(halfWidth - half, mCurrent.mH);
        mPath.lineTo(halfWidth + half, mCurrent.mH);
        mPath.lineTo(halfWidth, mCurrent.mH - half / 2.0f);
        mPath.lineTo(halfWidth - half, mCurrent.mH);
        mBounds.set(halfWidth - half, mCurrent.mH - m, halfWidth + half, mCurrent.mH);
        mShadow.setCircle(halfWidth, mCurrent.mH, m);
    }
}
