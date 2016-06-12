package org.tint.ui.uihelpers.browser;

import android.view.Menu;

import org.tint.ui.uihelpers.visitors.browser.BrowserActivityContextMenuVisitor;

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

    public abstract void accept(BrowserActivityContextMenuVisitor browserActivityContextMenuVisitor);
}
