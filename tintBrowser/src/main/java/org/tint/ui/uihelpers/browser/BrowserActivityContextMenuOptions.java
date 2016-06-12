package org.tint.ui.uihelpers.browser;

import android.view.ContextMenu;
import android.view.Menu;
import android.webkit.WebView;

import org.tint.ui.uihelpers.visitors.browser.BrowserActivityContextMenuVisitor;
import org.tint.ui.uihelpers.visitors.browser.BrowserCreateAnchorContextMenuVisitor;
import org.tint.ui.uihelpers.visitors.browser.BrowserCreateEmailContextMenuVisitor;
import org.tint.ui.uihelpers.visitors.browser.BrowserCreateImageContextMenuVisitor;

/**
 * User: Abhijit
 * Date: 2016-06-09
 */
public enum BrowserActivityContextMenuOptions {

    OPEN(Menu.FIRST + 10) {
        @Override
        public void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor) {
            browserActivityContextMenuVisitor.visitOpen(this);
        }
    },
    OPEN_IN_NEW_TAB(Menu.FIRST + 11) {
        @Override
        public void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor) {
            browserActivityContextMenuVisitor.visitOpenInNewTab(this);
        }
    },
    OPEN_IN_BACKGROUND(Menu.FIRST + 12) {
        @Override
        public void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor) {
            browserActivityContextMenuVisitor.visitOpenInBackground(this);
        }
    },
    DOWNLOAD(Menu.FIRST + 13) {
        @Override
        public void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor) {
            browserActivityContextMenuVisitor.visitDownload(this);
        }
    },
    COPY(Menu.FIRST + 14) {
        @Override
        public void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor) {
            browserActivityContextMenuVisitor.visitCopy(this);
        }
    },
    SEND_MAIL(Menu.FIRST + 15) {
        @Override
        public void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor) {
            browserActivityContextMenuVisitor.visitSendMail(this);
        }
    },
    SHARE(Menu.FIRST + 16) {
        @Override
        public void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor) {
            browserActivityContextMenuVisitor.visitShare(this);
        }
    },
    DEFAULT(-1) {
        @Override
        public void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor) {
            browserActivityContextMenuVisitor.visitDefault(this);
        }
    };

    private final int menuItemId;

    BrowserActivityContextMenuOptions(int menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public static BrowserActivityContextMenuOptions getById(int actionId) {
        for (BrowserActivityContextMenuOptions browserActivityContextMenuOptions : values()) {
            if (browserActivityContextMenuOptions.menuItemId == actionId) {
                return browserActivityContextMenuOptions;
            }
        }
        return DEFAULT;
    }


    public static void createImageItemMenu(ContextMenu menu, String parentFragmentUUID, WebView.HitTestResult result, boolean mPrivateBrowsing) {
        BrowserCreateImageContextMenuVisitor browserCreateImageContextMenuVisitor = new BrowserCreateImageContextMenuVisitor(menu, result, mPrivateBrowsing);
        OPEN.accept(browserCreateImageContextMenuVisitor);
        OPEN_IN_NEW_TAB.accept(browserCreateImageContextMenuVisitor);
        COPY.accept(browserCreateImageContextMenuVisitor);
        DOWNLOAD.accept(browserCreateImageContextMenuVisitor);
        SHARE.accept(browserCreateImageContextMenuVisitor);

        browserCreateImageContextMenuVisitor.createContributedContextMenu(menu, parentFragmentUUID, result.getType(), result.getExtra(),
                mPrivateBrowsing);
        browserCreateImageContextMenuVisitor.setHeaderTitle(menu, result.getExtra());
    }

    public static void createEmailItemMenu(ContextMenu menu, String parentFragmentUUID, WebView.HitTestResult result, boolean
            mPrivateBrowsing) {
        BrowserCreateEmailContextMenuVisitor browserCreateEmailContextMenuVisitor = new BrowserCreateEmailContextMenuVisitor(menu, result, mPrivateBrowsing);
        SEND_MAIL.accept(browserCreateEmailContextMenuVisitor);
        COPY.accept(browserCreateEmailContextMenuVisitor);
        SHARE.accept(browserCreateEmailContextMenuVisitor);

        browserCreateEmailContextMenuVisitor.createContributedContextMenu(menu, parentFragmentUUID, result.getType(), result.getExtra(),
                mPrivateBrowsing);
        browserCreateEmailContextMenuVisitor.setHeaderTitle(menu, result.getExtra());
    }

    public static void createAnchorItemMenu(ContextMenu menu, String parentFragmentUUID, WebView.HitTestResult result, boolean mPrivateBrowsing) {
        BrowserCreateAnchorContextMenuVisitor browserCreateAnchorContextMenuVisitor = new BrowserCreateAnchorContextMenuVisitor(menu, result, mPrivateBrowsing);
        OPEN.accept(browserCreateAnchorContextMenuVisitor);
        OPEN_IN_NEW_TAB.accept(browserCreateAnchorContextMenuVisitor);
        OPEN_IN_BACKGROUND.accept(browserCreateAnchorContextMenuVisitor);
        COPY.accept(browserCreateAnchorContextMenuVisitor);
        DOWNLOAD.accept(browserCreateAnchorContextMenuVisitor);
        SHARE.accept(browserCreateAnchorContextMenuVisitor);

        browserCreateAnchorContextMenuVisitor.createContributedContextMenu(menu, parentFragmentUUID, result.getType(), result.getExtra(),
                mPrivateBrowsing);
        browserCreateAnchorContextMenuVisitor.setHeaderTitle(menu, result.getExtra());
    }

    public abstract void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor);
}
