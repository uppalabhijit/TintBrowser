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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.FrameLayout;

import org.tint.R;
import org.tint.providers.BookmarksWrapper;
import org.tint.storage.BrowserSettingsStorage;
import org.tint.storage.TintBrowserActivityStorage;
import org.tint.tasks.ThumbnailSaver;
import org.tint.ui.activities.BookmarksActivity;
import org.tint.ui.activities.EditBookmarkActivity;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.dialogs.GeolocationPermissionsDialog;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.fragments.StartPageFragment;
import org.tint.ui.uihelpers.BrowserActivityContextMenuOptions;
import org.tint.ui.uihelpers.visitors.BrowserActivityContextMenuClickVisitor;
import org.tint.ui.uihelpers.visitors.BrowserActivityContextMenuVisitor;
import org.tint.ui.webview.CustomWebView;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

@SuppressLint("HandlerLeak")
public abstract class BaseUIManager implements UIManager {//, WebViewFragmentListener {

    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
            new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);


    private FrameLayout mFullscreenContainer;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;

    private GeolocationPermissionsDialog mGeolocationPermissionsDialog;

    protected TintBrowserActivity mActivity;
    protected ActionBar mActionBar;
    protected FragmentManager mFragmentManager;

    protected boolean mHomePageLoading = false;

    protected boolean mMenuVisible = false;

    private ValueCallback<Uri> mUploadMessage = null;

    protected StartPageFragment mStartPageFragment = null;

    private final TintBrowserActivityStorage tintBrowserActivityStorage = new TintBrowserActivityStorage();
    private final BrowserSettingsStorage browserSettingsStorage = new BrowserSettingsStorage();

    public BaseUIManager(TintBrowserActivity activity) {
        mActivity = activity;

        mActionBar = mActivity.getActionBar();
        mFragmentManager = mActivity.getFragmentManager();

        mGeolocationPermissionsDialog = null;

        setupUI();
    }

    protected abstract String getCurrentUrl();

    protected abstract int getTabCount();

    protected abstract BaseWebViewFragment getWebViewFragmentByUUID(UUID fragmentId);

    protected abstract void showStartPage(BaseWebViewFragment webViewFragment);

    protected abstract void hideStartPage(BaseWebViewFragment webViewFragment);

    protected abstract void resetUI();

    protected void setApplicationButtonImage(Bitmap icon) {
        BitmapDrawable image = ApplicationUtils.getApplicationButtonImage(mActivity, icon);

        if (image != null) {
            mActionBar.setIcon(image);
        } else {
            mActionBar.setIcon(R.drawable.ic_launcher);
        }
    }

    protected void setupUI() {
        setFullScreenFromPreferences();
    }

    @Override
    public TintBrowserActivity getMainActivity() {
        return mActivity;
    }

    @Override
    public void addTab(boolean loadHomePage, boolean privateBrowsing) {
        if (loadHomePage) {
            String userStartPage = tintBrowserActivityStorage.getUserHomePage();
            addTab(userStartPage, false, privateBrowsing);
        } else {
            addTab(null, false, privateBrowsing);
        }
    }

    @Override
    public void togglePrivateBrowsing() {
        BaseWebViewFragment fragment = getCurrentWebViewFragment();
        if (fragment != null) {
            CustomWebView webView = fragment.getWebView();
            String currentUrl = webView.getUrl();

            fragment.setPrivateBrowsing(!fragment.isPrivateBrowsingEnabled());
            fragment.resetWebView();

            resetUI();
            loadUrl(currentUrl);
        }
    }

    @Override
    public void loadUrl(String url) {
        loadUrl(getCurrentWebViewFragment(), url);
    }

    @Override
    public void loadUrl(UUID tabId, String url, boolean loadInCurrentTabIfNotFound) {
        BaseWebViewFragment fragment = getWebViewFragmentByUUID(tabId);
        if (fragment != null) {
            loadUrl(fragment, url);
        } else {
            if (loadInCurrentTabIfNotFound) {
                loadUrl(url);
            }
        }
    }

    @Override
    public void loadRawUrl(UUID tabId, String url, boolean loadInCurrentTabIfNotFound) {
        BaseWebViewFragment fragment = getWebViewFragmentByUUID(tabId);
        if (fragment != null) {
            fragment.getWebView().loadRawUrl(url);
        } else {
            if (loadInCurrentTabIfNotFound) {
                getCurrentWebView().loadRawUrl(url);
            }
        }
    }

    @Override
    public void loadHomePage() {
        mHomePageLoading = true;
        loadUrl(tintBrowserActivityStorage.getUserHomePage());
    }

    @Override
    public void loadHomePage(UUID tabId, boolean loadInCurrentTabIfNotFound) {
        mHomePageLoading = true;
        loadUrl(
                tabId,
                tintBrowserActivityStorage.getUserHomePage(),
                loadInCurrentTabIfNotFound);
    }

    @Override
    public void loadCurrentUrl() {
        loadUrl(getCurrentUrl());
    }

    @Override
    public void openBookmarksActivityForResult() {
        Intent i = new Intent(mActivity, BookmarksActivity.class);
        mActivity.startActivityForResult(i, Constants.ACTIVITY_BOOKMARKS);
    }

    @Override
    public void addBookmarkFromCurrentPage() {
        Intent i = new Intent(mActivity, EditBookmarkActivity.class);

        i.putExtra(Constants.EXTRA_ID, (long) -1);
        i.putExtra(Constants.EXTRA_LABEL, getCurrentWebView().getTitle());
        i.putExtra(Constants.EXTRA_URL, getCurrentWebView().getUrl());

        mActivity.startActivity(i);
    }

    @Override
    public void shareCurrentPage() {
        WebView webView = getCurrentWebView();

        if (webView != null) {
            ApplicationUtils.sharePage(mActivity, webView.getTitle(), webView.getUrl());
        }
    }

    @Override
    public void startSearch() {
        WebView webView = getCurrentWebView();

        if (webView != null) {
            webView.showFindDialog(null, true);
        }
    }

    @Override
    public void clearFormData() {
        WebViewDatabase.getInstance(mActivity).clearFormData();
        getCurrentWebView().clearFormData();
    }

    @Override
    public void clearCache() {
        getCurrentWebView().clearCache(true);
    }

    @Override
    public void setHttpAuthUsernamePassword(String host, String realm, String username, String password) {
        getCurrentWebView().setHttpAuthUsernamePassword(host, realm, username, password);
    }

    @Override
    public CustomWebView getWebViewByTabId(UUID tabId) {
        BaseWebViewFragment fragment = getWebViewFragmentByUUID(tabId);
        if (fragment != null) {
            return fragment.getWebView();
        } else {
            return null;
        }
    }

    @Override
    public void setUploadMessage(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
    }

    @Override
    public ValueCallback<Uri> getUploadMessage() {
        return mUploadMessage;
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intent != null) {
            if (Intent.ACTION_VIEW.equals(intent.getAction()) ||
                    Intent.ACTION_MAIN.equals(intent.getAction())) {
                // ACTION_VIEW and ACTION_MAIN can specify an url to load.
                String url = intent.getDataString();

                if (!TextUtils.isEmpty(url)) {
                    if (!isCurrentTabReusable()) {
                        addTab(url, false, tintBrowserActivityStorage.getIncognitoByDefaultStatus());
                    } else {
                        loadUrl(url);
                    }
                } else {
                    // We do not have an url. Open a new tab if there is no tab currently opened,
                    // else do nothing.
                    if (getTabCount() <= 0) {
                        addTab(true, tintBrowserActivityStorage.getIncognitoByDefaultStatus());
                    }
                }
            } else if (Constants.ACTION_BROWSER_CONTEXT_MENU.equals(intent.getAction())) {
                if (intent.hasExtra(Constants.EXTRA_ACTION_ID)) {
                    int actionId = intent.getIntExtra(Constants.EXTRA_ACTION_ID, -1);
                    int intExtraHitResult = intent.getIntExtra(Constants.EXTRA_HIT_TEST_RESULT, -1);
                    boolean isIncognitoTab = intent.getBooleanExtra(Constants.EXTRA_INCOGNITO, false);
                    String url = intent.getStringExtra(Constants.EXTRA_URL);
                    BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor = new BrowserActivityContextMenuClickVisitor
                            (this, intExtraHitResult, actionId, isIncognitoTab, url);
                    BrowserActivityContextMenuOptions browserActivityContextMenuOptions = BrowserActivityContextMenuOptions.getById(actionId);
                    browserActivityContextMenuOptions.accept(browserActivityContextMenuVisitor);
                }
            }
        } else {
            addTab(true, false);
        }
    }

    @Override
    public boolean onKeyBack() {
        if (mCustomView != null) {
            onHideCustomView();
            return true;
        }

        return false;
    }

    @Override
    public void onPageFinished(final WebView view, final String url) {

        if (mHomePageLoading) {
            mHomePageLoading = false;
            if (browserSettingsStorage.isHomePageUrlUpdateNeeded()) {
                browserSettingsStorage.setHomePageUrlUpdateNeeded(false);
                tintBrowserActivityStorage.setUserHomePage(url);
            }
        }

        view.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (BookmarksWrapper.urlHasBookmark(mActivity.getContentResolver(), url, view.getOriginalUrl())) {
                    Picture p = view.capturePicture();

                    new ThumbnailSaver(mActivity.getContentResolver(),
                            url,
                            view.getOriginalUrl(),
                            p,
                            ApplicationUtils.getBookmarksThumbnailsDimensions(mActivity)).execute();
                }
            }
        }, 2000);
    }

    @Override
    public void onClientPageFinished(CustomWebView view, String url) {
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        if (view == getCurrentWebView()) {
            setApplicationButtonImage(icon);
        }
    }

    @Override
    public void onMainActivityPause() {
        CustomWebView webView = getCurrentWebView();
        if (webView != null) {
            webView.pauseTimers();
        }
    }

    @Override
    public void onMainActivityResume() {
        CustomWebView webView = getCurrentWebView();
        if (webView != null) {
            webView.resumeTimers();
        }
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }

        if (requestedOrientation == -1) {
            requestedOrientation = mActivity.getRequestedOrientation();
        }

        mOriginalOrientation = mActivity.getRequestedOrientation();
        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        mFullscreenContainer = new FullscreenHolder(mActivity);
        mFullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        decor.addView(mFullscreenContainer, COVER_SCREEN_PARAMS);
        mCustomView = view;

        mCustomViewCallback = callback;
        mActivity.setRequestedOrientation(requestedOrientation);
    }

    @Override
    public void onHideCustomView() {
        if (mCustomView == null)
            return;

        FrameLayout decor = (FrameLayout) mActivity.getWindow().getDecorView();
        decor.removeView(mFullscreenContainer);
        mFullscreenContainer = null;
        mCustomView = null;
        mCustomViewCallback.onCustomViewHidden();
        // Show the content view.
        mActivity.setRequestedOrientation(mOriginalOrientation);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
        if (mGeolocationPermissionsDialog == null) {
            mGeolocationPermissionsDialog = new GeolocationPermissionsDialog(mActivity);
        }

        mGeolocationPermissionsDialog.initialize(origin, callback);
        mGeolocationPermissionsDialog.show();

    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        if (mGeolocationPermissionsDialog != null) {
            mGeolocationPermissionsDialog.hide();
        }
    }

    @Override
    public void loadUrl(BaseWebViewFragment webViewFragment, String url) {
        CustomWebView webView = webViewFragment.getWebView();

        if (Constants.URL_ABOUT_START.equals(url)) {
            showStartPage(webViewFragment);

            // Check if there is no pb with this.
            // This recreate a new WebView, because i cannot found a way
            // to reset completely (history and display) a WebView.
            webViewFragment.resetWebView();
        } else {
            hideStartPage(webViewFragment);
            webView.loadUrl(url);
        }

        webView.requestFocus();
    }

    @Override
    public boolean isFullScreen() {
        return tintBrowserActivityStorage.isFullScreen();
    }

    @Override
    public void toggleFullScreen() {
        boolean newValue = !isFullScreen();
        tintBrowserActivityStorage.setFullScreen(newValue);
        setFullScreenFromPreferences();
    }


    @Override
    public void saveTabs() {
        String userStartPage = tintBrowserActivityStorage.getUserHomePage();

        Set<String> tabs = new HashSet<String>();
        for (BaseWebViewFragment f : getTabsFragments()) {
            if (!f.isStartPageShown() &&
                    !f.isWebViewOnUrl(userStartPage) &&
                    !f.isPrivateBrowsingEnabled()) {
                tabs.add(f.getWebView().getUrl());
            }
        }
        tintBrowserActivityStorage.saveTabs(tabs);
    }

    protected abstract Collection<BaseWebViewFragment> getTabsFragments();

    protected abstract void setFullScreenFromPreferences();

    protected boolean isStartPageShownOnCurrentTab() {
        BaseWebViewFragment currentWebViewFragment = getCurrentWebViewFragment();
        return currentWebViewFragment != null && currentWebViewFragment.isStartPageShown();
    }

    protected boolean isHomePageStartPage() {
        return Constants.URL_ABOUT_START.equals(tintBrowserActivityStorage.getUserHomePage());
    }

    /**
     * Check if the current tab can be reused to display an intent request.
     * A tab is reusable if it is on the user-defined start page.
     *
     * @return True if the current tab can be reused.
     */
    private boolean isCurrentTabReusable() {
        String homePageUrl = tintBrowserActivityStorage.getUserHomePage();
        BaseWebViewFragment currentWebViewFragment = getCurrentWebViewFragment();
        CustomWebView currentWebView = getCurrentWebView();

        return (currentWebViewFragment != null && currentWebViewFragment.isStartPageShown()) ||
                (currentWebView != null && homePageUrl != null && homePageUrl.equals(currentWebView.getUrl()));
    }

    static class FullscreenHolder extends FrameLayout {

        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }

        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }
}
