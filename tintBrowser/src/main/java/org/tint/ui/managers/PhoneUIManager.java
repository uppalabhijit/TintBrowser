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

package org.tint.ui.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.SparseArray;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.tint.R;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.fragments.PhoneStartPageFragment;
import org.tint.ui.fragments.PhoneWebViewFragment;
import org.tint.ui.uihelpers.NoopDrawerListener;
import org.tint.ui.views.BadgedImageView;
import org.tint.ui.views.PhoneUrlBar;
import org.tint.ui.views.PhoneUrlBar.OnPhoneUrlBarEventListener;
import org.tint.ui.views.TabView;
import org.tint.ui.views.TabsScroller;
import org.tint.ui.views.TabsScroller.OnRemoveListener;
import org.tint.ui.webview.CustomWebView;
import org.tint.utils.Constants;

class PhoneUIManager extends BasePhoneUIManager {

    private BadgedImageView mFaviconView;

    private ImageView mHome;

    private ImageView mExitFullScreen;

    private TabAdapter mAdapter;

    private SharedPreferences mPreferences;
    private TabsScroller tabsScroller;
    private DrawerLayout drawerLayout;


    static {
        sAnimationType = AnimationType.NONE;
    }

    public PhoneUIManager(TintBrowserActivity activity) {
        super(activity);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        mAdapter = new TabAdapter();
        tabsScroller.setAdapter(mAdapter);
    }

    @Override
    protected void setupUI() {

        tabsScroller = (TabsScroller) mActivity.findViewById(R.id.tabs_scroller);
        drawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);

