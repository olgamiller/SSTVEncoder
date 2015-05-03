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
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import om.sstvencoder.TextOverlay.LabelSettings;

public class EditTextActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 101;
    public static final String SETTINGS_ID = "Settings";
    private EditText mEditText;
    private LabelSettings mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        mSettings = getIntent().getParcelableExtra(SETTINGS_ID);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mEditText.setText(mSettings.getText());
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    handleActionDone();
                    return true;
                }
                return false;
            }
        });
    }

    private void handleActionDone() {
        Intent intent = new Intent();
        mSettings.setText(mEditText.getText().toString());
        intent.putExtra(SETTINGS_ID, mSettings);
        setResult(RESULT_OK, intent);
        finish();
    }
}
