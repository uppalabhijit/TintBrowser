package org.tint.ui.uihelpers.visitors;

import java.util.HashMap;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.Toast;

import org.tint.R;
import org.tint.controllers.ContextRegistry;
import org.tint.controllers.Controller;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.managers.UIManager;
import org.tint.ui.model.DownloadItem;
import org.tint.ui.uihelpers.BrowserActivityContextMenuOptions;
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
    public void visitOpen() {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(TintBrowserActivity.CONTEXT_MENU_OPEN);
        } else {
            uiManager.loadUrl(url);
        }
    }

    @Override
    public void visitOpenInNewTab() {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(TintBrowserActivity.CONTEXT_MENU_OPEN_IN_NEW_TAB, isIncognitoTab);
        } else {
            uiManager.addTab(url, false, isIncognitoTab);
        }
    }

    @Override
    public void visitOpenInBackground() {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(TintBrowserActivity.CONTEXT_MENU_OPEN_IN_BACKGROUND, isIncognitoTab);
        } else {
            uiManager.addTab(url, true, isIncognitoTab);
        }
    }

    @Override
    public void visitDownload() {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(TintBrowserActivity.CONTEXT_MENU_DOWNLOAD);
        } else {
            DownloadItem item = new DownloadItem(url);

            long id = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(item);
            item.setId(id);

            Controller.getInstance().getDownloadsList().add(item);
            Toast.makeText(context, String.format(ApplicationUtils.getStringFromResource(R.string.DownloadStart), item.getFileName()), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void visitCopy() {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(TintBrowserActivity.CONTEXT_MENU_COPY);
        } else {
            ApplicationUtils.copyTextToClipboard(context, url, ApplicationUtils.getStringFromResource(R.string.UrlCopyToastMessage));
        }
    }

    @Override
    public void visitSendMail() {

    }

    @Override
    public void visitShare() {
        if (HitTestResult.SRC_IMAGE_ANCHOR_TYPE == intExtraHitResult) {
            requestHrefNode(TintBrowserActivity.CONTEXT_MENU_SHARE);
        } else {
            ApplicationUtils.sharePage(context, null, url);
        }
    }

    @Override
    public void visitDefault() {
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
