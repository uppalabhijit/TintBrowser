package org.tint.ui.uihelpers.visitors;

/**
 * User: Abhijit
 * Date: 2016-06-04
 */
public interface BookmarksContextMenuVisitor {
    boolean visitOpenInTab();

    boolean visitEditBookmark();

    boolean visitCopyUrl();

    boolean visitShareUrl();

    boolean visitDeleteBookmark();

    boolean visitDeleteFolder();

    boolean visitDefault();
}
