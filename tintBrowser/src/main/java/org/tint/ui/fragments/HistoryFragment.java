/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.tint.ui.fragments;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentBreadCrumbs;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.DateSorter;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.ui.model.BookmarkHistoryItem;
import org.tint.ui.adapters.HistoryAdapter;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.managers.UIManager;
import org.tint.ui.uihelpers.HistoryContextMenuOptions;
import org.tint.ui.uihelpers.visitors.HistoryContextMenuClickVisitor;
import org.tint.utils.Constants;

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private View mContainer = null;

    private UIManager mUIManager;

    private ExpandableListView mListView;

    private ListView mGroupList;
    private ListView mChildList;

    private ProgressBar mProgress;

    private FragmentBreadCrumbs mChildHeader;

    private HistoryAdapter mAdapter;

    private HistoryGroupWrapper mGroupAdapter;
    private HistoryChildWrapper mChildAdapter;

    private boolean mTwoPaneMode;

    private boolean mIsListShown = true;

    private int mSelectedGroup;
    private boolean[] mExpandedGroups = new boolean[DateSorter.DAY_COUNT];

    private HistoryContextMenuClickVisitor historyContextMenuClickVisitor;
    private OnCheckedChangeListener mBookmarkStarChangeListener;

    public HistoryFragment() {
        mUIManager = Controller.getInstance().getUIManager();

        for (int i = 0; i < mExpandedGroups.length; i++) {
            mExpandedGroups[i] = false;
        }

        mExpandedGroups[0] = true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        historyContextMenuClickVisitor = new HistoryContextMenuClickVisitor(new WeakReference<Activity>(getActivity()), mUIManager);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mContainer == null) {
            mContainer = inflater.inflate(R.layout.history_fragment, container, false);

            View stub = mContainer.findViewById(R.id.history_group);

            if (stub == null) {
                inflateSinglePane();
            } else {
                inflateTwoPane();
            }

            mBookmarkStarChangeListener = new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    long id = (Long) buttonView.getTag();
                    BookmarksWrapper.toggleBookmark(getActivity().getContentResolver(), id, isChecked);
                    if (isChecked) {
                        Toast.makeText(getActivity(), R.string.BookmarkAdded, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.BookmarkRemoved, Toast.LENGTH_SHORT).show();
                    }
                }
            };

            mAdapter = new HistoryAdapter(getActivity(),
                    mBookmarkStarChangeListener,
                    getActivity().getResources().getInteger(R.integer.favicon_size));

            if (mTwoPaneMode) {
                mGroupAdapter = new HistoryGroupWrapper(mAdapter);
                mGroupList.setAdapter(mGroupAdapter);

                mChildAdapter = new HistoryChildWrapper(mAdapter);
                mChildList.setAdapter(mChildAdapter);

                registerForContextMenu(mChildList);

            } else {
                mListView.setAdapter(mAdapter);
                registerForContextMenu(mListView);
            }

            setListShown(false);

            getLoaderManager().initLoader(0, null, this);
        }

        return mContainer;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        BookmarkHistoryItem selectedItem = null;

        if (mTwoPaneMode) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

            int group = mChildAdapter.getSelectedGroup();
            int child = info.position;

            selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);
        } else {
            ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

            int type = ExpandableListView.getPackedPositionType(info.packedPosition);

            if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                int child = ExpandableListView.getPackedPositionChild(info.packedPosition);

                selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);
            }
        }
        HistoryContextMenuOptions.createMenu(menu, getActivity(), selectedItem, mUIManager);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        BookmarkHistoryItem selectedItem = null;
        if (mTwoPaneMode) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuItem.getMenuInfo();

            int group = mChildAdapter.getSelectedGroup();
            int child = info.position;

            selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);

        } else {
            ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuItem.getMenuInfo();

            int type = ExpandableListView.getPackedPositionType(info.packedPosition);

            if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
                int child = ExpandableListView.getPackedPositionChild(info.packedPosition);

                selectedItem = (BookmarkHistoryItem) mAdapter.getChild(group, child);
            }
        }
        historyContextMenuClickVisitor.setBookmarkHistoryItem(selectedItem);
        historyContextMenuClickVisitor.setMenuItem(menuItem);

        if (selectedItem != null) {
            boolean status = HistoryContextMenuOptions.getById(menuItem.getItemId()).accept(historyContextMenuClickVisitor);
            if (status) {
                return status;
            }
        }
        return super.onContextItemSelected(menuItem);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mSelectedGroup = 0;
        setListShown(false);

        return BookmarksWrapper.getCursorLoaderForHistory(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);

        if (data != null) {

            if (!mTwoPaneMode) {
                if (!historyContextMenuClickVisitor.isAfterDelete()) {
                    if (mAdapter.getGroupCount() > 0) {
                        for (int i = 0; i < mExpandedGroups.length; i++) {
                            if (mExpandedGroups[i]) {
                                mListView.expandGroup(i, true);
                            }
                        }
                    }
                }
                historyContextMenuClickVisitor.resetAfterDelete();
            } else {
                // Select previously selected group.
                selectGroup(mAdapter.getGroupView(mSelectedGroup, false, null, null), mSelectedGroup);
            }
        }

        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    private void setListShown(boolean shown) {
        if (mIsListShown == shown) {
            return;
        }

        mIsListShown = shown;

        if (shown) {
            mProgress.setVisibility(View.GONE);

            if (mTwoPaneMode) {
                mChildList.setVisibility(View.VISIBLE);
            } else {
                mListView.setVisibility(View.VISIBLE);
            }
        } else {
            mProgress.setVisibility(View.VISIBLE);

            if (mTwoPaneMode) {
                mChildList.setVisibility(View.VISIBLE);
            } else {
                mListView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void inflateSinglePane() {
        mTwoPaneMode = false;

        mListView = (ExpandableListView) mContainer.findViewById(R.id.HistoryExpandableList);

        mListView.setOnGroupExpandListener(new OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                mExpandedGroups[groupPosition] = true;
            }
        });

        mListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                mExpandedGroups[groupPosition] = false;
            }
        });

        mListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                openItem(groupPosition, childPosition);

                return true;
            }
        });

        mProgress = (ProgressBar) mContainer.findViewById(R.id.HistoryProgressBar);
    }

    private void inflateTwoPane() {
        mTwoPaneMode = true;

        mChildHeader = (FragmentBreadCrumbs) mContainer.findViewById(R.id.history_child_breadcrumbs);
        mChildHeader.setMaxVisible(1);
        mChildHeader.setActivity(getActivity());

        mGroupList = (ListView) mContainer.findViewById(R.id.history_group_list);
        mChildList = (ListView) mContainer.findViewById(R.id.history_child_list);

        mGroupList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        mGroupList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectGroup(view, position);
            }
        });

        mChildList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openItem(mChildAdapter.getSelectedGroup(), position);
            }
        });

        mProgress = (ProgressBar) mContainer.findViewById(R.id.HistoryProgressBar);
    }

    private void openItem(int groupPosition, int childPosition) {
        BookmarkHistoryItem item = (BookmarkHistoryItem) mAdapter.getChild(groupPosition, childPosition);
        Intent result = new Intent();
        result.putExtra(Constants.EXTRA_NEW_TAB, false);
        result.putExtra(Constants.EXTRA_URL, item.getUrl());

        getActivity().setResult(Activity.RESULT_OK, result);
        getActivity().finish();
    }

    private void selectGroup(View view, int position) {
        CharSequence title = ((TextView) view).getText();
        mChildHeader.setTitle(title, title);

        mChildAdapter.setSelectedGroup(position);
        mGroupList.setItemChecked(position, true);

        mSelectedGroup = position;
    }

    private abstract class HistoryWrapper extends BaseAdapter {

        protected HistoryAdapter mHistoryAdapter;

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

        public HistoryWrapper(HistoryAdapter adapter) {
            mHistoryAdapter = adapter;
            mHistoryAdapter.registerDataSetObserver(mObserver);
        }
    }

    private class HistoryGroupWrapper extends HistoryWrapper {

        public HistoryGroupWrapper(HistoryAdapter adapter) {
            super(adapter);
        }

        @Override
        public int getCount() {
            return mAdapter.getGroupCount();
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
            return mAdapter.getGroupView(position, false, convertView, parent);
        }
    }

    private class HistoryChildWrapper extends HistoryWrapper {

        private int mSelectedGroup;

        public HistoryChildWrapper(HistoryAdapter adapter) {
            super(adapter);
        }

        @Override
        public int getCount() {
            return mAdapter.getChildrenCount(mSelectedGroup);
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
            return mAdapter.getChildView(mSelectedGroup, position, false, convertView, parent);
        }

        public void setSelectedGroup(int groupPosition) {
            mSelectedGroup = groupPosition;
            notifyDataSetChanged();
        }

        public int getSelectedGroup() {
            return mSelectedGroup;
        }

    }

}
