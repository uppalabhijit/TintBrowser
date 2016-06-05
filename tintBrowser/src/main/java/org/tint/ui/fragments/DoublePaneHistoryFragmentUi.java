package org.tint.ui.fragments;

import android.app.FragmentBreadCrumbs;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.tint.R;
import org.tint.ui.adapters.HistoryAdapter;
import org.tint.ui.model.BookmarkHistoryItem;

/**
 * User: Abhijit
 * Date: 2016-06-05
 */
public class DoublePaneHistoryFragmentUi extends BaseHistoryFragmentUi {
    private HistoryGroupWrapper groupAdapter;
    private HistoryChildWrapper childAdapter;
    public ListView groupList;
    public ListView childList;
    private FragmentBreadCrumbs childHeader;

    public DoublePaneHistoryFragmentUi(HistoryFragment historyFragment) {
        super(historyFragment);
    }

    @Override
    protected void initViews() {
        View mContainer = historyFragment.mContainer;
        childHeader = (FragmentBreadCrumbs) mContainer.findViewById(R.id.history_child_breadcrumbs);
        childHeader.setMaxVisible(1);
        childHeader.setActivity(historyFragment.getActivity());

        ListView mGroupList = (ListView) mContainer.findViewById(R.id.history_group_list);
        ListView mChildList = (ListView) mContainer.findViewById(R.id.history_child_list);

        mGroupList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        mGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectGroup(view, position);
            }
        });

        mChildList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openItem(historyFragment, childAdapter.getSelectedGroup(), position);
            }
        });

        mProgress = (ProgressBar) mContainer.findViewById(R.id.HistoryProgressBar);
        historyFragment.mContainer = mContainer;
    }

    @Override
    protected void attachAdapterToListViews(HistoryAdapter historyAdapter) {
        groupAdapter = new HistoryGroupWrapper(historyAdapter, this);
        groupList.setAdapter(groupAdapter);

        childAdapter = new HistoryChildWrapper(historyAdapter, this);
        childList.setAdapter(childAdapter);
    }

    @Override
    protected void registerForContextMenu() {
        historyFragment.registerForContextMenu(childList);
    }

    @Override
    protected BookmarkHistoryItem getItemForContextMenu(ContextMenu.ContextMenuInfo contextMenuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
        int group = childAdapter.getSelectedGroup();
        int child = info.position;

        return (BookmarkHistoryItem) mAdapter.getChild(group, child);
    }

    @Override
    protected BookmarkHistoryItem getContextMenuSelectedItem(MenuItem menuItem) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();

        int group = childAdapter.getSelectedGroup();
        int child = info.position;

        return (BookmarkHistoryItem) mAdapter.getChild(group, child);
    }

    private static abstract class HistoryWrapper extends BaseAdapter {
        protected HistoryAdapter mHistoryAdapter;

        protected HistoryWrapper(HistoryAdapter adapter) {
            mHistoryAdapter = adapter;
            mHistoryAdapter.registerDataSetObserver(mObserver);
        }

        private DataSetObserver mObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                notifyDataSetInvalidated();
            }
        };
    }

    private static class HistoryGroupWrapper extends HistoryWrapper {
        private final DoublePaneHistoryFragmentUi doublePaneHistoryFragmentUi;

        private HistoryGroupWrapper(HistoryAdapter adapter, DoublePaneHistoryFragmentUi doublePaneHistoryFragmentUi) {
            super(adapter);
            this.doublePaneHistoryFragmentUi = doublePaneHistoryFragmentUi;
        }

        @Override
        public int getCount() {
            return doublePaneHistoryFragmentUi.mAdapter.getGroupCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return doublePaneHistoryFragmentUi.mAdapter.getGroupView(position, false, convertView, parent);
        }
    }

    private static class HistoryChildWrapper extends HistoryWrapper {
        private final DoublePaneHistoryFragmentUi doublePaneHistoryFragmentUi;
        private int mSelectedGroup;

        public HistoryChildWrapper(HistoryAdapter adapter, DoublePaneHistoryFragmentUi doublePaneHistoryFragmentUi) {
            super(adapter);
            this.doublePaneHistoryFragmentUi = doublePaneHistoryFragmentUi;
        }

        @Override
        public int getCount() {
            return doublePaneHistoryFragmentUi.mAdapter.getChildrenCount(mSelectedGroup);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return doublePaneHistoryFragmentUi.mAdapter.getChildView(mSelectedGroup, position, false, convertView, parent);
        }

        public void setSelectedGroup(int groupPosition) {
            mSelectedGroup = groupPosition;
            notifyDataSetChanged();
        }

        public int getSelectedGroup() {
            return mSelectedGroup;
        }

    }

    @Override
    protected void showItemsList() {
        childList.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onLoadFinished(Cursor data) {
        // Select previously selected group.
        selectGroup(mAdapter.getGroupView(historyFragment.mSelectedGroup, false, null, null), historyFragment.mSelectedGroup);
    }

    private void selectGroup(View view, int position) {
        CharSequence title = ((TextView) view).getText();
        childHeader.setTitle(title, title);
        childAdapter.setSelectedGroup(position);
        groupList.setItemChecked(position, true);
        historyFragment.mSelectedGroup = position;
    }
}