        drawerLayout.setDrawerListener(new NoopDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                tabsScroller.snapToSelected(mCurrentTabIndex, true);
            }
        });

        mExitFullScreen = (ImageView) mActivity.findViewById(R.id.ExitFullScreen);
        mExitFullScreen.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleFullScreen();
            }
        });

        ImageView openTabView = (ImageView) mActivity.findViewById(R.id.BtnAddTab);
        openTabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addTab(true, PreferenceManager.getDefaultSharedPreferences(v.getContext()).
                        getBoolean(Constants.PREFERENCE_INCOGNITO_BY_DEFAULT, false));

                if (mPreferences.getBoolean(Constants.PREFERENCE_CLOSE_PANEL_ON_NEW_TAB, true)) {
                    closeDrawer();
                } else {
                    // Wait for the adapter/scoller to updated before scrolling to the new tab.
                    // Maybe find a better way to do this.
                    tabsScroller.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            tabsScroller.snapToSelected(mCurrentTabIndex, true);
                        }
                    }, 50);
                }
            }
        });

        ImageView openBookmarksView = (ImageView) mActivity.findViewById(R.id.BtnBookmarks);
        openBookmarksView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openBookmarksActivityForResult();
            }
        });

        mProgressBar = (ProgressBar) mActivity.findViewById(R.id.WebViewProgress);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(100);
        mProgressBar.setVisibility(View.GONE);

        mUrlBar = (PhoneUrlBar) mActivity.findViewById(R.id.UrlBar);

        mUrlBar.setEventListener(new OnPhoneUrlBarEventListener() {

            @Override
            public void onVisibilityChanged(boolean urlBarVisible) {
                if (urlBarVisible) {
                    mUrlBar.showGoStopReloadButton();
                } else {
                    BaseWebViewFragment currentFragment = getCurrentWebViewFragment();

                    if ((currentFragment != null) &&
                            (currentFragment.isStartPageShown())) {
                        mUrlBar.hideGoStopReloadButton();
                    }
                }
            }

            @Override
            public void onUrlValidated() {
                loadCurrentUrl();
            }

            @Override
            public void onGoStopReloadClicked() {
                if (mUrlBar.isUrlChangedByUser()) {
                    // Use the UIManager to load urls, as it perform check on them.
                    loadCurrentUrl();
                } else if (getCurrentWebView().isLoading()) {
                    getCurrentWebView().stopLoading();
                } else {
                    getCurrentWebView().reload();
                }
            }

            @Override
            public void onMenuVisibilityChanged(boolean isVisible) {
                mMenuVisible = isVisible;
            }
        });

        mUrlBar.setTitle(R.string.ApplicationName);
        mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);

        mFaviconView = (BadgedImageView) mActivity.findViewById(R.id.FaviconView);
        mFaviconView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUrlBar.isUrlBarVisible()) {
                    mUrlBar.hideUrl();
                } else {
                    togglePanel();
                }
            }
        });

        mTopBar = (LinearLayout) mActivity.findViewById(R.id.TopBar);
        mTopBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Steal event from WebView.
            }
        });

        mBack = (ImageView) mActivity.findViewById(R.id.BtnBack);
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((!getCurrentWebViewFragment().isStartPageShown()) &&
                        (getCurrentWebView().canGoBack())) {
                    getCurrentWebView().goBack();
                    closeDrawer();
                }
            }
        });
        mBack.setEnabled(false);

        mForward = (ImageView) mActivity.findViewById(R.id.BtnForward);
        mForward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((!getCurrentWebViewFragment().isStartPageShown()) &&
                        (getCurrentWebView().canGoForward())) {
                    getCurrentWebView().goForward();
                    closeDrawer();
                }
            }
        });
        mForward.setEnabled(false);

        mHome = (ImageView) mActivity.findViewById(R.id.BtnHome);
        mHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.GONE);
                loadHomePage();
                closeDrawer();
            }
        });

        tabsScroller.setOnRemoveListener(new OnRemoveListener() {

            @Override
            public void onRemovePosition(int position) {
                if (mFragmentsList.size() > 1) {
                    closeTabByIndex(position);
                } else {
                    loadHomePage();
                }
            }
        });

        super.setupUI();
    }

    private void togglePanel() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    private boolean isDrawerOpen() {
        return drawerLayout.isDrawerOpen(Gravity.LEFT);
    }

    private void openDrawer() {
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    private void closeDrawer() {
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void addTab(String url, boolean openInBackground, boolean privateBrowsing) {
        super.addTab(url, openInBackground, privateBrowsing);

        updateUrlBar();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void closeLastTab() {
        super.closeLastTab();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void closeTabByIndex(int index) {
        super.closeTabByIndex(index);

        updateUrlBar();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void showCurrentTab(boolean notifyTabSwitched) {
        super.showCurrentTab(notifyTabSwitched);

        TabView currentTabView = mAdapter.getViewAt(mCurrentTabIndex);
        if (currentTabView != null) {
            currentTabView.setSelected(true);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (view == getCurrentWebView()) {
            mProgressBar.setProgress(0);
            mProgressBar.setVisibility(View.VISIBLE);

            mUrlBar.setUrl(url);

            mUrlBar.setGoStopReloadImage(R.drawable.ic_stop);

            updateBackForwardEnabled();
        }

        CustomWebView webview = (CustomWebView) view;
        PhoneWebViewFragment parent = (PhoneWebViewFragment) webview.getParentFragment();

        if (parent != null) {
            int index = mFragmentsList.indexOf(parent);
            if (index != -1) {
                TabView tabview = mAdapter.getViewAt(index);
                tabview.setFavicon(null);
            }
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (view == getCurrentWebView()) {
            mProgressBar.setProgress(100);
            mProgressBar.setVisibility(View.GONE);

            mUrlBar.setUrl(url);

            mUrlBar.setGoStopReloadImage(R.drawable.ic_refresh);

            updateBackForwardEnabled();
        }
    }

    @Override
    public void onClientPageFinished(final CustomWebView view, String url) {
        super.onClientPageFinished(view, url);

        PhoneWebViewFragment parent = (PhoneWebViewFragment) view.getParentFragment();

        if ((parent != null) && (!parent.isStartPageShown()) && (!view.isLoading())) {
            int index = mFragmentsList.indexOf(parent);
            if (index != -1) {
                final TabView tabview = mAdapter.getViewAt(index);
                drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tabview.setImage(view.capturePicture());
                    }
                }, 50);
            }
        }
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        // Don't call parent here, we don't need the parent behavior.
        CustomWebView webview = (CustomWebView) view;
        PhoneWebViewFragment parent = (PhoneWebViewFragment) webview.getParentFragment();

        if ((parent != null) &&
                (!parent.isStartPageShown())) {
            int index = mFragmentsList.indexOf(parent);
            if (index != -1) {
                TabView tabview = mAdapter.getViewAt(index);
                tabview.setFavicon(icon);
            }
        }
    }

    @Override
    public boolean onKeyBack() {
        if (!super.onKeyBack()) {
            if (mUrlBar.isUrlBarVisible()) {
                mUrlBar.hideUrl();

                return true;
            } else if (isDrawerOpen()) {
                closeDrawer();
                return true;
            } else {
                CustomWebView currentWebView = getCurrentWebView();

                if ((currentWebView != null) &&
                        (currentWebView.canGoBack())) {
                    currentWebView.goBack();
                    return true;
                } else if (isHomePageStartPage() &&
                        !isStartPageShownOnCurrentTab()) {
                    loadHomePage();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onKeySearch() {
        mUrlBar.showUrl();
        return true;
    }

    @Override
    public void onMenuVisibilityChanged(boolean isVisible) {
    }

    @Override
    public void onShowStartPage() {
        super.onShowStartPage();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        mActionMode = mode;
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        if (mActionMode != null) {
            mActionMode = null;

            InputMethodManager mgr = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(null, 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if ((requestCode == Constants.ACTIVITY_BOOKMARKS) &&
                (resultCode == Activity.RESULT_OK)) {
            closeDrawerIfOpen();
        }
    }

    private void closeDrawerIfOpen() {
        if (isDrawerOpen()) {
            closeDrawer();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    protected void setFullScreenFromPreferences() {
        boolean fullScreen = isFullScreen();

        Window win = mActivity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;

        if (fullScreen) {
            winParams.flags |= bits;
            mTopBar.setVisibility(View.GONE);
            mExitFullScreen.setVisibility(View.VISIBLE);
        } else {
            winParams.flags &= ~bits;
            mTopBar.setVisibility(View.VISIBLE);
            mExitFullScreen.setVisibility(View.GONE);
        }

        win.setAttributes(winParams);
    }

    @Override
    protected void updateUrlBar() {
        CustomWebView currentWebView;
        BaseWebViewFragment currentFragment = getCurrentWebViewFragment();

        if ((currentFragment != null) &&
                (currentFragment.isStartPageShown())) {
            currentWebView = null;
        } else {
            currentWebView = getCurrentWebView();
        }

        if (currentWebView != null) {
            String title = currentWebView.getTitle();
            String url = currentWebView.getUrl();
            Bitmap icon = currentWebView.getFavicon();

            if ((title != null) &&
                    (!title.isEmpty())) {
                mUrlBar.setTitle(title);
            } else {
                mUrlBar.setTitle(R.string.ApplicationName);
            }

            if ((url != null) &&
                    (!url.isEmpty())) {
                mUrlBar.setSubtitle(url);
                mUrlBar.setUrl(url);
            } else {
                mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);
                mUrlBar.setUrl(null);
            }

            setApplicationButtonImage(icon);

            if (currentWebView.isLoading()) {
                mProgressBar.setProgress(currentWebView.getProgress());
                mProgressBar.setVisibility(View.VISIBLE);
                mUrlBar.setGoStopReloadImage(R.drawable.ic_stop);
            } else {
                mProgressBar.setVisibility(View.GONE);
                mUrlBar.setGoStopReloadImage(R.drawable.ic_refresh);
            }

            updateBackForwardEnabled();
        } else {
            mUrlBar.setTitle(R.string.ApplicationName);
            mUrlBar.setSubtitle(R.string.UrlBarUrlDefaultSubTitle);

            mProgressBar.setVisibility(View.GONE);

            mUrlBar.setUrl(null);
            mBack.setEnabled(false);
            mForward.setEnabled(false);
        }

        mFaviconView.setValue(mFragmentsList.size());

        mUrlBar.setPrivateBrowsingIndicator(currentFragment != null ? currentFragment.isPrivateBrowsingEnabled() : false);
    }

    @Override
    protected void createStartPageFragment() {
        mStartPageFragment = new PhoneStartPageFragment();
    }

    private void showTabByIndex(int index, boolean notifyTabSwitched) {
        PhoneWebViewFragment oldFragment = mFragmentsList.get(mCurrentTabIndex);
        oldFragment.getWebView().onPause();

        TabView oldTabView = mAdapter.getViewAt(mCurrentTabIndex);
        if (oldTabView != null) {
            oldTabView.setSelected(false);
        }

        mCurrentTabIndex = index;
        showCurrentTab(notifyTabSwitched);
    }

    private class TabAdapter extends BaseAdapter {

        private SparseArray<TabView> mViews;

        public TabAdapter() {
            super();
            mViews = new SparseArray<TabView>();
        }

        @Override
        public int getCount() {
            return mFragmentsList.size();
        }

        @Override
        public PhoneWebViewFragment getItem(int position) {
            return mFragmentsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void notifyDataSetChanged() {
            mViews.clear();
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final TabView tabview = new TabView(mActivity);

            PhoneWebViewFragment fragment = getItem(position);

            if (fragment.isStartPageShown()) {
                tabview.setTitle(R.string.StartPageLabel);
                tabview.setFavicon(null);
            } else {
                CustomWebView webView = fragment.getWebView();

                tabview.setTitle(webView.getTitle());

                tabview.setFavicon(webView.getFavicon());
                tabview.setImage(webView.isLoading() ? null : webView.capturePicture());
            }

            tabview.setSelected(position == mCurrentTabIndex);

            tabview.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (tabview.isClose(v)) {
                        tabsScroller.animateOut(tabview);
                    } else {
                        showTabByIndex(position, true);
                        closeDrawer();
                    }
                }
            });

            mViews.put(position, tabview);

            return tabview;
        }

        public TabView getViewAt(int position) {
            return mViews.get(position);
        }

    }
}
