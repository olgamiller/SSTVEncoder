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

package om.sstvencoder;

import android.content.Intent;
import android.media.ExifInterface;

public final class Utility {

    public static android.graphics.Rect getEmbeddedRect(int w, int h, int iw, int ih) {
        android.graphics.Rect rect;

        int ow = (9 * w) / 10;
        int oh = (9 * h) / 10;

        if (iw * oh < ow * ih) {
            rect = new android.graphics.Rect(0, 0, (iw * oh) / ih, oh);
            rect.offset((w - (iw * oh) / ih) / 2, (h - oh) / 2);
        } else {
            rect = new android.graphics.Rect(0, 0, ow, (ih * ow) / iw);
            rect.offset((w - ow) / 2, (h - (ih * ow) / iw) / 2);
        }
        return rect;
    }

    /* public static void vibrate(int duration, android.content.Context context) {
        android.os.Vibrator vibrator = (android.os.Vibrator) context.getSystemService(android.content.Context.VIBRATOR_SERVICE);
        vibrator.vibrate(duration);
    } */

    public static String createMessage(Exception ex) {
        String message = ex.getMessage() + "\n";
        for (StackTraceElement el : ex.getStackTrace())
            message += "\n" + el.toString();
        return message;
    }

    public static Intent createEmailIntent(final String subject, final String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/email");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"olga.rgb@googlemail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        return intent;
    }

    public static int convertToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
        }
        return 0;
    }
}
