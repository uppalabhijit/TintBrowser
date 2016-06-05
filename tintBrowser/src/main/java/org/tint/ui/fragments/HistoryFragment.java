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
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.DateSorter;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.managers.UIManager;
import org.tint.ui.model.BookmarkHistoryItem;
import org.tint.ui.uihelpers.HistoryContextMenuOptions;
import org.tint.ui.uihelpers.visitors.HistoryContextMenuClickVisitor;

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public View mContainer = null;

    private UIManager mUIManager;

    private BaseHistoryFragmentUi baseHistoryFragmentUi;

    public int mSelectedGroup;
    public boolean[] mExpandedGroups = new boolean[DateSorter.DAY_COUNT];

    public HistoryContextMenuClickVisitor historyContextMenuClickVisitor;

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
            baseHistoryFragmentUi = BaseHistoryFragmentUi.create(stub, this);
            baseHistoryFragmentUi.initViews();
            baseHistoryFragmentUi.initListViews();
            baseHistoryFragmentUi.registerForContextMenu();
            baseHistoryFragmentUi.setListShown(false);

            getLoaderManager().initLoader(0, null, this);
        }

        return mContainer;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        BookmarkHistoryItem selectedItem = baseHistoryFragmentUi.getItemForContextMenu(menuInfo);
        HistoryContextMenuOptions.createMenu(menu, getActivity(), selectedItem, mUIManager);
    }

    @Override
    public boolean onContextItemSelected(MenuItem menuItem) {
        BookmarkHistoryItem selectedItem = null;
        baseHistoryFragmentUi.getContextMenuSelectedItem(menuItem);

        if (selectedItem != null) {
            historyContextMenuClickVisitor.setBookmarkHistoryItem(selectedItem);
            historyContextMenuClickVisitor.setMenuItem(menuItem);
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
        baseHistoryFragmentUi.setListShown(false);
        return BookmarksWrapper.getCursorLoaderForHistory(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        baseHistoryFragmentUi.changeCursor(data);
        if (data != null) {
            baseHistoryFragmentUi.onLoadFinished(data);
        }
        baseHistoryFragmentUi.setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        baseHistoryFragmentUi.changeCursor(null);
    }
}
