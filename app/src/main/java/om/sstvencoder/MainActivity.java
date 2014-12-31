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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends ActionBarActivity {

    private ImageView mImageView;
    private Encoder mEncoder;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mEncoder = new Encoder();

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        super.onNewIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null && type.startsWith("image/")) {
            Uri mUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (mUri != null) {
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mUri);
                    mImageView.setImageBitmap(mBitmap);
                } catch (Exception ignore) {
                }
            }
        }

        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.smpte_color_bars);
            mImageView.setImageBitmap(mBitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_martin1:
                mImageView.setImageBitmap(mEncoder.sendMartin1(mBitmap));
                return true;
            case R.id.action_send_martin2:
                mImageView.setImageBitmap(mEncoder.sendMartin2(mBitmap));
                return true;
            case R.id.action_send_scottie1:
                mImageView.setImageBitmap(mEncoder.sendScottie1(mBitmap));
                return true;
            case R.id.action_send_scottie2:
                mImageView.setImageBitmap(mEncoder.sendScottie2(mBitmap));
                return true;
            case R.id.action_send_scottiedx:
                mImageView.setImageBitmap(mEncoder.sendScottieDX(mBitmap));
                return true;
            case R.id.action_send_robot36:
                mImageView.setImageBitmap(mEncoder.sendRobot36(mBitmap));
                return true;
            case R.id.action_send_robot72:
                mImageView.setImageBitmap(mEncoder.sendRobot72(mBitmap));
                return true;
            case R.id.action_send_wrasse:
                mImageView.setImageBitmap(mEncoder.sendWrasse(mBitmap));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEncoder.destroy();
    }
}