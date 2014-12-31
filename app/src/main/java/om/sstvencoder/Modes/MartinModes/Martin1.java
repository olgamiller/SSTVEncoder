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

package om.sstvencoder.Modes.MartinModes;

import android.graphics.Bitmap;

public class Martin1 extends Martin {
    public Martin1(Bitmap bitmap){
        super(bitmap);
        mVISCode = 44;

        mColorScanDurationMs = 146.432;
        mColorScanSamples = convertMsToSamples(mColorScanDurationMs);
    }
}
