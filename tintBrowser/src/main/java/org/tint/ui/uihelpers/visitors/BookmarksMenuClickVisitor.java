package org.tint.ui.uihelpers.visitors;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.providers.BookmarksWrapper;
import org.tint.tasks.HistoryBookmarksExportTask;
import org.tint.tasks.HistoryBookmarksImportTask;
import org.tint.ui.activities.BookmarksActivity;
import org.tint.ui.activities.EditBookmarkActivity;
import org.tint.ui.preferences.IHistoryBookmaksExportListener;
import org.tint.ui.preferences.IHistoryBookmaksImportListener;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;
import org.tint.utils.IOUtils;

/**
 * Created by Abhijit on 2016-05-28.
 */
public class BookmarksMenuClickVisitor implements BookmarksMenuVisitor, IHistoryBookmaksExportListener, IHistoryBookmaksImportListener {
    private final WeakReference<BookmarksActivity> bookmarksActivityWeakReference;
    private final MenuItem menuItem;

    private ProgressDialog mProgress;
    private HistoryBookmarksImportTask mImportTask;
    private HistoryBookmarksExportTask mExportTask;

    private static final AtomicReference<AsyncTask<String, Integer, String>> mImportSyncThread =
            new AtomicReference<AsyncTask<String, Integer, String>>();

    private static final AtomicReference<AsyncTask<Cursor, Integer, String>> mExportSyncThread =
            new AtomicReference<AsyncTask<Cursor, Integer, String>>();

    public BookmarksMenuClickVisitor(WeakReference<BookmarksActivity> bookmarksActivityWeakReference, MenuItem menuItem) {
        this.bookmarksActivityWeakReference = bookmarksActivityWeakReference;
        this.menuItem = menuItem;
    }

    @Override
    public boolean visitHome() {
        BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
        bookmarksActivity.setResult(Activity.RESULT_CANCELED);
        bookmarksActivity.finish();
        return true;
    }

    @Override
    public boolean visitAddBookmarks() {
        BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
        Intent i = new Intent(bookmarksActivity, EditBookmarkActivity.class);
        i.putExtra(Constants.EXTRA_ID, -1);
        bookmarksActivity.startActivity(i);
        return true;
    }

    @Override
    public boolean visitSortBookmarks() {
        changeSortMode();
        return true;
    }

    @Override
    public boolean visitImportBookmarks() {
        importHistoryBookmarks();
        return true;
    }

    @Override
    public boolean visitExportBookmarks() {
        exportHistoryBookmarks();
        return true;
    }

    @Override
    public boolean visitClearBookmarksAndHistory() {
        clearHistoryBookmarks();
        return true;
    }

    @Override
    public boolean visitDefault() {
        if (Controller.getInstance().getAddonManager().onContributedHistoryBookmarksMenuItemSelected(
                bookmarksActivityWeakReference.get(),
                menuItem.getItemId(),
                Controller.getInstance().getUIManager().getCurrentWebView())) {
            return true;
        } else {
            BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
            return bookmarksActivity.getParent().onContextItemSelected(menuItem);
        }
    }

