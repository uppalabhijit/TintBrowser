package org.tint.ui.uihelpers.visitors;

/**
 * User: Abhijit
 * Date: 2016-06-09
 */
public interface BrowserActivityContextMenuVisitor {
    void visitOpen();

    void visitOpenInNewTab();

    void visitOpenInBackground();

    void visitDownload();

    void visitCopy();

    void visitSendMail();

    void visitShare();

    void visitDefault();
}
