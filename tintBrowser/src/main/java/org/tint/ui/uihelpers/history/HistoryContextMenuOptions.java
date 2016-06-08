package org.tint.ui.uihelpers.history;

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
import org.tint.ui.uihelpers.visitors.history.HistoryContextMenuVisitor;
import org.tint.utils.ApplicationUtils;

/**
 * User: Abhijit
 * Date: 2016-06-04
 */
public enum HistoryContextMenuOptions {
    OPEN_IN_TAB(Menu.FIRST) {
        @Override
        public boolean accept(HistoryContextMenuVisitor historyContextMenuVisitor) {
            return historyContextMenuVisitor.visitOpenInTab();
        }
    }, COPY_URL(Menu.FIRST + 1) {
        @Override
        public boolean accept(HistoryContextMenuVisitor historyContextMenuVisitor) {
            return historyContextMenuVisitor.visitCopyUrl();
        }
    }, SHARE_URL(Menu.FIRST + 2) {
        @Override
        public boolean accept(HistoryContextMenuVisitor historyContextMenuVisitor) {
            return historyContextMenuVisitor.visitShareUrl();
        }
    }, DELETE_HISTORY_ITEM(Menu.FIRST + 3) {
        @Override
        public boolean accept(HistoryContextMenuVisitor historyContextMenuVisitor) {
            return historyContextMenuVisitor.visitDeleteItem();
        }
    }, DEFAULT(-1) {
        @Override
        public boolean accept(HistoryContextMenuVisitor historyContextMenuVisitor) {
            return historyContextMenuVisitor.visitDefault();
        }
    };

    private final int itemId;

    HistoryContextMenuOptions(int itemId) {
        this.itemId = itemId;
    }

    public static void createMenu(ContextMenu contextMenu, Activity activity, BookmarkHistoryItem bookmarkHistoryItem, UIManager uiManager) {
        if (bookmarkHistoryItem != null) {
            BitmapDrawable icon = ApplicationUtils.getApplicationButtonImage(activity, bookmarkHistoryItem.getFavicon());
            if (icon != null) {
                contextMenu.setHeaderIcon(icon);
            }
            contextMenu.setHeaderTitle(bookmarkHistoryItem.getTitle());

            contextMenu.add(0, OPEN_IN_TAB.itemId, 0, R.string.OpenInTab);
            contextMenu.add(0, COPY_URL.itemId, 0, R.string.CopyUrl);
            contextMenu.add(0, SHARE_URL.itemId, 0, R.string.ContextMenuShareUrl);
            contextMenu.add(0, DELETE_HISTORY_ITEM.itemId, 0, R.string.DeleteHistoryItem);

            List<AddonMenuItem> addonsContributions = Controller.getInstance().getAddonManager().getContributedHistoryContextMenuItems(uiManager.getCurrentWebView());
            for (AddonMenuItem item : addonsContributions) {
                contextMenu.add(0, item.getAddon().getMenuId(), 0, item.getMenuItem());
            }
        }
    }

    public static HistoryContextMenuOptions getById(int itemId) {
        for (HistoryContextMenuOptions historyContextMenuOptions : values()) {
            if (historyContextMenuOptions.itemId == itemId) {
                return historyContextMenuOptions;
            }
        }
        return DEFAULT;
    }

    public abstract boolean accept(HistoryContextMenuVisitor historyContextMenuVisitor);
}
