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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.*;
import android.widget.EditText;

import org.tint.R;
import org.tint.domain.ssl.ShowUserSslExceptionVisitor;
import org.tint.domain.ssl.SslAuthorityStatus;
import org.tint.domain.ssl.SslExceptionsWrapper;
import org.tint.ui.managers.UIManager;
import org.tint.ui.webview.CustomWebView;

public class CustomWebViewClient extends WebViewClient {

    private static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile(
            "(?i)" + // switch on case insensitive matching
                    "(" + // begin group for schema
                    "(?:http|https|file):\\/\\/" +
                    "|(?:inline|data|about|javascript):" +
                    ")" +
                    "(.*)");

    private UIManager mUIManager;

    private Message mDontResend;
    private Message mResend;

    public CustomWebViewClient(UIManager uiManager) {
        mUIManager = uiManager;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        mUIManager.onPageStarted(view, url, favicon);
        ((CustomWebView) view).onClientPageStarted(url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        mUIManager.onPageFinished(view, url);
        CustomWebView customWebView = (CustomWebView) view;
        customWebView.onClientPageFinished(url);
        customWebView.maintainScrollPositionIfUserHasScrolled();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return checkUrlLoading(url);
    }

    private boolean checkUrlLoading(String url) {
        Intent intent;

        try {
            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
        } catch (URISyntaxException e) {
            Log.w("CustomWebViewClient", "Bad URI " + url + ": " + e.getMessage());
            return false;
        }

        if (mUIManager.getMainActivity().getPackageManager().resolveActivity(intent, 0) == null) {
            String packagename = intent.getPackage();
            if (packagename != null) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:" + packagename));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                mUIManager.getMainActivity().startActivity(intent);

                return true;
            } else {
                return false;
            }
        }

        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setComponent(null);

        Matcher m = ACCEPTED_URI_SCHEMA.matcher(url);
        if (m.matches() && !isSpecializedHandlerAvailable(intent)) {
            return false;
        }

        try {
            if (mUIManager.getMainActivity().startActivityIfNeeded(intent, -1)) {
                return true;
            }
        } catch (ActivityNotFoundException ex) {
            // ignore the error. If no application can handle the URL,
            // eg about:blank, assume the browser can handle it.
        }

        return false;
    }

    /**
     * Search for intent handlers that are specific to this URL
     * aka, specialized apps like google maps or youtube
     */
    private boolean isSpecializedHandlerAvailable(Intent intent) {
        PackageManager pm = mUIManager.getMainActivity().getPackageManager();
        List<ResolveInfo> handlers = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        if (handlers == null || handlers.size() == 0) {
            return false;
        }

        for (ResolveInfo resolveInfo : handlers) {
            IntentFilter filter = resolveInfo.filter;
            if (filter == null) {
                // No intent filter matches this intent?
                // Error on the side of staying in the browser, ignore
                continue;
            }

            if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) {
                // Generic handler, skip
                continue;
            }

            return true;
        }

        return false;
    }

    @Override
    public void onReceivedSslError(final WebView view, final SslErrorHandler handler, SslError error) {

        boolean hasAuthority = false;
        String authority = view.getResources().getString(R.string.UnknownAutority);
        if (error.getUrl() != null) {
            try {
                URL url = new URL(error.getUrl());
                authority = url.getAuthority();
                hasAuthority = true;
            } catch (MalformedURLException e) {
                hasAuthority = false;
            }
        }

        ShowUserSslExceptionVisitor sslExceptionVisitor = new ShowUserSslExceptionVisitor(authority, handler, error);
        if (hasAuthority) {
            SslAuthorityStatus result = SslExceptionsWrapper.getStatusForAuthority(view.getContext().getContentResolver(), authority);
            result.handleSslError(sslExceptionVisitor);
        }
        sslExceptionVisitor.askUserIfNeeded();

    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, final String host, final String realm) {
        String username = null;
        String password = null;

        boolean reuseHttpAuthUsernamePassword = handler.useHttpAuthUsernamePassword();

        if (reuseHttpAuthUsernamePassword && view != null) {
            String[] credentials = view.getHttpAuthUsernamePassword(
                    host, realm);
            if (credentials != null && credentials.length == 2) {
                username = credentials[0];
                password = credentials[1];
            }
        }

        if (username != null && password != null) {
            handler.proceed(username, password);
        } else {
            LayoutInflater factory = LayoutInflater.from(mUIManager.getMainActivity());
            final View v = factory.inflate(R.layout.http_authentication_dialog, null);

            if (username != null) {
                ((EditText) v.findViewById(R.id.username_edit)).setText(username);
            }
            if (password != null) {
                ((EditText) v.findViewById(R.id.password_edit)).setText(password);
            }

            AlertDialog dialog = new AlertDialog.Builder(mUIManager.getMainActivity())
                    .setTitle(String.format(mUIManager.getMainActivity().getString(R.string.HttpAuthenticationDialogDialogTitle), host, realm))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setView(v)
                    .setPositiveButton(R.string.Proceed,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    String nm = ((EditText) v
                                            .findViewById(R.id.username_edit))
                                            .getText().toString();
                                    String pw = ((EditText) v
                                            .findViewById(R.id.password_edit))
                                            .getText().toString();
                                    mUIManager.setHttpAuthUsernamePassword(host, realm, nm, pw);
                                    handler.proceed(nm, pw);
                                }
                            })
                    .setNegativeButton(R.string.Cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    handler.cancel();
                                }
                            })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            handler.cancel();
                        }
                    })
                    .create();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();

            v.findViewById(R.id.username_edit).requestFocus();
        }
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {

        mDontResend = dontResend;
        mResend = resend;

        new AlertDialog.Builder(mUIManager.getMainActivity()).setTitle(R.string.FormResubmitTitle)
                .setMessage(R.string.FormResubmitMessage)
                .setPositiveButton(R.string.OK,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (mResend != null) {
                                    mResend.sendToTarget();
                                    mResend = null;
                                    mDontResend = null;
                                }
                            }
                        }).setNegativeButton(R.string.Cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        if (mDontResend != null) {
                            mDontResend.sendToTarget();
                            mResend = null;
                            mDontResend = null;
                        }
                    }
                }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (mDontResend != null) {
                    mDontResend.sendToTarget();
                    mResend = null;
                    mDontResend = null;
                }
            }
        }).show();
    }


    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        IAdBlocker iAdBlocker = IAdBlocker.Factory.get();
        if (iAdBlocker.isAd(url)) {
            getLogger().debug("[CustomWebViewClient][shouldInterceptRequest] found ad at '{}'", url);
            return iAdBlocker.createEmptyResource();
        } else {
            return super.shouldInterceptRequest(view, url);
        }
    }
}
