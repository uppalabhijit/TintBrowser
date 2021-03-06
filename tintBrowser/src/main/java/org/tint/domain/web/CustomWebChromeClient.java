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

package org.tint.domain.web;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.*;
import android.webkit.GeolocationPermissions.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tint.R;
import org.tint.storage.BrowserSettingsStorage;
import org.tint.tasks.UpdateFaviconTask;
import org.tint.tasks.UpdateHistoryTask;
import org.tint.ui.managers.UIManager;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

public class CustomWebChromeClient extends WebChromeClient {

    private UIManager mUIManager;

    private Bitmap mDefaultVideoPoster = null;
    private View mVideoProgressView = null;

    public CustomWebChromeClient(UIManager uiManager) {
        mUIManager = uiManager;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        mUIManager.onProgressChanged(view, newProgress);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        mUIManager.onReceivedTitle(view, title);

        if (!view.isPrivateBrowsingEnabled()) {
            UpdateHistoryTask task = new UpdateHistoryTask(mUIManager.getMainActivity());
            task.execute(view.getTitle(), view.getUrl(), view.getOriginalUrl());
        }
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        mUIManager.onReceivedIcon(view, icon);

        UpdateFaviconTask task = new UpdateFaviconTask(mUIManager.getMainActivity().getContentResolver(), view.getUrl(), view.getOriginalUrl(), icon);
        task.execute();
    }

//    @Override
//    public boolean onCreateWindow(WebView view, final boolean dialog, final boolean userGesture, final Message resultMsg) {
//        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
//        CustomWebView curentWebView = mUIManager.getCurrentWebView();
//        mUIManager.addTab(false, curentWebView.isPrivateBrowsingEnabled());
//        transport.setWebView(mUIManager.getCurrentWebView());
//        resultMsg.sendToTarget();
//        return true;
//    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
        mUIManager.setUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType((acceptType == null || acceptType.isEmpty()) ? "*/*" : acceptType);
        mUIManager.getMainActivity().startActivityForResult(
                Intent.createChooser(i, mUIManager.getMainActivity().getString(R.string.FileChooserPrompt)),
                Constants.ACTIVITY_OPEN_FILE_CHOOSER);
    }

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        mUIManager.setUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        mUIManager.getMainActivity().startActivityForResult(
                Intent.createChooser(i, mUIManager.getMainActivity().getString(R.string.FileChooserPrompt)),
                Constants.ACTIVITY_OPEN_FILE_CHOOSER);
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        if (mDefaultVideoPoster == null) {
            mDefaultVideoPoster = ApplicationUtils.getBitmpaFromResource(R.drawable.default_video_poster);
        }
        return mDefaultVideoPoster;
    }

    @Override
    public View getVideoLoadingProgressView() {
        if (mVideoProgressView == null) {
            mVideoProgressView = ApplicationUtils.inflateView(R.layout.video_loading_progress);
        }
        return mVideoProgressView;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        return IJsPromptsManager.Factory.create(mUIManager).onJsAlert(view, url, message, result);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        return IJsPromptsManager.Factory.create(mUIManager).onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
        return IJsPromptsManager.Factory.create(mUIManager).onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public void onHideCustomView() {
        super.onHideCustomView();
        mUIManager.onHideCustomView();
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        super.onShowCustomView(view, requestedOrientation, callback);
        mUIManager.onShowCustomView(view, requestedOrientation, callback);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        super.onShowCustomView(view, callback);
        mUIManager.onShowCustomView(view, -1, callback);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
        mUIManager.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        mUIManager.onGeolocationPermissionsHidePrompt();
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        if (new BrowserSettingsStorage().isJSLogsToConsoleEnabled()) {
            Log.d("TintJS", cm.sourceId() + ":" + cm.lineNumber() + " " + cm.message());
        }
        return true;
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }
}
