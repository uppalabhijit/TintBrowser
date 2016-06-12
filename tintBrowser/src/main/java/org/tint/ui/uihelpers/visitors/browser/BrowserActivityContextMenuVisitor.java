package org.tint.ui.uihelpers.visitors.browser;

import org.tint.ui.uihelpers.browser.BrowserActivityContextMenuOptions;

/**
 * User: Abhijit
 * Date: 2016-06-09
 */
public interface BrowserActivityContextMenuVisitor {
    void visitOpen(BrowserActivityContextMenuOptions open);

    void visitOpenInNewTab(BrowserActivityContextMenuOptions openInNewTab);

    void visitOpenInBackground(BrowserActivityContextMenuOptions openInBackground);

    void visitDownload(BrowserActivityContextMenuOptions download);

    void visitCopy(BrowserActivityContextMenuOptions copy);

    void visitSendMail(BrowserActivityContextMenuOptions sendMail);

    void visitShare(BrowserActivityContextMenuOptions share);

    void visitDefault(BrowserActivityContextMenuOptions defaultOption);
}
