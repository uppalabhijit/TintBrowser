package org.tint.ui.uihelpers;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.ContextMenu;
import android.view.Menu;

import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.ui.model.BookmarkHistoryItem;
import org.tint.ui.managers.UIManager;
import org.tint.ui.uihelpers.visitors.BookmarksContextMenuVisitor;
import org.tint.utils.ApplicationUtils;

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

        @Override
        protected void addToContextMenu(ContextMenu contextMenu) {
            contextMenu.add(0, OPEN_IN_TAB.itemId, 0, R.string.OpenInTab);
        }
    }, EDIT_BOOKMARK(Menu.FIRST + 1) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitEditBookmark();
        }

        @Override
        protected void addToContextMenu(ContextMenu contextMenu) {
            contextMenu.add(0, EDIT_BOOKMARK.itemId, 0, R.string.EditBookmark);
        }
    }, COPY_URL(Menu.FIRST + 2) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitCopyUrl();
        }

        @Override
        protected void addToContextMenu(ContextMenu contextMenu) {
            contextMenu.add(0, COPY_URL.itemId, 0, R.string.CopyUrl);
        }
    }, SHARE_URL(Menu.FIRST + 3) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitShareUrl();
        }

        @Override
        protected void addToContextMenu(ContextMenu contextMenu) {
            contextMenu.add(0, SHARE_URL.itemId, 0, R.string.ContextMenuShareUrl);
        }
    }, DELETE_BOOKMARK(Menu.FIRST + 4) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitDeleteItem();
        }

        @Override
        protected void addToContextMenu(ContextMenu contextMenu) {
            contextMenu.add(0, DELETE_BOOKMARK.itemId, 0, R.string.DeleteBookmark);
        }
    }, DELETE_FOLDER(Menu.FIRST + 5) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitDeleteFolder();
        }

        @Override
        protected void addToContextMenu(ContextMenu contextMenu) {
            contextMenu.add(0, DELETE_FOLDER.itemId, 0, R.string.DeleteFolder);
        }

        @Override
        protected boolean isValidForFolders() {
            return true;
        }
    }, DEFAULT(-1) {
        @Override
        public boolean accept(BookmarksContextMenuVisitor bookmarksContextMenuVisitor) {
            return bookmarksContextMenuVisitor.visitDefault();
        }

        @Override
        protected void addToContextMenu(ContextMenu contextMenu) {

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

    protected abstract void addToContextMenu(ContextMenu contextMenu);

    protected boolean isValidForFolders() {
        return false;
    }

    public static void createContextMenu(ContextMenu contextMenu, Activity activity, BookmarkHistoryItem bookmarkHistoryItem, UIManager uiManager) {
        if (bookmarkHistoryItem != null) {
            contextMenu.setHeaderTitle(bookmarkHistoryItem.getTitle());
            BitmapDrawable icon = ApplicationUtils.getApplicationButtonImage(activity, bookmarkHistoryItem.getFavicon());
            if (icon != null) {
                contextMenu.setHeaderIcon(icon);
            }
            for (BookmarksContextMenuOptions bookmarksContextMenuOptions : values()) {
                if (bookmarksContextMenuOptions.isValidForFolders() == bookmarkHistoryItem.isFolder()) {
                    bookmarksContextMenuOptions.addToContextMenu(contextMenu);
                }
            }
            List<AddonMenuItem> addonsContributions = Controller.getInstance().getAddonManager().getContributedBookmarkContextMenuItems(uiManager.getCurrentWebView());
            for (AddonMenuItem item : addonsContributions) {
                contextMenu.add(0, item.getAddon().getMenuId(), 0, item.getMenuItem());
            }
        }
    }
}
