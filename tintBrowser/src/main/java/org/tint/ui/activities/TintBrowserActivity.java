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
import java.util.Set;

import android.app.DownloadManager;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebIconDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.ContextRegistry;
import org.tint.controllers.Controller;
import org.tint.domain.download.DownloadStatus;
import org.tint.domain.download.TintDownloadManager;
import org.tint.domain.model.DownloadRequest;
import org.tint.domain.model.DownloadResponse;
import org.tint.providers.BookmarksWrapper;
import org.tint.storage.CommonPrefsStorage;
import org.tint.storage.TintBrowserActivityStorage;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.managers.UIFactory;
import org.tint.ui.managers.UIManager;
import org.tint.ui.uihelpers.TabRestoreMode;
import org.tint.ui.uihelpers.TintActivityResultHandler;
import org.tint.ui.uihelpers.browser.BrowserActivityMenuOptions;
import org.tint.ui.uihelpers.visitors.browser.BrowserActivityMenuClickVisitor;
import org.tint.ui.webview.CustomWebView;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;
import org.tint.utils.Predicate;

public class TintBrowserActivity extends BaseActivity {

    private OnSharedPreferenceChangeListener preferenceChangeListener;

    private UIManager uiManager;

    private BroadcastReceiver downloadsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onReceivedDownloadNotification(context, intent);
        }
    };

    private BroadcastReceiver packagesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Controller.getInstance().getAddonManager().unbindAddons();
            Controller.getInstance().getAddonManager().bindAddons();
        }
    };

    @Override
    protected int getLayoutId() {
        return UIFactory.getMainLayout(this);
    }

    @Override
    protected int getTitleId() {
        return -1;
    }

    @Override
    protected void doOnCreate(Bundle savedInstanceState) {
        uiManager = UIFactory.createUIManager(this);
        Controller.getInstance().init(uiManager, this);
        Controller.getInstance().getAddonManager().bindAddons();
        initializeWebIconDatabase();
        getLogger().debug("[TintBrowserActivity] [doOnCreate] creating the activity");
        preferenceChangeListener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                uiManager.onSharedPreferenceChanged(sharedPreferences, key);

                // If the user changed the history size, reset the last history truncation date.
                if (Constants.PREFERENCE_HISTORY_SIZE.equals(key)) {
                    Editor prefEditor = sharedPreferences.edit();
                    prefEditor.putLong(Constants.TECHNICAL_PREFERENCE_LAST_HISTORY_TRUNCATION, -1);
                    prefEditor.commit();
                }
            }
        };
        final TintBrowserActivityStorage tintBrowserActivityStorage = new TintBrowserActivityStorage();
        tintBrowserActivityStorage.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        registerPackageChangeReceiver();

        Intent startIntent = getIntent();

        boolean firstRun = tintBrowserActivityStorage.isFirstRun();
        if (firstRun) {
            tintBrowserActivityStorage.setFirstRunDone();
            tintBrowserActivityStorage.setAppVersionCode(ApplicationUtils.getApplicationVersionCode(this));

            BookmarksWrapper.fillDefaultBookmaks(
                    getContentResolver(),
                    getResources().getStringArray(R.array.DefaultBookmarksTitles),
                    getResources().getStringArray(R.array.DefaultBookmarksUrls));
            startIntent = showTutorialIfNeeded(startIntent, new Predicate<Integer>() {
                @Override
                public boolean isSatisfiedBy(Integer integer) {
                    return true;
                }
            });
        } else {
            int currentVersionCode = ApplicationUtils.getApplicationVersionCode(this);
            final int savedVersionCode = tintBrowserActivityStorage.getAppVersionCode();

            if (currentVersionCode != savedVersionCode) {
                tintBrowserActivityStorage.setAppVersionCode(currentVersionCode);
                startIntent = showTutorialIfNeeded(startIntent, new Predicate<Integer>() {
                    @Override
                    public boolean isSatisfiedBy(Integer integer) {
                        return savedVersionCode < integer;
                    }
                });
            }
        }

        uiManager.onNewIntent(startIntent);

        final Set<String> tabs = tintBrowserActivityStorage.getSavedTabs();
        if (!tabs.isEmpty()) {
            String tabsRestoreMode = tintBrowserActivityStorage.getRestoreTabsPreference();
            TabRestoreMode.getFromString(tabsRestoreMode).execute(this, tintBrowserActivityStorage, tabs);
        }
        tintBrowserActivityStorage.deleteSavedTabs();
    }

    private Intent showTutorialIfNeeded(Intent startIntent, Predicate<Integer> predicate) {
        // Show tutorial only on phones.
        if (UIFactory.isPhone(this)) {
            // Version code 9 introduce the new phone UI.
            if (predicate.isSatisfiedBy(9)) {
                startIntent = new Intent(Intent.ACTION_VIEW);
                startIntent.setData(Uri.parse(Constants.URL_ABOUT_TUTORIAL));
            }
        }
        return startIntent;
    }

    private void registerPackageChangeReceiver() {
        IntentFilter packagesFilter = new IntentFilter();
        packagesFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packagesFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        packagesFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packagesFilter.addCategory(Intent.CATEGORY_DEFAULT);
        packagesFilter.addDataScheme("package");
        registerReceiver(packagesReceiver, packagesFilter);
    }

    @Override
    protected void initActionBar(Bundle savedInstanceState) {
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
            @Override
            public void onMenuVisibilityChanged(boolean isVisible) {
                uiManager.onMenuVisibilityChanged(isVisible);
            }
        });

    }

    @Override
    protected void doCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(UIFactory.getMainMenuLayout(this), menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        BaseWebViewFragment currentFragment = uiManager.getCurrentWebViewFragment();

        menu.setGroupEnabled(
                R.id.MainActivity_DisabledOnStartPageMenuGroup,
                currentFragment != null && !currentFragment.isStartPageShown());

        CustomWebView currentWebView = uiManager.getCurrentWebView();

        boolean privateBrowsing = currentWebView != null && currentWebView.isPrivateBrowsingEnabled();

        menu.findItem(R.id.MainActivity_MenuIncognitoTab).setChecked(privateBrowsing);
        menu.findItem(R.id.MainActivity_MenuFullScreen).setChecked(uiManager.isFullScreen());

        menu.removeGroup(R.id.MainActivity_AddonsMenuGroup);

        if (!privateBrowsing &&
                (currentWebView != null)) {
            List<AddonMenuItem> contributedMenuItems = Controller.getInstance().getAddonManager().getContributedMainMenuItems(currentWebView);
            for (AddonMenuItem item : contributedMenuItems) {
                menu.add(R.id.MainActivity_AddonsMenuGroup, item.getAddon().getMenuId(), 0, item.getMenuItem());
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BrowserActivityMenuClickVisitor browserActivityMenuClickVisitor = new BrowserActivityMenuClickVisitor(new
                WeakReference<TintBrowserActivity>(this), uiManager, item);
        return BrowserActivityMenuOptions.getById(item.getItemId()).accept(browserActivityMenuClickVisitor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        new TintActivityResultHandler(new WeakReference<TintBrowserActivity>(this), uiManager).onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        uiManager.onNewIntent(intent);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (uiManager.onKeyBack()) {
                    return true;
                } else {
                    moveTaskToBack(true);
                    return true;
                }
            case KeyEvent.KEYCODE_SEARCH:
                if (uiManager.onKeySearch()) {
                    return true;
                } else {
                    return super.onKeyUp(keyCode, event);
                }
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        uiManager.onMainActivityPause();
        unregisterReceiver(downloadsReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        uiManager.onMainActivityResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);

        registerReceiver(downloadsReceiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        uiManager.saveTabs();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Controller.getInstance().getAddonManager().unbindAddons();
        WebIconDatabase.getInstance().close();
        CommonPrefsStorage commonPrefsStorage = new CommonPrefsStorage();
        commonPrefsStorage.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        unregisterReceiver(packagesReceiver);

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Do nothing for now, as default implementation mess up with tabs/fragment management.
        // In the future, save and restore tabs.
        //super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO Do nothing for now, as default implementation mess up with tabs/fragment management.
        // In the future, save and restore tabs.
        //super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        uiManager.onActionModeFinished(mode);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        uiManager.onActionModeStarted(mode);
    }

    public UIManager getUIManager() {
        return uiManager;
    }

    /**
     * Initialize the Web icons database.
     */
    private void initializeWebIconDatabase() {

        final WebIconDatabase db = WebIconDatabase.getInstance();
        db.open(getDir("icons", 0).getPath());
    }

    private void onReceivedDownloadNotification(Context context, Intent intent) {
        TintDownloadManager tintDownloadManager = new TintDownloadManager();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            DownloadRequest item = Controller.getInstance().getDownloadItemById(id);
            if (item != null) {
                // This is one of our downloads.
                DownloadResponse downloadResponse = tintDownloadManager.queryById(id);
                DownloadStatus.getByStatus(downloadResponse.getStatus()).execute(context, downloadResponse,
                        item);
            }
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
            tintDownloadManager.showDownloads();
        }
    }

    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass().getSimpleName());
    }
}