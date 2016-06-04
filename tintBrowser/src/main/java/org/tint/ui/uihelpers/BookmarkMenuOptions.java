package org.tint.ui.uihelpers;

import org.tint.R;

/**
 * Created by Abhijit on 2016-05-28.
 */
public enum BookmarkMenuOptions {

    GO_HOME(android.R.id.home) {
        @Override
        public boolean accept(BookmarksMenuVisitor bookmarksMenuVisitor) {
            return bookmarksMenuVisitor.visitHome();
        }
    }, ADD_BOOKMARK(R.id.BookmarksActivityMenuAddBookmark) {
        @Override
        public boolean accept(BookmarksMenuVisitor bookmarksMenuVisitor) {
            return bookmarksMenuVisitor.visitAddBookmarks();
        }
    }, SORT_BOOKMARK(R.id.BookmarksActivityMenuSortBookmarks) {
        @Override
        public boolean accept(BookmarksMenuVisitor bookmarksMenuVisitor) {
            return bookmarksMenuVisitor.visitSortBookmarks();
        }
    }, IMPORT_BOOKMARK(R.id.BookmarksActivityMenuImportHistoryBookmarks) {
        @Override
        public boolean accept(BookmarksMenuVisitor bookmarksMenuVisitor) {
            return bookmarksMenuVisitor.visitImportBookmarks();
        }
    }, EXPORT_BOOKMARK(R.id.BookmarksActivityMenuExportHistoryBookmarks) {
        @Override
        public boolean accept(BookmarksMenuVisitor bookmarksMenuVisitor) {
            return bookmarksMenuVisitor.visitExportBookmarks();
        }
    }, CLEAR_BOOKMARK_HISTORY(R.id.BookmarksActivityMenuClearHistoryBookmarks) {
        @Override
        public boolean accept(BookmarksMenuVisitor bookmarksMenuVisitor) {
            return bookmarksMenuVisitor.visitClearBookmarksAndHistory();
        }
    }, DEFAULT(-1) {
        @Override
        public boolean accept(BookmarksMenuVisitor bookmarksMenuVisitor) {
            return bookmarksMenuVisitor.visitDefault();
        }
    };

    private final int resId;

    BookmarkMenuOptions(int resId) {
        this.resId = resId;
    }

    public abstract boolean accept(BookmarksMenuVisitor bookmarksMenuVisitor);

    public static BookmarkMenuOptions getById(int resId) {
        for (BookmarkMenuOptions bookmarkMenuOptions : values()) {
            if (bookmarkMenuOptions.resId == resId) {
                return bookmarkMenuOptions;
            }
        }
        return DEFAULT;
    }
}