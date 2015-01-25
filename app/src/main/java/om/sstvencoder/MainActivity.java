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

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

    private CropView mCropView;
    private Encoder mEncoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mCropView = (CropView) findViewById(R.id.cropView);
        mEncoder = new Encoder();
        mCropView.setModeSize(mEncoder.setRobot36());
        setTitle(R.string.action_robot36);
        if (!handleIntent(getIntent()))
            mCropView.setBitmapStream(getResources().openRawResource(R.raw.smpte_color_bars));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    private boolean handleIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                try {
                    ContentResolver resolver = getContentResolver();
                    mCropView.setBitmapStream(resolver.openInputStream(uri));
                    mCropView.rotateImage(getOrientation(resolver, uri));
                    return true;
                } catch (Exception ignore) {
                }
            }
        }
        return false;
    }

    public static int getOrientation(ContentResolver resolver, Uri uri) {
        int orientation = 0;
        Cursor cursor = resolver.query(uri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION},
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst())
                orientation = cursor.getInt(0);
            cursor.close();
        }
        return orientation;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                send();
                return true;
            case R.id.action_rotate:
                mCropView.rotateImage(90);
                return true;
            case R.id.action_martin1:
                mCropView.setModeSize(mEncoder.setMartin1());
                setTitle(R.string.action_martin1);
                return true;
            case R.id.action_martin2:
                mCropView.setModeSize(mEncoder.setMartin2());
                setTitle(R.string.action_martin2);
                return true;
            case R.id.action_scottie1:
                mCropView.setModeSize(mEncoder.setScottie1());
                setTitle(R.string.action_scottie1);
                return true;
            case R.id.action_scottie2:
                mCropView.setModeSize(mEncoder.setScottie2());
                setTitle(R.string.action_scottie2);
                return true;
            case R.id.action_scottiedx:
                mCropView.setModeSize(mEncoder.setScottieDX());
                setTitle(R.string.action_scottiedx);
                return true;
            case R.id.action_robot36:
                mCropView.setModeSize(mEncoder.setRobot36());
                setTitle(R.string.action_robot36);
                return true;
            case R.id.action_robot72:
                mCropView.setModeSize(mEncoder.setRobot72());
                setTitle(R.string.action_robot72);
                return true;
            case R.id.action_wrasse:
                mCropView.setModeSize(mEncoder.setWrasse());
                setTitle(R.string.action_wrasse);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void send() {
        mEncoder.send(mCropView.getBitmap());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEncoder.destroy();
    }
}