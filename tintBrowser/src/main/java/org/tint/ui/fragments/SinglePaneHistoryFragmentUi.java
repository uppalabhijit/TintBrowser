package org.tint.ui.fragments;

import android.database.Cursor;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;

import org.tint.R;
import org.tint.ui.adapters.HistoryAdapter;
import org.tint.ui.model.BookmarkHistoryItem;

/**
 * User: Abhijit
 * Date: 2016-06-05
 */
public class SinglePaneHistoryFragmentUi extends BaseHistoryFragmentUi {
    public ExpandableListView listView;

    public SinglePaneHistoryFragmentUi(HistoryFragment historyFragment) {
        super(historyFragment);
    }

    @Override
    protected void initViews() {
        View mContainer = historyFragment.mContainer;
        listView = (ExpandableListView) mContainer.findViewById(R.id.HistoryExpandableList);
        listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                historyFragment.mExpandedGroups[groupPosition] = true;
            }
        });
        listView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                historyFragment.mExpandedGroups[groupPosition] = false;
            }
        });
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                openItem(historyFragment, groupPosition, childPosition);
                return true;
            }
        });
        mProgress = (ProgressBar) mContainer.findViewById(R.id.HistoryProgressBar);
    }

    @Override
    protected void attachAdapterToListViews(HistoryAdapter historyAdapter) {
        listView.setAdapter(historyAdapter);
    }

    @Override
    protected void registerForContextMenu() {
        historyFragment.registerForContextMenu(listView);
    }

    @Override
    protected BookmarkHistoryItem getItemForContextMenu(ContextMenu.ContextMenuInfo contextMenuInfo) {
        BookmarkHistoryItem selectedItem = null;
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) contextMenuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
            selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);
        }
        return selectedItem;
    }

    @Override
    protected BookmarkHistoryItem getContextMenuSelectedItem(MenuItem menuItem) {
        BookmarkHistoryItem selectedItem = null;
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuItem.getMenuInfo();
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int child = ExpandableListView.getPackedPositionChild(info.packedPosition);

            selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);
        }
        return selectedItem;
    }

    @Override
    protected void showItemsList() {
        listView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onLoadFinished(Cursor data) {
        if (!historyFragment.historyContextMenuClickVisitor.isAfterDelete()) {
            if (mAdapter.getGroupCount() > 0) {
                for (int i = 0; i < historyFragment.mExpandedGroups.length; i++) {
                    if (historyFragment.mExpandedGroups[i]) {
                        listView.expandGroup(i, true);
                    }
                }
            }
        }
        historyFragment.historyContextMenuClickVisitor.resetAfterDelete();
    }
}
