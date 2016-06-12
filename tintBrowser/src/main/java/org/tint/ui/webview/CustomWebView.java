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

package org.tint.ui.webview;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.Toast;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.domain.utils.UrlUtils;
import org.tint.ui.dialogs.DownloadConfirmDialog;
import org.tint.ui.fragments.BaseWebViewFragment;
import org.tint.ui.managers.UIManager;
import org.tint.ui.model.DownloadItem;
import org.tint.ui.uihelpers.browser.HtmlNode;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

public class CustomWebView extends WebView implements DownloadListener, DownloadConfirmDialog.IUserActionListener {

    private UIManager mUIManager;
    private Context mContext;
    private BaseWebViewFragment mParentFragment;

    private boolean mIsLoading = false;
    private boolean mPrivateBrowsing = false;

    private static boolean sMethodsLoaded = false;
    private static Method sWebSettingsSetProperty = null;

    public CustomWebView(UIManager uiManager, boolean privateBrowsing) {
        this(uiManager.getMainActivity(), null, privateBrowsing);
        mUIManager = uiManager;
    }

    // Used only by edit mode (UI designer)
    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.webViewStyle);
        mContext = context;
    }

    public CustomWebView(Context context, AttributeSet attrs, boolean privateBrowsing) {
        super(context, attrs, android.R.attr.webViewStyle);
        mPrivateBrowsing = privateBrowsing;

        mContext = context;

        if (!isInEditMode()) {

            if (!sMethodsLoaded) {
                loadMethods();
            }

            loadSettings();
            setupContextMenu();
        }
    }

    public void setParentFragment(BaseWebViewFragment parentFragment) {
        mParentFragment = parentFragment;
    }

    public BaseWebViewFragment getParentFragment() {
        return mParentFragment;
    }

    public UUID getParentFragmentUUID() {
        return mParentFragment.getUUID();
    }

    public boolean isLoading() {
        return mIsLoading;
    }

    public boolean isPrivateBrowsingEnabled() {
        return mPrivateBrowsing;
    }

    @Override
    public void loadUrl(String url) {
        if ((url != null) &&
                (url.length() > 0)) {

            if (UrlUtils.isUrl(url)) {
                url = UrlUtils.checkUrl(url);
            } else {
                url = UrlUtils.getSearchUrl(mContext, url);
            }

            if (Constants.URL_ABOUT_TUTORIAL.equals(url)) {
                loadDataWithBaseURL(
                        "file:///android_asset/",
                        ApplicationUtils.getStringFromRawResource(mContext, R.raw.phone_tutorial_html),
                        "text/html",
                        "UTF-8",
                        Constants.URL_ABOUT_TUTORIAL);
            } else {
                super.loadUrl(url);
            }
        }
    }

    public void loadRawUrl(String url) {
        super.loadUrl(url);
    }

    public void onClientPageStarted(String url) {
        mIsLoading = true;

        if (!isPrivateBrowsingEnabled()) {
            Controller.getInstance().getAddonManager().onPageStarted(mContext, this, url);
        }
    }

    public void onClientPageFinished(String url) {
        mIsLoading = false;

        if (!isPrivateBrowsingEnabled()) {
            Controller.getInstance().getAddonManager().onPageFinished(mContext, this, url);
        }

        mUIManager.onClientPageFinished(this, url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void loadSettings() {
        WebSettings settings = getSettings();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        settings.setJavaScriptEnabled(prefs.getBoolean(Constants.PREFERENCE_ENABLE_JAVASCRIPT, true));
        settings.setLoadsImagesAutomatically(prefs.getBoolean(Constants.PREFERENCE_ENABLE_IMAGES, true));
        settings.setUseWideViewPort(prefs.getBoolean(Constants.PREFERENCE_USE_WIDE_VIEWPORT, true));
        settings.setLoadWithOverviewMode(prefs.getBoolean(Constants.PREFERENCE_LOAD_WITH_OVERVIEW, false));

        settings.setGeolocationEnabled(prefs.getBoolean(Constants.PREFERENCE_ENABLE_GEOLOCATION, true));
        settings.setSaveFormData(prefs.getBoolean(Constants.PREFERENCE_REMEMBER_FORM_DATA, true));
        settings.setSavePassword(prefs.getBoolean(Constants.PREFERENCE_REMEMBER_PASSWORDS, true));

        settings.setTextZoom(prefs.getInt(Constants.PREFERENCE_TEXT_SCALING, 100));

        int minimumFontSize = prefs.getInt(Constants.PREFERENCE_MINIMUM_FONT_SIZE, 1);
        settings.setMinimumFontSize(minimumFontSize);
        settings.setMinimumLogicalFontSize(minimumFontSize);

        boolean useInvertedDisplay = prefs.getBoolean(Constants.PREFERENCE_INVERTED_DISPLAY, false);
        setWebSettingsProperty(settings, "inverted", useInvertedDisplay ? "true" : "false");

        if (useInvertedDisplay) {
            setWebSettingsProperty(settings,
                    "inverted_contrast",
                    Float.toString(prefs.getInt(Constants.PREFERENCE_INVERTED_DISPLAY_CONTRAST, 100) / 100f));
        }

        settings.setUserAgentString(prefs.getString(Constants.PREFERENCE_USER_AGENT, Constants.USER_AGENT_ANDROID));
        settings.setPluginState(PluginState.valueOf(prefs.getString(Constants.PREFERENCE_PLUGINS, PluginState.ON_DEMAND.toString())));

        CookieManager.getInstance().setAcceptCookie(prefs.getBoolean(Constants.PREFERENCE_ACCEPT_COOKIES, true));

        settings.setSupportZoom(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(true);
        settings.setSupportMultipleWindows(true);
        settings.setEnableSmoothTransition(true);

        if (mPrivateBrowsing) {
            settings.setGeolocationEnabled(false);
            settings.setSaveFormData(false);
            settings.setSavePassword(false);

            settings.setAppCacheEnabled(false);
            settings.setDatabaseEnabled(false);
            settings.setDomStorageEnabled(false);
        } else {
            // HTML5 API flags
            settings.setAppCacheEnabled(true);
            settings.setDatabaseEnabled(true);
            settings.setDomStorageEnabled(true);

            // HTML5 configuration settings.
            settings.setAppCacheMaxSize(3 * 1024 * 1024);
            settings.setAppCachePath(mContext.getDir("appcache", 0).getPath());
            settings.setDatabasePath(mContext.getDir("databases", 0).getPath());
            settings.setGeolocationDatabasePath(mContext.getDir("geolocation", 0).getPath());
        }

        setLongClickable(true);
        setDownloadListener(this);
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        DownloadItem item = new DownloadItem(url);
        item.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url));

        String fileName = item.getFileName();
        BasicHeader header = new BasicHeader("Content-Disposition", contentDisposition);
        HeaderElement[] helelms = header.getElements();
        if (helelms.length > 0) {
            HeaderElement helem = helelms[0];
            if (helem.getName().equalsIgnoreCase("attachment")) {
                NameValuePair nmv = helem.getParameterByName("filename");
                if (nmv != null) {
                    fileName = nmv.getValue();
                }
            }
        }
        item.setFilename(fileName);
        item.setIncognito(isPrivateBrowsingEnabled());

        DownloadConfirmDialog dialog = new DownloadConfirmDialog(getContext())
                .setDownloadItem(item)
                .setCallbackListener(this);
        dialog.show();
    }

    @Override
    public void onAcceptDownload(DownloadItem item) {
        long id = ((DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(item);
        item.setId(id);

        Controller.getInstance().getDownloadsList().add(item);

        Toast.makeText(mContext, String.format(mContext.getString(R.string.DownloadStart), item.getFileName()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDenyDownload() {
    }

    private void setupContextMenu() {
        setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                HitTestResult result = ((android.webkit.WebView) v).getHitTestResult();
                int resultType = result.getType();
                HtmlNode.getFromResultType(resultType).execute(menu, CustomWebView.this.getParentFragmentUUID().toString(), result, isPrivateBrowsingEnabled());
            }
        });
    }

    private static void loadMethods() {
        try {

            // 15 is ICS 2nd release.
            if (android.os.Build.VERSION.SDK_INT > 15) {
                // WebSettings became abstract in JB, and "setProperty" moved to the concrete class, WebSettingsClassic,
                // not present in the SDK. So we must look for the class first, then for the methods.
                ClassLoader classLoader = CustomWebView.class.getClassLoader();
                Class<?> webSettingsClassicClass = classLoader.loadClass("android.webkit.WebSettingsClassic");
                sWebSettingsSetProperty = webSettingsClassicClass.getMethod("setProperty", new Class[]{String.class, String.class});
            } else {
                sWebSettingsSetProperty = WebSettings.class.getMethod("setProperty", new Class[]{String.class, String.class});
            }

        } catch (NoSuchMethodException e) {
            Log.e("CustomWebView", "loadMethods(): " + e.getMessage());
            sWebSettingsSetProperty = null;
        } catch (ClassNotFoundException e) {
            Log.e("CustomWebView", "loadMethods(): " + e.getMessage());
            sWebSettingsSetProperty = null;
        }

        sMethodsLoaded = true;
    }

    private static void setWebSettingsProperty(WebSettings settings, String key, String value) {
        if (sWebSettingsSetProperty != null) {
            try {
                sWebSettingsSetProperty.invoke(settings, key, value);
            } catch (IllegalArgumentException e) {
                Log.e("CustomWebView", "setWebSettingsProperty(): " + e.getMessage());
            } catch (IllegalAccessException e) {
                Log.e("CustomWebView", "setWebSettingsProperty(): " + e.getMessage());
            } catch (InvocationTargetException e) {
                Log.e("CustomWebView", "setWebSettingsProperty(): " + e.getMessage());
            }
        }
    }

}
