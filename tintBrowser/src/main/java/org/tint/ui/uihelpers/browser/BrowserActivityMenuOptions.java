package org.tint.ui.uihelpers.browser;

import org.tint.R;
import org.tint.ui.uihelpers.visitors.browser.BrowserActivityMenuVisitor;

/**
 * Created by Abhijit on 2016-05-28.
 */
public enum BrowserActivityMenuOptions {
    ADD_TAB(R.id.MainActivity_MenuAddTab) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitAddTab();
        }
    }, CLOSE_TAB(R.id.MainActivity_MenuCloseTab) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitCloseTab();
        }
    }, ADD_BOOKMARK(R.id.MainActivity_MenuAddBookmark) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitAddBookmark();
        }
    }, MENU_BOOKMARK(R.id.MainActivity_MenuBookmarks) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitMenuBookmarks();
        }
    }, INCOGNITO_TAB(R.id.MainActivity_MenuIncognitoTab) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitIncognitoTab();
        }
    }, FULL_SCREEN(R.id.MainActivity_MenuFullScreen) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitFullScreen();
        }
    }, SHARE(R.id.MainActivity_MenuSharePage) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitShare();
        }
    }, SEARCH(R.id.MainActivity_MenuSearch) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitSearch();
        }
    }, SETTINGS(R.id.MainActivity_MenuPreferences) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitSettings();
        }
    }, DEFAULT(-1) {
        @Override
        public boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor) {
            return browserActivityMenuVisitor.visitDefault();
        }
    };
    private final int resId;

    public int getResId() {
        return resId;
    }

    BrowserActivityMenuOptions(int resId) {
        this.resId = resId;
    }

    public static BrowserActivityMenuOptions getById(int resId) {
        for (BrowserActivityMenuOptions browserActivityMenuOptions : values()) {
            if (browserActivityMenuOptions.resId == resId) {
                return browserActivityMenuOptions;
            }
        }
        return DEFAULT;
    }

    public abstract boolean accept(BrowserActivityMenuVisitor browserActivityMenuVisitor);
}
