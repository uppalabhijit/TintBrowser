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
}
