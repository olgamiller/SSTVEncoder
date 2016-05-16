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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import om.sstvencoder.TextOverlay.LabelSettings;

public class EditTextActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 101;
    public static final String SETTINGS_ID = "EditLabel";
    private EditText mEditText;
    private ColorView mColorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mColorView = (ColorView) findViewById(R.id.edit_color);
        mColorView.setOnChangeListener(new ColorView.OnChangeListener() {
            @Override
            public void onChange(View v, int color) {
                mEditText.setTextColor(color);
            }
        });
    }

    // Now ColorView should have width and height unequal 0.
    @Override
    protected void onStart() {
        super.onStart();
        LabelSettings settings = getIntent().getParcelableExtra(SETTINGS_ID);
        mColorView.setColor(settings.getColor());
        mEditText.setText(settings.getText());
        mEditText.setTextColor(mColorView.getColor());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_text, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                handleActionDone();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleActionDone() {
        Intent intent = new Intent();
        LabelSettings settings = new LabelSettings();
        settings.setText(mEditText.getText().toString());
        settings.setColor(mColorView.getColor());
        intent.putExtra(SETTINGS_ID, settings);
        setResult(RESULT_OK, intent);
        finish();
    }
}
