package org.tint.ui.uihelpers.visitors.browser;

import java.util.List;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.MenuItem;

import org.tint.addons.AddonMenuItem;
import org.tint.controllers.ContextRegistry;
import org.tint.controllers.Controller;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.uihelpers.browser.BrowserActivityContextMenuOptions;
import org.tint.utils.Constants;

/**
 * User: Abhijit
 * Date: 2016-06-12
 */
public class NoopBrowserContextMenuVisitor implements BrowserActivityContextMenuVisitor {
    @Override
    public void visitOpen(BrowserActivityContextMenuOptions open) {
    }

    @Override
    public void visitOpenInNewTab(BrowserActivityContextMenuOptions openInNewTab) {
    }

    @Override
    public void visitOpenInBackground(BrowserActivityContextMenuOptions openInBackground) {
    }

    @Override
    public void visitDownload(BrowserActivityContextMenuOptions download) {
    }

    @Override
    public void visitCopy(BrowserActivityContextMenuOptions copy) {
    }

    @Override
    public void visitSendMail(BrowserActivityContextMenuOptions sendMail) {
    }

    @Override
    public void visitShare(BrowserActivityContextMenuOptions share) {
    }

    @Override
    public void visitDefault(BrowserActivityContextMenuOptions defaultOption) {
    }

    protected final Intent createIntent(String action, int actionId, int hitTestResult, String url, boolean mPrivateBrowsing) {
        Intent result = new Intent(ContextRegistry.get(), TintBrowserActivity.class);
        result.setAction(action);
        result.putExtra(Constants.EXTRA_ACTION_ID, actionId);
        result.putExtra(Constants.EXTRA_HIT_TEST_RESULT, hitTestResult);
        result.putExtra(Constants.EXTRA_URL, url);
        result.putExtra(Constants.EXTRA_INCOGNITO, mPrivateBrowsing);
        return result;
    }

    public void createContributedContextMenu(ContextMenu menu, String parentFragmentUUID, int hitTestResult, String url, boolean
            mPrivateBrowsing) {
        if (!mPrivateBrowsing) {
            MenuItem item;

            List<AddonMenuItem> contributedItems = Controller.getInstance().getAddonManager().getContributedLinkContextMenuItems(parentFragmentUUID,
                    hitTestResult, url);
            for (AddonMenuItem contribution : contributedItems) {
                item = menu.add(0, contribution.getAddon().getMenuId(), 0, contribution.getMenuItem());
                item.setIntent(createIntent(Constants.ACTION_BROWSER_CONTEXT_MENU, contribution.getAddon().getMenuId(), hitTestResult,
                        url, mPrivateBrowsing));
            }
        }
    }

    public void setHeaderTitle(ContextMenu contextMenu, String resultExtra) {
        contextMenu.setHeaderTitle(resultExtra);
    }
}
