/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.utilities.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * Responsible for displaying buttons to launch the major activities. Launches some activities based
 * on returns of others.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends Activity {
    private static final String t = "MainMenuActivity";

    // menu options
    private static final int MENU_PREFERENCES = Menu.FIRST;

    // buttons
    private Button mEnterDataButton;
    private Button mManageFilesButton;
    private Button mSendDataButton;
    private Button mReviewDataButton;

    private AlertDialog mAlertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main_menu);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.main_menu));

        // if sd card error, quit
        if (!FileUtils.storageReady()) {
            createErrorDialog(getString(R.string.no_sd_error), true);
            return;
        }

        // enter data button. expects a result.
        mEnterDataButton = (Button) findViewById(R.id.enter_data);
        mEnterDataButton.setText(getString(R.string.enter_data_button));
        mEnterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FormChooserList.class);
                startActivity(i);
            }
        });

        // review data button. expects a result.
        mReviewDataButton = (Button) findViewById(R.id.review_data);
        mReviewDataButton.setText(getString(R.string.review_data_button));
        mReviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                startActivity(i);
            }
        });

        // send data button. expects a result.
        mSendDataButton = (Button) findViewById(R.id.send_data);
        mSendDataButton.setText(getString(R.string.send_data_button));
        mSendDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceUploaderList.class);
                startActivity(i);
            }
        });

        // manage forms button. no result expected.
        mManageFilesButton = (Button) findViewById(R.id.manage_forms);
        mManageFilesButton.setText(getString(R.string.manage_files));
        mManageFilesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FileManagerTabs.class);
                startActivity(i);
            }
        });
    }


    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        dismissDialogs();
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_PREFERENCES, 0, getString(R.string.general_preferences)).setIcon(
            android.R.drawable.ic_menu_preferences);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                Intent ig = new Intent(this, PreferencesActivity.class);
                startActivity(ig);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }


    /**
     * Dismiss any showing dialogs that we manage.
     */
    private void dismissDialogs() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }
}
