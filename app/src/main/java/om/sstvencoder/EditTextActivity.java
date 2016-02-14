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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import om.sstvencoder.TextOverlay.LabelSettings;

public class EditTextActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 101;
    public static final String SETTINGS_ID = "Settings";
    private EditText mEditText;
    private int mColor;
    private MenuItem mColorMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        LabelSettings settings = getIntent().getParcelableExtra(SETTINGS_ID);
        mEditText = (EditText) findViewById(R.id.edit_text);
        mEditText.setText(settings.getText());
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
        mColor = settings.getColor();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_text, menu);
        mColorMenuItem = menu.findItem(R.id.color);

        switch (mColor) {
            case Color.BLACK:
                setColor(Color.BLACK, menu.findItem(R.id.color_black));
                break;
            case Color.RED:
                setColor(Color.RED, menu.findItem(R.id.color_red));
                break;
            case Color.MAGENTA:
                setColor(Color.MAGENTA, menu.findItem(R.id.color_magenta));
                break;
            case Color.WHITE:
                setColor(Color.WHITE, menu.findItem(R.id.color_white));
                break;
            case Color.YELLOW:
                setColor(Color.YELLOW, menu.findItem(R.id.color_yellow));
                break;
            case Color.CYAN:
                setColor(Color.CYAN, menu.findItem(R.id.color_cyan));
                break;
            case Color.GREEN:
                setColor(Color.GREEN, menu.findItem(R.id.color_green));
                break;
            case Color.BLUE:
                setColor(Color.BLUE, menu.findItem(R.id.color_blue));
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                handleActionDone();
                return true;
            case R.id.color_black:
                setColor(Color.BLACK, item);
                return true;
            case R.id.color_red:
                setColor(Color.RED, item);
                return true;
            case R.id.color_magenta:
                setColor(Color.MAGENTA, item);
                return true;
            case R.id.color_white:
                setColor(Color.WHITE, item);
                return true;
            case R.id.color_yellow:
                setColor(Color.YELLOW, item);
                return true;
            case R.id.color_cyan:
                setColor(Color.CYAN, item);
                return true;
            case R.id.color_green:
                setColor(Color.GREEN, item);
                return true;
            case R.id.color_blue:
                setColor(Color.BLUE, item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setColor(int color, MenuItem item) {
        mColor = color;
        mColorMenuItem.setTitle(item.getTitle());
        mColorMenuItem.setIcon(item.getIcon());
    }

    private void handleActionDone() {
        Intent intent = new Intent();
        LabelSettings settings = new LabelSettings();
        settings.setText(mEditText.getText().toString());
        settings.setColor(mColor);
        intent.putExtra(SETTINGS_ID, settings);
        setResult(RESULT_OK, intent);
        finish();
    }
}
