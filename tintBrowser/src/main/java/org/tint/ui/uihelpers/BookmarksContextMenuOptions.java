package org.tint.ui.uihelpers;

import android.view.Menu;

/**
 * User: Abhijit
 * Date: 2016-06-04
 */
public enum BookmarksContextMenuOptions {
    OPEN_IN_TAB(Menu.FIRST) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitOpenInTab();
        }
    }, EDIT_BOOKMARK(Menu.FIRST + 1) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitEditBookmark();
        }
    }, COPY_URL(Menu.FIRST + 2) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitCopyUrl();
        }
    }, SHARE_URL(Menu.FIRST + 3) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitShareUrl();
        }
    }, DELETE_BOOKMARK(Menu
            .FIRST + 4) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitDeleteBookmark();
        }
    }, DELETE_FOLDER(Menu.FIRST + 5) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitDeleteFolder();
        }
    }, DEFAULT(-1) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitDefault();
        }
    };
    private final int itemId;

    BookmarksContextMenuOptions(int itemId) {
        this.itemId = itemId;
    }

    public static BookmarksContextMenuOptions getById(int itemId) {
        for (BookmarksContextMenuOptions bookmarksContextMenuOptions : values()) {
            if (bookmarksContextMenuOptions.itemId == itemId) {
                return bookmarksContextMenuOptions;
            }
        }
        return DEFAULT;
    }

    public abstract boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor);
}
