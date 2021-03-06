package org.tint.ui.uihelpers.visitors.bookmarks;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.widget.AdapterView.AdapterContextMenuInfo;

import org.tint.R;
import org.tint.ui.model.BookmarkHistoryItem;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.activities.EditBookmarkActivity;
import org.tint.ui.fragments.BookmarksFragment;
import org.tint.ui.managers.UIManager;
import org.tint.ui.uihelpers.visitors.history.HistoryContextMenuClickVisitor;
import org.tint.utils.Constants;

/**
 * User: Abhijit
 * Date: 2016-06-04
 */
public class BookmarksContextMenuClickVisitor extends HistoryContextMenuClickVisitor implements BookmarksContextMenuVisitor {
    private final WeakReference<Activity> weakReference;
    private final BookmarkHistoryItem bookmarkHistoryItem;
    private final BookmarksFragment bookmarksFragment;
    private final UIManager uiManager;
    private final MenuItem menuItem;
    private final AdapterContextMenuInfo adapterContextMenuInfo;
    private ProgressDialog progressDialog;

    public BookmarksContextMenuClickVisitor(WeakReference<Activity> weakReference, BookmarksFragment bookmarksFragment, UIManager
            uiManager, MenuItem menuItem, AdapterContextMenuInfo adapterContextMenuInfo) {
        super(weakReference, uiManager);
        this.weakReference = weakReference;
        this.bookmarksFragment = bookmarksFragment;
        this.bookmarkHistoryItem = BookmarksWrapper.getBookmarkById(weakReference.get().getContentResolver(), adapterContextMenuInfo.id);
        this.uiManager = uiManager;
        this.menuItem = menuItem;
        this.adapterContextMenuInfo = adapterContextMenuInfo;
    }

    @Override
    public boolean visitEditBookmark() {
        if (bookmarkHistoryItem != null) {
            Intent i = new Intent(weakReference.get(), EditBookmarkActivity.class);
            i.putExtra(Constants.EXTRA_ID, adapterContextMenuInfo.id);
            i.putExtra(Constants.EXTRA_FOLDER_ID, bookmarkHistoryItem.getFolderId());
            i.putExtra(Constants.EXTRA_LABEL, bookmarkHistoryItem.getTitle());
            i.putExtra(Constants.EXTRA_URL, bookmarkHistoryItem.getUrl());
            weakReference.get().startActivity(i);
        }
        return true;
    }

    @Override
    public boolean visitDeleteItem() {
        BookmarksWrapper.deleteBookmark(weakReference.get().getContentResolver(), adapterContextMenuInfo.id);
        return true;
    }

    @Override
    public boolean visitDeleteFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(weakReference.get());
        builder.setCancelable(true);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.DeleteFolder);
        builder.setMessage(R.string.ConfirmDeleteFolderMessage);
        builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doDeleteFolder(adapterContextMenuInfo.id);
            }
        });
        builder.setNegativeButton(R.string.No, null);
        builder.create().show();
        return true;
    }

    private void doDeleteFolder(long folderId) {
        progressDialog = ProgressDialog.show(
                weakReference.get(),
                weakReference.get().getString(R.string.DeleteFolderTitle),
                weakReference.get().getString(R.string.DeleteFolderMessage));
        new DeleteFolderTask(weakReference.get().getApplicationContext(), folderId, progressDialog, bookmarksFragment).execute((Object[]) null);
    }

    private static class DeleteFolderTask extends AsyncTask<Object, Object, Object> {
        private final Context context;
        private final long folderId;
        private final ProgressDialog progressDialog;
        private final BookmarksFragment bookmarksFragment;

        public DeleteFolderTask(Context context, long folderId, ProgressDialog progressDialog, BookmarksFragment bookmarksFragment) {
            this.context = context;
            this.folderId = folderId;
            this.progressDialog = progressDialog;
            this.bookmarksFragment = bookmarksFragment;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            BookmarksWrapper.deleteFolder(context.getContentResolver(), folderId);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressDialog.dismiss();
            bookmarksFragment.getLoaderManager().restartLoader(0, null, bookmarksFragment);
        }
    }
}