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

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.ui.adapters.BookmarksFragmentAdapter;
import org.tint.ui.fragments.BookmarksFragment;
import org.tint.ui.fragments.HistoryFragment;
import org.tint.ui.managers.UIManager;
import org.tint.ui.uihelpers.bookmarks.BookmarkMenuOptions;
import org.tint.ui.uihelpers.visitors.bookmarks.BookmarksMenuClickVisitor;

public class BookmarksActivity extends BaseActivity {

    private static final String EXTRA_SELECTED_TAB_INDEX = "EXTRA_SELECTED_TAB_INDEX";
    private UIManager uiManager;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected int getLayoutId() {
        return R.layout.bookmarks_layout;
    }

    @Override
    protected int getTitleId() {
        return R.string.BookmarksTitle;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {
        uiManager = Controller.getInstance().getUIManager();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        BookmarksFragmentAdapter adapter = new BookmarksFragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new BookmarksFragment(), "Bookmarks");
        adapter.addFragment(new HistoryFragment(), "History");
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void initActionBar(Bundle savedInstanceState) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void doCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bookmarks_activity_menu, menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (getSupportActionBar().getSelectedNavigationIndex() == 0) {
            menu.findItem(R.id.BookmarksActivityMenuSortBookmarks).setVisible(true);
        } else {
            menu.findItem(R.id.BookmarksActivityMenuSortBookmarks).setVisible(false);
        }

        menu.removeGroup(R.id.BookmarksActivity_AddonsMenuGroup);

        List<AddonMenuItem> contributedMenuItems = Controller.getInstance().getAddonManager().getContributedHistoryBookmarksMenuItems(uiManager.getCurrentWebView());
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
