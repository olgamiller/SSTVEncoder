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

import android.os.Parcel;
import android.os.Parcelable;

public class LabelSettings implements Parcelable {
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public LabelSettings createFromParcel(Parcel source) {
            return new LabelSettings(source);
        }

        @Override
        public LabelSettings[] newArray(int size) {
            return new LabelSettings[size];
        }
    };

    public float TextSize;
    private String mText;

    public LabelSettings() {
        mText = "";
    }

    public LabelSettings(Parcel src) {
        readFromParcel(src);
    }

    private void readFromParcel(Parcel src) {
        setText(src.readString());
        TextSize = src.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mText);
        dest.writeFloat(TextSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setText(String text) {
        if (text != null)
            mText = text.trim();
        else
            mText = "";
    }

    public String getText() {
        return mText;
    }
}
