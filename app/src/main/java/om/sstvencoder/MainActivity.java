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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.FileNotFoundException;

import om.sstvencoder.TextOverlay.LabelSettings;

public class MainActivity extends AppCompatActivity {
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
        loadImage(getIntent());
    }

    private void loadImage(Intent intent) {
        if (!handleIntent(intent)) {
            try {
                mCropView.setBitmapStream(getResources().openRawResource(R.raw.smpte_color_bars));
            } catch (Exception ex) {
                String s = Utility.createMessage(ex) + "\n\n" + intent;
                showErrorMessage(getString(R.string.load_img_err_title), ex.getMessage(), s);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    private boolean handleIntent(Intent intent) {
        if (!isIntentTypeValid(intent.getType()) || !isIntentActionValid(intent.getAction()))
            return false;
        Uri uri = intent.hasExtra(Intent.EXTRA_STREAM) ? (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM) : intent.getData();
        if (uri != null) {
            try {
                ContentResolver resolver = getContentResolver();
                mCropView.setBitmapStream(resolver.openInputStream(uri));
                mCropView.rotateImage(getOrientation(resolver, uri));
                return true;
            } catch (FileNotFoundException ex) {
                String s = getString(R.string.load_img_err_title) + ": \n" + ex.getMessage();
                Toast.makeText(this, s, Toast.LENGTH_LONG).show();
            } catch (Exception ex) {
                String s = Utility.createMessage(ex) + "\n\n" + uri + "\n\n" + intent;
                showErrorMessage(getString(R.string.load_img_err_title), ex.getMessage(), s);
            }
        } else {
            String s = getString(R.string.load_img_err_txt_unsupported);
            showErrorMessage(getString(R.string.load_img_err_title), s, s + "\n\n" + intent);
        }
        return false;
    }

    private boolean isIntentActionValid(String action) {
        return Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action);
    }

    private boolean isIntentTypeValid(String type) {
        return type != null && type.startsWith("image/");
    }

    private void showErrorMessage(final String title, final String shortText, final String longText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(shortText);
        builder.setNeutralButton(getString(R.string.btn_ok), null);
        builder.setPositiveButton(getString(R.string.btn_send_email), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = Utility.createEmailIntent(getString(R.string.email_subject), longText);
                startActivity(Intent.createChooser(intent, getString(R.string.chooser_title)));
            }
        });
        builder.show();
    }

    private void showOrientationErrorMessage(Uri uri, Exception ex) {
        String title = getString(R.string.load_img_orientation_err_title);
        String longText = title + "\n\n" + Utility.createMessage(ex) + "\n\n" + uri;
        showErrorMessage(title, ex.getMessage(), longText);
    }

    public int getOrientation(ContentResolver resolver, Uri uri) {
        int orientation = 0;
        try {
            Cursor cursor = resolver.query(uri, new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);
            if (cursor.moveToFirst())
                orientation = cursor.getInt(0);
            cursor.close();
        } catch (Exception ignore) {
            try {
                ExifInterface exif = new ExifInterface(uri.getPath());
                orientation = Utility.convertToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0));
            } catch (Exception ex) {
                showOrientationErrorMessage(uri, ex);
            }
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
            case R.id.action_pd50:
                mCropView.setModeSize(mEncoder.setPD50());
                setTitle(R.string.action_pd50);
                return true;
            case R.id.action_pd90:
                mCropView.setModeSize(mEncoder.setPD90());
                setTitle(R.string.action_pd90);
                return true;
            case R.id.action_pd120:
                mCropView.setModeSize(mEncoder.setPD120());
                setTitle(R.string.action_pd120);
                return true;
            case R.id.action_pd160:
                mCropView.setModeSize(mEncoder.setPD160());
                setTitle(R.string.action_pd160);
                return true;
            case R.id.action_pd180:
                mCropView.setModeSize(mEncoder.setPD180());
                setTitle(R.string.action_pd180);
                return true;
            case R.id.action_pd240:
                mCropView.setModeSize(mEncoder.setPD240());
                setTitle(R.string.action_pd240);
                return true;
            case R.id.action_pd290:
                mCropView.setModeSize(mEncoder.setPD290());
                setTitle(R.string.action_pd290);
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
            case R.id.action_wraase:
                mCropView.setModeSize(mEncoder.setWraase());
                setTitle(R.string.action_wraase);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void send() {
        mEncoder.send(mCropView.getBitmap());
    }

    public void startEditTextActivity(LabelSettings settings) {
        Intent intent = new Intent(this, EditTextActivity.class);
        intent.putExtra(EditTextActivity.SETTINGS_ID, settings);
        startActivityForResult(intent, EditTextActivity.REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EditTextActivity.REQUEST_CODE) {
            mCropView.loadLabelSettings(resultCode == RESULT_OK && data != null ?
                    (LabelSettings) data.getParcelableExtra(EditTextActivity.SETTINGS_ID) : null);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEncoder.destroy();
    }
}