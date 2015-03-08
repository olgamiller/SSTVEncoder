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

class Label {
    private class Geometry {
        float mX, mY, mW, mH;

        Geometry(float x, float y, float w, float h) {
            mX = x;
            mY = y;
            mW = w;
            mH = h;
        }
    }

    private String mText;
    private Paint mPaint;
    private RectF mBounds;
    private Geometry mVertical, mHorizontal, mCurrent;

    public Label(float x, float y, float w, float h) {
        mCurrent = new Geometry(x, y, w, h);
        if (w < h)
            mVertical = mCurrent;
        else
            mHorizontal = mCurrent;
        mText = "";
        mBounds = new RectF();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(130.0f);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public LabelSettings getSettings() {
        LabelSettings settings = new LabelSettings();
        settings.Text = mText;
        settings.TextSize = mPaint.getTextSize();
        return settings;
    }

    public void loadSettings(LabelSettings settings) {
        mText = settings.Text;
        mPaint.setTextSize(settings.TextSize);
        adjustBounds();
    }

    public boolean contains(float x, float y) {
        return mBounds.contains(x, y);
    }

    public void draw(Canvas canvas) {
        //mPaint.setStyle(Paint.Style.STROKE);
        //mPaint.setColor(Color.GREEN);
        //canvas.drawRect(mBounds, mPaint);
        //mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setColor(Color.BLACK);
        canvas.drawText(mText, mCurrent.mX, mCurrent.mY, mPaint);
    }

    public void draw(Canvas canvas, Rect src, Rect dst) {
        //mPaint.setColor(Color.BLACK);
        float factor = dst.height() / (float) src.height();
        float textSize = mPaint.getTextSize();
        mPaint.setTextSize(textSize * factor);
        canvas.drawText(mText, (mCurrent.mX - src.left) * factor, (mCurrent.mY - src.top) * factor, mPaint);
        mPaint.setTextSize(textSize);
    }

    public void update(float w, float h) {
        if (mVertical == null)
            mVertical = new Geometry(mHorizontal.mX - (mHorizontal.mW - w) / 2.0f, (h - mHorizontal.mH) / 2.0f + mHorizontal.mY, w, h);
        else if (mHorizontal == null)
            mHorizontal = new Geometry((w - mVertical.mW) / 2.0f + mVertical.mX, mVertical.mY - (mVertical.mH - h) / 2.0f, w, h);
        mCurrent = w < h ? mVertical : mHorizontal;
        adjustBounds();
    }

    private void adjustBounds() {
        Rect bounds = new Rect();
        mPaint.getTextBounds(mText, 0, mText.length(), bounds);
        mBounds.set(bounds);
        mBounds.offset(mCurrent.mX, mCurrent.mY);
    }
}
