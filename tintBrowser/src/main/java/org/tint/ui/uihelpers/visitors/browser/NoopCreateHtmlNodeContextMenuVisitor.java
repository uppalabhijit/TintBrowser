package org.tint.ui.uihelpers.visitors.browser;

import java.util.List;

import android.view.ContextMenu;
import android.view.MenuItem;

import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.utils.Constants;

/**
 * User: Abhijit
 * Date: 2016-06-18
 */
public class NoopCreateHtmlNodeContextMenuVisitor extends NoopBrowserContextMenuVisitor {

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
