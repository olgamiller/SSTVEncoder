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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import om.sstvencoder.TextOverlay.LabelSettings;

public class EditTextActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final int REQUEST_CODE = 101;
    public static final String SETTINGS_ID = "EditLabel";
    private EditText mEditText;
    private ColorView mColorView;
    private float mTextSize;

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

        mTextSize = settings.getTextSize();
        Spinner editTextSize = (Spinner) findViewById(R.id.edit_text_size);
        editTextSize.setOnItemSelectedListener(this);
        String[] textSizeList = new String[]{"Small", "Normal", "Large", "Huge"};
        editTextSize.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, textSizeList));
        editTextSize.setSelection(textSizeToPosition(mTextSize));

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
        settings.setTextSize(mTextSize);
        intent.putExtra(SETTINGS_ID, settings);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mTextSize = positionToTextSize(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private int textSizeToPosition(float textSize) {
        int position = (int) (textSize - 1f);
        if (0 <= position && position <= 3)
            return position;
        return 1;
    }

    private float positionToTextSize(int position) {
        return position + 1f;
    }
}
