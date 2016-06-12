package org.tint.ui.uihelpers.visitors.browser;

import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import org.tint.R;
import org.tint.ui.uihelpers.browser.BrowserActivityContextMenuOptions;
import org.tint.utils.Constants;

/**
 * User: Abhijit
 * Date: 2016-06-12
 */
public class BrowserCreateImageContextMenuVisitor extends NoopBrowserContextMenuVisitor {
    private final Menu menu;
    private final WebView.HitTestResult result;
    private final int resultType;
    private final String resultExtra;
    private final boolean mPrivateBrowsing;

    public BrowserCreateImageContextMenuVisitor(Menu menu, WebView.HitTestResult result, boolean mPrivateBrowsing) {
        this.menu = menu;
        this.result = result;
        this.resultType = result.getType();
        this.resultExtra = result.getExtra();
        this.mPrivateBrowsing = mPrivateBrowsing;
    }

    @Override
    public void visitOpen(BrowserActivityContextMenuOptions open) {
        MenuItem item = menu.add(0, open.getMenuItemId(), 0, R.string.ContextMenuViewImage);
        item.setIntent(createIntent(Constants.ACTION_BROWSER_CONTEXT_MENU, open.getMenuItemId(), resultType, resultExtra, mPrivateBrowsing));
    }

    @Override
    public void visitOpenInNewTab(BrowserActivityContextMenuOptions openInNewTab) {
        MenuItem item = menu.add(0, openInNewTab.getMenuItemId(), 0, R.string.ContextMenuViewImageInNewTab);
        item.setIntent(createIntent(Constants.ACTION_BROWSER_CONTEXT_MENU, openInNewTab.getMenuItemId(), resultType, resultExtra, mPrivateBrowsing));
    }

    @Override
    public void visitCopy(BrowserActivityContextMenuOptions copy) {
        MenuItem item = menu.add(0, copy.getMenuItemId(), 0, R.string.ContextMenuCopyImageUrl);
        item.setIntent(createIntent(Constants.ACTION_BROWSER_CONTEXT_MENU, copy.getMenuItemId(), resultType, resultExtra, mPrivateBrowsing));
    }

    @Override
    public void visitDownload(BrowserActivityContextMenuOptions download) {
        MenuItem item = menu.add(0, download.getMenuItemId(), 0, R.string.ContextMenuDownloadImage);
        item.setIntent(createIntent(Constants.ACTION_BROWSER_CONTEXT_MENU, download.getMenuItemId(), resultType, resultExtra, mPrivateBrowsing));
    }

    @Override
    public void visitShare(BrowserActivityContextMenuOptions share) {
        MenuItem item = menu.add(0, share.getMenuItemId(), 0, R.string.ContextMenuShareImageUrl);
        item.setIntent(createIntent(Constants.ACTION_BROWSER_CONTEXT_MENU, share.getMenuItemId(), resultType, resultExtra, mPrivateBrowsing));
    }
}
