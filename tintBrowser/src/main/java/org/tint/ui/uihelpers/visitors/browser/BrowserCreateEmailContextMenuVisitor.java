package org.tint.ui.uihelpers.visitors.browser;

import android.content.Intent;
import android.net.Uri;
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
public class BrowserCreateEmailContextMenuVisitor extends NoopCreateHtmlNodeContextMenuVisitor {
    private final Menu menu;
    private final int resultType;
    private final String resultExtra;
    private final boolean mPrivateBrowsing;

    public BrowserCreateEmailContextMenuVisitor(Menu menu, WebView.HitTestResult result, boolean mPrivateBrowsing) {
        this.menu = menu;
        this.resultType = result.getType();
        this.resultExtra = result.getExtra();
        this.mPrivateBrowsing = mPrivateBrowsing;
    }

    @Override
    public void visitSendMail(BrowserActivityContextMenuOptions sendMail) {
        MenuItem item = menu.add(0, sendMail.getMenuItemId(), 0, R.string.ContextMenuSendEmail);
        item.setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(WebView.SCHEME_MAILTO + resultExtra)));
    }

    @Override
    public void visitShare(BrowserActivityContextMenuOptions share) {
        MenuItem item = menu.add(0, share.getMenuItemId(), 0, R.string.ContextMenuShareEmailUrl);
        item.setIntent(createIntent(Constants.ACTION_BROWSER_CONTEXT_MENU, share.getMenuItemId(), resultType, resultExtra, mPrivateBrowsing));
    }

    @Override
    public void visitCopy(BrowserActivityContextMenuOptions copy) {
        MenuItem item = menu.add(0, copy.getMenuItemId(), 0, R.string.ContextMenuCopyEmailUrl);
        item.setIntent(createIntent(Constants.ACTION_BROWSER_CONTEXT_MENU, copy.getMenuItemId(), resultType, resultExtra, mPrivateBrowsing));
    }
}
