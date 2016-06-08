package org.tint.ui.uihelpers.visitors.bookmarks;

import org.tint.ui.uihelpers.visitors.history.HistoryContextMenuVisitor;

/**
 * User: Abhijit
 * Date: 2016-06-04
 */
public interface BookmarksContextMenuVisitor extends HistoryContextMenuVisitor {

    boolean visitEditBookmark();

    boolean visitDeleteFolder();

}
