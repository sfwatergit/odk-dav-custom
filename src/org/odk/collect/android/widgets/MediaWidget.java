/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import java.io.File;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.PromptElement;
import org.odk.collect.android.R;
import org.odk.collect.android.SharedConstants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Widget that allows user to take pictures, sounds or video and add them to the
 * form.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MediaWidget extends LinearLayout implements IQuestionWidget, IBinaryWidget {

    private Button mCaptureButton;
    private Button mPlayButton;

    private String mBinaryName;
    private TextView mDisplayText;

    private Uri mExternalUri;
    private String mCaptureIntent;
    private String mType;
    private String mAnswersPath;
    private int mRequestCode;
    private int mCaptureText;
    private int mReplaceText;
    private int mPlayText;


    public MediaWidget(Context context, String type, String path) {
        super(context);
        initialize(type, path);
    }


    private void initialize(String type, String path) {

        mType = type;
        mAnswersPath = path;

        if (mType.equals("image")) {
            mExternalUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            mCaptureIntent = android.provider.MediaStore.ACTION_IMAGE_CAPTURE;
            mRequestCode = SharedConstants.IMAGE_CAPTURE;
            mCaptureText = R.string.capture_image;
            mReplaceText = R.string.replace_image;
            mPlayText = R.string.play_image;
        } else if (mType.equals("audio")) {
            mExternalUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mCaptureIntent = android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION;
            mRequestCode = SharedConstants.AUDIO_CAPTURE;
            mCaptureText = R.string.capture_audio;
            mReplaceText = R.string.replace_audio;
            mPlayText = R.string.play_audio;
        } else if (mType.equals("video")) {
            mExternalUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            mCaptureIntent = android.provider.MediaStore.ACTION_VIDEO_CAPTURE;
            mRequestCode = SharedConstants.VIDEO_CAPTURE;
            mCaptureText = R.string.capture_video;
            mReplaceText = R.string.replace_video;
            mPlayText = R.string.play_video;
        }
    }


    private void deleteMedia() {

        // get the file path and delete the file
        File f = new File(mAnswersPath + "/" + mBinaryName);
        f.delete();

        // clean up variables
        mBinaryName = null;

    }


    public void clearAnswer() {

        // remove the file
        deleteMedia();

        // reset buttons
        mPlayButton.setEnabled(false);
        mCaptureButton.setText(getContext().getString(mCaptureText));
        mDisplayText.setText(getContext().getString(R.string.no_capture));
    }


    public IAnswerData getAnswer() {
        if (mBinaryName != null) {
            return new StringData(mBinaryName.toString());
        } else {
            return null;
        }
    }


    public void buildView(PromptElement prompt) {

        this.setOrientation(LinearLayout.VERTICAL);

        // setup capture button
        mCaptureButton = new Button(getContext());
        mCaptureButton.setText(getContext().getString(mCaptureText));
        mCaptureButton
                .setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mCaptureButton.setPadding(20, 20, 20, 20);
        mCaptureButton.setEnabled(!prompt.isReadonly());

        // launch capture intent on click
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(mCaptureIntent);
                if (mType.equals("image")) {
                    // TODO only way to get large image from android
                    // http://code.google.com/p/android/issues/detail?id=1480
                    i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
                            SharedConstants.TMPFILE_PATH)));
                } else {
                    i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mExternalUri.toString());
                }
                ((Activity) getContext()).startActivityForResult(i, mRequestCode);


            }
        });

        // setup play button
        mPlayButton = new Button(getContext());
        mPlayButton.setText(getContext().getString(mPlayText));
        mPlayButton.setTextSize(TypedValue.COMPLEX_UNIT_PT, SharedConstants.APPLICATION_FONTSIZE);
        mPlayButton.setPadding(20, 20, 20, 20);

        // on play, launch the appropriate viewer
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent("android.intent.action.VIEW");
                File f = new File(mAnswersPath + "/" + mBinaryName);
                i.setDataAndType(Uri.fromFile(f), mType + "/*");
                ((Activity) getContext()).startActivity(i);

            }
        });

        // retrieve answer from data model and update ui
        mDisplayText = new TextView(getContext());
        mBinaryName = prompt.getAnswerText();
        if (mBinaryName != null) {
            mPlayButton.setEnabled(true);
            mCaptureButton.setText(getContext().getString(mReplaceText));
            mDisplayText.setText(getContext().getString(R.string.one_capture));
        } else {
            mPlayButton.setEnabled(false);
            mDisplayText.setText(getContext().getString(R.string.no_capture));
        }

        // finish complex layout
        this.addView(mDisplayText);
        this.addView(mCaptureButton);
        this.addView(mPlayButton);

    }


    private Uri getUriFromPath(String path) {

        // find entry in content provider
        Cursor c =
                getContext().getContentResolver().query(mExternalUri, null, "_data='" + path + "'",
                        null, null);
        c.moveToFirst();

        // create uri from path
         String newPath = mExternalUri + "/" + c.getInt(c.getColumnIndex("_id"));
         c.close();
         return Uri.parse(newPath);

    }


    private String getPathFromUri(Uri uri) {

        // find entry in content provider
        Cursor c = getContext().getContentResolver().query(uri, null, null, null, null);
        c.moveToFirst();

        // get data path
        String colString = c.getString(c.getColumnIndex("_data"));
        c.close();
        return colString;
    }



    public void setBinaryData(Object binaryuri) {

        // you are replacing an answer. remove the media.
        if (mBinaryName != null) {
            deleteMedia();
        }

        // get the file path and move the file
        String binarypath = getPathFromUri((Uri) binaryuri);
        File f = new File(binarypath);
        String s = mAnswersPath + binarypath.substring(binarypath.lastIndexOf('/'));
        f.renameTo(new File(s));

        // remove the database entry and update the name
        getContext().getContentResolver().delete(getUriFromPath(binarypath), null, null);
        mBinaryName = s.substring(s.lastIndexOf('/') + 1);

    }

}
