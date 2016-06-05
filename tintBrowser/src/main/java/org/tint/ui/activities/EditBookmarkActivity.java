/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.tint.ui.activities;

import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import org.tint.R;
import org.tint.ui.model.FolderItem;
import org.tint.providers.BookmarksWrapper;
import org.tint.utils.Constants;

public class EditBookmarkActivity extends BaseActivity {

    private long id = -1;

    private EditText label;
    private EditText url;

    private Spinner foldersSpinner;

    private EditText newFolderName;

    private Button ok;
    private Button cancel;

    private List<FolderItem> folders;

    @Override
    protected int getLayoutId() {
        return R.layout.edit_bookmark_activity;
    }

    @Override
    protected int getTitleId() {
        return R.string.AddBookmarkTitle;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {
        folders = BookmarksWrapper.getFirstLevelFoldersList(getContentResolver());
        folders.add(0, new FolderItem(-1, getString(R.string.Bookmarks)));
        folders.add(0, new FolderItem(-2, getString(R.string.NewFolder)));

        label = (EditText) findViewById(R.id.EditBookmarkActivity_LabelEdit);
        url = (EditText) findViewById(R.id.EditBookmarkActivity_UrlEdit);

        foldersSpinner = (Spinner) findViewById(R.id.EditBookmarkActivity_FolderSpinner);

        FoldersAdapter adapter = new FoldersAdapter(this, folders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foldersSpinner.setAdapter(adapter);

        foldersSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                if (position == 0) {
                    newFolderName.setVisibility(View.VISIBLE);
                    newFolderName.requestFocus();
                } else {
                    newFolderName.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // Default to root folder.
        foldersSpinner.setSelection(1);

        newFolderName = (EditText) findViewById(R.id.EditBookmarkActivity_FolderValue);

        ok = (Button) findViewById(R.id.EditBookmarkActivity_OK);
        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (save()) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        cancel = (Button) findViewById(R.id.EditBookmarkActivity_Cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String label = extras.getString(Constants.EXTRA_LABEL);
            if (!TextUtils.isEmpty(label)) {
                this.label.setText(label);
            }

            String url = extras.getString(Constants.EXTRA_URL);
            if (!TextUtils.isEmpty(url)) {
                this.url.setText(url);
            }

            // This is a bit dirty...
            long folderId = extras.getLong(Constants.EXTRA_FOLDER_ID);
            if (folderId != -1) {
                for (int i = 0; i < folders.size(); i++) {
                    if (folders.get(i).getId() == folderId) {
                        foldersSpinner.setSelection(i);
                        break;
                    }
                }
            }

            id = extras.getLong(Constants.EXTRA_ID);
        }
    }

    @Override
    protected void initActionBar(Bundle savedInstanceState) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void doCreateOptionsMenu(Menu menu) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private boolean save() {
        String label = this.label.getText().toString();
        String url = this.url.getText().toString();

        if ((!TextUtils.isEmpty(label)) &&
                (!TextUtils.isEmpty(url))) {

            long folderId = -1;
            int folderSpinnerSelection = foldersSpinner.getSelectedItemPosition();

            switch (folderSpinnerSelection) {
                case 0:
                    if (TextUtils.isEmpty(newFolderName.getText().toString())) {
                        Toast.makeText(this, R.string.ProvideNewFolderName, Toast.LENGTH_SHORT).show();
                        return false;
                    } else {
                        folderId = BookmarksWrapper.getFolderId(getContentResolver(), newFolderName.getText().toString(), true);
                    }
                    break;

                case 1:
                    folderId = -1;
                    break;
                default:
                    folderId = folders.get(folderSpinnerSelection).getId();
                    break;
            }

            BookmarksWrapper.setAsBookmark(getContentResolver(), id, folderId, label, url, true);
            return true;
        } else {
            Toast.makeText(this, R.string.AddBookmarkLabelOrUrlEmpty, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private class FoldersAdapter extends ArrayAdapter<FolderItem> {

        public FoldersAdapter(Context context, List<FolderItem> values) {
            super(context, android.R.layout.simple_spinner_item, android.R.id.text1, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            TextView tv = (TextView) v.findViewById(android.R.id.text1);
            tv.setText(getItem(position).getTitle());

            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = super.getDropDownView(position, convertView, parent);

            TextView tv = (TextView) v.findViewById(android.R.id.text1);
            tv.setText(getItem(position).getTitle());

            return v;
        }


    }

}
