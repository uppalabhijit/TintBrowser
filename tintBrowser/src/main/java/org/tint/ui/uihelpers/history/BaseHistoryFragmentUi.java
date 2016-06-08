package org.tint.ui.uihelpers.history;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.tint.R;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.adapters.HistoryAdapter;
import org.tint.ui.fragments.HistoryFragment;
import org.tint.ui.model.BookmarkHistoryItem;
import org.tint.utils.Constants;

/**
 * User: Abhijit
 * Date: 2016-06-05
 */
public abstract class BaseHistoryFragmentUi {
    protected ProgressBar mProgress;
    protected final HistoryFragment historyFragment;
    protected boolean mIsListShown = true;
    protected HistoryAdapter mAdapter;

    protected BaseHistoryFragmentUi(HistoryFragment historyFragment) {
        this.historyFragment = historyFragment;
    }

    public static BaseHistoryFragmentUi create(View stub, HistoryFragment historyFragment) {
        if (stub == null) {
            return new SinglePaneHistoryFragmentUi(historyFragment);
        } else {
            return new DoublePaneHistoryFragmentUi(historyFragment);
        }
    }

    public abstract void initViews();

    public final void initListViews() {
        CompoundButton.OnCheckedChangeListener mBookmarkStarChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                long id = (Long) buttonView.getTag();
                BookmarksWrapper.toggleBookmark(historyFragment.getActivity().getContentResolver(), id, isChecked);
                if (isChecked) {
                    Toast.makeText(historyFragment.getActivity(), R.string.BookmarkAdded, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(historyFragment.getActivity(), R.string.BookmarkRemoved, Toast.LENGTH_SHORT).show();
                }
            }
        };
        mAdapter = new HistoryAdapter(historyFragment.getActivity(),
                mBookmarkStarChangeListener,
                historyFragment.getActivity().getResources().getInteger(R.integer.favicon_size));
        attachAdapterToListViews(mAdapter);
    }

    protected final void openItem(HistoryFragment historyFragment, int groupPosition, int childPosition) {
        BookmarkHistoryItem item = (BookmarkHistoryItem) mAdapter.getChild(groupPosition, childPosition);
        Intent result = new Intent();
        result.putExtra(Constants.EXTRA_NEW_TAB, false);
        result.putExtra(Constants.EXTRA_URL, item.getUrl());

        historyFragment.getActivity().setResult(Activity.RESULT_OK, result);
        historyFragment.getActivity().finish();
    }

    protected abstract void attachAdapterToListViews(HistoryAdapter historyAdapter);

    public abstract void registerForContextMenu();

    public abstract BookmarkHistoryItem getItemForContextMenu(ContextMenu.ContextMenuInfo contextMenuInfo);

    public abstract BookmarkHistoryItem getContextMenuSelectedItem(MenuItem menuItem);

    public void setListShown(boolean shown) {
        if (mIsListShown == shown) {
            return;
        }
        mIsListShown = shown;
        if (shown) {
            mProgress.setVisibility(View.GONE);
        } else {
            mProgress.setVisibility(View.VISIBLE);
        }
        showItemsList();
    }

    protected abstract void showItemsList();

    public abstract void onLoadFinished(Cursor data);

    public final void changeCursor(Cursor data) {
        mAdapter.changeCursor(data);
    }
}
