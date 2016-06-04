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

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.ui.fragments.BookmarksFragment;
import org.tint.ui.fragments.HistoryFragment;
import org.tint.ui.managers.UIManager;
import org.tint.ui.tabs.GenericTabListener;
import org.tint.ui.uihelpers.BookmarkMenuOptions;
import org.tint.ui.uihelpers.BookmarksMenuClickVisitor;

public class BookmarksActivity extends Activity {

    private static final String EXTRA_SELECTED_TAB_INDEX = "EXTRA_SELECTED_TAB_INDEX";
	
    private UIManager mUIManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.BookmarksTitle);

        mUIManager = Controller.getInstance().getUIManager();

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Tab tab = actionBar.newTab();
        tab.setText(R.string.BookmarksTabTitle);
        tab.setTabListener(new GenericTabListener<BookmarksFragment>(this, "bookmarks", BookmarksFragment.class));
        actionBar.addTab(tab);

        tab = actionBar.newTab();
        tab.setText(R.string.HistoryTabTitle);
        tab.setTabListener(new GenericTabListener<HistoryFragment>(this, "history", HistoryFragment.class));
        actionBar.addTab(tab);

        if ((savedInstanceState != null) &&
                (savedInstanceState.containsKey(EXTRA_SELECTED_TAB_INDEX))) {
            int selectedIndex = savedInstanceState.getInt(EXTRA_SELECTED_TAB_INDEX);
        	
            if ((selectedIndex == 0) ||
                    (selectedIndex == 1)) {
                actionBar.setSelectedNavigationItem(selectedIndex);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_TAB_INDEX, getActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.bookmarks_activity_menu, menu);
		
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (getActionBar().getSelectedNavigationIndex() == 0) {
            menu.findItem(R.id.BookmarksActivityMenuSortBookmarks).setVisible(true);
        } else {
            menu.findItem(R.id.BookmarksActivityMenuSortBookmarks).setVisible(false);
        }

        menu.removeGroup(R.id.BookmarksActivity_AddonsMenuGroup);

        List<AddonMenuItem> contributedMenuItems = Controller.getInstance().getAddonManager().getContributedHistoryBookmarksMenuItems(mUIManager.getCurrentWebView());
        for (AddonMenuItem item : contributedMenuItems) {
            menu.add(R.id.BookmarksActivity_AddonsMenuGroup, item.getAddon().getMenuId(), 0, item.getMenuItem());
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return BookmarkMenuOptions.getById(item.getItemId()).accept(new BookmarksMenuClickVisitor(new WeakReference<BookmarksActivity>(this),
                item));
    }
}
