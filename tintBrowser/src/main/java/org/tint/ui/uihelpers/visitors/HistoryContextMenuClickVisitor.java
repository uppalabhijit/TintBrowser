package org.tint.ui.uihelpers.visitors;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.model.BookmarkHistoryItem;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.managers.UIManager;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

/**
 * User: Abhijit
 * Date: 2016-06-04
 */
public class HistoryContextMenuClickVisitor implements HistoryContextMenuVisitor {
    private final WeakReference<Activity> activityWeakReference;
    private final UIManager uiManager;

    private BookmarkHistoryItem bookmarkHistoryItem;
    private MenuItem menuItem;

    private boolean afterDelete = false;

    public HistoryContextMenuClickVisitor(WeakReference<Activity> activityWeakReference, UIManager uiManager) {
        this.activityWeakReference = activityWeakReference;
        this.uiManager = uiManager;
    }

    @Override
    public boolean visitOpenInTab() {
        Intent result = new Intent();
        result.putExtra(Constants.EXTRA_NEW_TAB, true);
        result.putExtra(Constants.EXTRA_URL, bookmarkHistoryItem.getUrl());

        activityWeakReference.get().setResult(Activity.RESULT_OK, result);
        activityWeakReference.get().finish();

        return true;
    }

    @Override
    public boolean visitCopyUrl() {
        if (bookmarkHistoryItem != null) {
            ApplicationUtils.copyTextToClipboard(activityWeakReference.get(), bookmarkHistoryItem.getUrl(), activityWeakReference.get().getResources().getString(R.string.UrlCopyToastMessage));
        }

        return true;
    }

    @Override
    public boolean visitShareUrl() {
        if (bookmarkHistoryItem != null) {
            ApplicationUtils.sharePage(activityWeakReference.get(), null, bookmarkHistoryItem.getUrl());
        }

        return true;
    }

    @Override
    public boolean visitDeleteItem() {
        BookmarksWrapper.deleteHistoryRecord(activityWeakReference.get().getContentResolver(), bookmarkHistoryItem.getId());
        afterDelete = true;
        return true;
    }

    @Override
    public boolean visitDefault() {
        if (Controller.getInstance().getAddonManager().onContributedHistoryContextMenuItemSelected(
                activityWeakReference.get(),
                menuItem.getItemId(),
                bookmarkHistoryItem.getTitle(),
                bookmarkHistoryItem.getUrl(),
                uiManager.getCurrentWebView())) {
            return true;
        }
        return false;
    }

    public boolean isAfterDelete() {
        return afterDelete;
    }

    public void resetAfterDelete() {
        afterDelete = false;
    }

    public void setBookmarkHistoryItem(BookmarkHistoryItem bookmarkHistoryItem) {
        this.bookmarkHistoryItem = bookmarkHistoryItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }
}
