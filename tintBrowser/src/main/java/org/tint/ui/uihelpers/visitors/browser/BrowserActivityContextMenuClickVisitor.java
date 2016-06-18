package org.tint.ui.uihelpers.visitors.browser;

import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;

import org.tint.R;
import org.tint.controllers.ContextRegistry;
import org.tint.controllers.Controller;
import org.tint.domain.download.TintDownloadManager;
import org.tint.ui.managers.UIManager;
import org.tint.ui.uihelpers.browser.BrowserActivityContextMenuOptions;
import org.tint.utils.ApplicationUtils;

/**
 * User: Abhijit
 * Date: 2016-06-09
 */
public class BrowserActivityContextMenuClickVisitor implements BrowserActivityContextMenuVisitor {
    private final Context context;
    private final UIManager uiManager;
    private final MessageHandler messageHandler;
    private final int intExtraHitResult;
    private final int intActionId;
    private final boolean isIncognitoTab;
    private final String url;
    private static final int FOCUS_NODE_HREF = 102;

    public BrowserActivityContextMenuClickVisitor(UIManager uiManager, int intExtraHitResult, int intActionId, boolean isIncognitoTab, String url) {
        this.context = ContextRegistry.get();
        this.uiManager = uiManager;
        this.messageHandler = new MessageHandler(uiManager);
        this.intExtraHitResult = intExtraHitResult;
        this.intActionId = intActionId;
        this.isIncognitoTab = isIncognitoTab;
        this.url = url;
    }

    @Override
    public void visitOpen(BrowserActivityContextMenuOptions open) {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(open.getMenuItemId());
        } else {
            uiManager.loadUrl(url);
        }
    }

    @Override
    public void visitOpenInNewTab(BrowserActivityContextMenuOptions openInNewTab) {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(openInNewTab.getMenuItemId(), isIncognitoTab);
        } else {
            uiManager.addTab(url, false, isIncognitoTab);
        }
    }

    @Override
    public void visitOpenInBackground(BrowserActivityContextMenuOptions openInBackground) {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(openInBackground.getMenuItemId(), isIncognitoTab);
        } else {
            uiManager.addTab(url, true, isIncognitoTab);
        }
    }

    @Override
    public void visitDownload(BrowserActivityContextMenuOptions download) {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(download.getMenuItemId());
        } else {
            new TintDownloadManager().startDownload(url);
        }
    }

    @Override
    public void visitCopy(BrowserActivityContextMenuOptions copy) {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(copy.getMenuItemId());
        } else {
            ApplicationUtils.copyTextToClipboard(context, url, ApplicationUtils.getStringFromResource(R.string.UrlCopyToastMessage));
        }
    }

    @Override
    public void visitSendMail(BrowserActivityContextMenuOptions sendMail) {

    }

    @Override
    public void visitShare(BrowserActivityContextMenuOptions share) {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(share.getMenuItemId());
        } else {
            ApplicationUtils.sharePage(context, null, url);
        }
    }

    @Override
    public void visitDefault(BrowserActivityContextMenuOptions defaultOption) {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(intActionId);
        } else {
            Controller.getInstance().getAddonManager().onContributedContextLinkMenuItemSelected(
                    context,
                    intActionId,
                    intExtraHitResult,
                    url,
                    uiManager.getCurrentWebView());
        }
    }

    private void requestHrefNode(int action) {
        requestHrefNode(action, false);
    }

    private void requestHrefNode(int action, boolean incognito) {
        WebView webView = uiManager.getCurrentWebView();

        if (webView != null) {
            final HashMap<String, WebView> hrefMap = new HashMap<String, WebView>();
            hrefMap.put("webview", webView);

            final Message msg = messageHandler.obtainMessage(
                    FOCUS_NODE_HREF,
                    action,
                    incognito ? 1 : 0,
                    hrefMap);

            webView.requestFocusNodeHref(msg);
        }
    }

    private static class MessageHandler extends Handler {
        private final UIManager uiManager;

        private MessageHandler(UIManager uiManager) {
            this.uiManager = uiManager;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FOCUS_NODE_HREF:
                    String url = (String) msg.getData().get("url");
                    String src = (String) msg.getData().get("src");

                    if (url == "") {
                        url = src;
                    }

                    if (TextUtils.isEmpty(url)) {
                        break;
                    }

                    boolean isIncognitoTab = msg.arg2 > 0 ? true : false;

                    int actionId = msg.arg1;
                    int intExtraHitResult = HitTestResult.SRC_IMAGE_ANCHOR_TYPE - 1;

                    BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor = new BrowserActivityContextMenuClickVisitor
                            (uiManager, intExtraHitResult, actionId, isIncognitoTab, url);
                    BrowserActivityContextMenuOptions browserActivityContextMenuOptions = BrowserActivityContextMenuOptions.getById(actionId);
                    browserActivityContextMenuOptions.accept(browserActivityContextMenuVisitor);

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