    private void changeSortMode() {
        final BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
        int currentSort = PreferenceManager.getDefaultSharedPreferences(bookmarksActivity).getInt(Constants.PREFERENCE_BOOKMARKS_SORT_MODE, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(bookmarksActivity);

        builder.setInverseBackgroundForced(true);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        Resources resources = getResources(bookmarksActivity);
        builder.setTitle(resources.getString(R.string.SortBookmarks));

        builder.setSingleChoiceItems(
                new String[]{
                        resources.getString(R.string.MostUsedSortMode),
                        resources.getString(R.string.AlphaSortMode),
                        resources.getString(R.string.RecentSortMode)
                },
                currentSort,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(bookmarksActivity).edit();
                        editor.putInt(Constants.PREFERENCE_BOOKMARKS_SORT_MODE, which);
                        editor.commit();
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private Resources getResources(BookmarksActivity bookmarksActivity) {
        return bookmarksActivity.getResources();
    }

    private void importHistoryBookmarks() {
        final BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
        List<String> exportedFiles = IOUtils.getExportedBookmarksFileList();
        final String[] choices = exportedFiles.toArray(new String[exportedFiles.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(bookmarksActivity);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(bookmarksActivity.getResources().getString(R.string.HistoryBookmarksImportSourceTitle));
        builder.setSingleChoiceItems(choices, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                mImportTask = new HistoryBookmarksImportTask(bookmarksActivity, BookmarksMenuClickVisitor.this);

                mProgress = ProgressDialog.show(bookmarksActivity,
                        getString(bookmarksActivity, R.string.HistoryBookmarksImportTitle),
                        getString(bookmarksActivity, R.string.HistoryBookmarksImportInitialMessage),
                        true,
                        false);

                mProgress.show();

                boolean retVal = mImportSyncThread.compareAndSet(null, mImportTask);
                if (retVal) {
                    mImportTask.execute(choices[which]);
                }

            }
        });

        builder.setCancelable(true);
        builder.setNegativeButton(R.string.Cancel, null);
        builder.show();
    }

    private String getString(BookmarksActivity bookmarksActivity, int stringId) {
        return bookmarksActivity.getString(stringId);
    }

    private void exportHistoryBookmarks() {
        BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
        mExportTask = new HistoryBookmarksExportTask(bookmarksActivity, BookmarksMenuClickVisitor.this);

        mProgress = ProgressDialog.show(bookmarksActivity,
                getString(bookmarksActivity, R.string.HistoryBookmarksExportTitle),
                getString(bookmarksActivity, R.string.HistoryBookmarksExportInitialMessage),
                true,
                false);

        mProgress.show();

        boolean retVal = mExportSyncThread.compareAndSet(null, mExportTask);
        if (retVal) {
            mExportTask.execute(BookmarksWrapper.getAllHistoryBookmarks(bookmarksActivity.getContentResolver()));
        }
    }

    private void clearHistoryBookmarks() {
        final BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
        AlertDialog.Builder builder = new AlertDialog.Builder(bookmarksActivity);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(getString(bookmarksActivity, R.string.HistoryBookmarksClearTitle));
        builder.setSingleChoiceItems(getResources(bookmarksActivity).getStringArray(R.array.ClearHistoryBookmarksChoice), 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                switch (which) {
                    case 0:
                        BookmarksWrapper.clearHistoryAndOrBookmarks(bookmarksActivity.getContentResolver(), true, false);
                        break;

                    case 1:
                        BookmarksWrapper.clearHistoryAndOrBookmarks(bookmarksActivity.getContentResolver(), false, true);
                        break;

                    case 2:
                        BookmarksWrapper.clearHistoryAndOrBookmarks(bookmarksActivity.getContentResolver(), true, true);
                        break;

                    default:
                        break;
                }
            }

        });

        builder.setCancelable(true);
        builder.setNegativeButton(R.string.Cancel, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onExportProgress(int step, int progress, int total) {
        BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
        switch (step) {
            case 0:
                mProgress.setMessage(getString(bookmarksActivity, R.string.HistoryBookmarksExportCheckCardMessage));
                break;
            case 1:
                mProgress.setMessage(String.format(getString(bookmarksActivity, R.string.HistoryBookmarksExportProgressMessage), progress, total));
                break;
            default:
                break;
        }
    }

    @Override
    public void onExportDone(String message) {
        mExportSyncThread.compareAndSet(mExportTask, null);
        mProgress.dismiss();

        if (message != null) {
            BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
            ApplicationUtils.showErrorDialog(bookmarksActivity,
                    getString(bookmarksActivity, R.string.HistoryBookmarksExportErrorTitle),
                    String.format(getString(bookmarksActivity, R.string.HistoryBookmarksExportErrorMessage), message));
        }
    }

    @Override
    public void onImportProgress(int step, int progress, int total) {
        BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
        switch (step) {
            case 0:
                mProgress.setMessage(getString(bookmarksActivity, R.string.HistoryBookmarksImportReadingFile));
                break;
            case 1:
                mProgress.setMessage(getString(bookmarksActivity, R.string.HistoryBookmarksImportParsingFile));
                break;
            case 2:
                mProgress.setMessage(String.format(getString(bookmarksActivity, R.string.HistoryBookmarksImportProgressMessage), progress, total));
                break;
            case 3:
                mProgress.setMessage(String.format(getString(bookmarksActivity, R.string.HistoryBookmarksImportFoldersProgressMessage), progress, total));
                break;
            case 4:
                mProgress.setMessage(getString(bookmarksActivity, R.string.HistoryBookmarksImportFoldersLinkMessage));
                break;
            case 5:
                mProgress.setMessage(String.format(getString(bookmarksActivity, R.string.HistoryBookmarksImportBookmarksProgressMessage), progress, total));
                break;
            case 6:
                mProgress.setMessage(String.format(getString(bookmarksActivity, R.string.HistoryBookmarksImportHistoryProgressMessage), progress, total));
                break;
            case 7:
                mProgress.setMessage(getString(bookmarksActivity, R.string.HistoryBookmarksImportInsertMessage));
                break;
            default:
                break;
        }
    }

    @Override
    public void onImportDone(String message) {
        mImportSyncThread.compareAndSet(mImportTask, null);
        mProgress.dismiss();

        if (message != null) {
            BookmarksActivity bookmarksActivity = bookmarksActivityWeakReference.get();
            ApplicationUtils.showErrorDialog(bookmarksActivity,
                    getString(bookmarksActivity, R.string.HistoryBookmarksImportErrorTitle),
                    String.format(getString(bookmarksActivity, R.string.HistoryBookmarksImportErrorMessage), message));
        }
    }
}
