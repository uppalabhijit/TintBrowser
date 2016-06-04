package org.tint.ui.uihelpers;

/**
 * Created by Abhijit on 2016-05-28.
 */
public interface BookmarksMenuVisitor {
    boolean visitHome();

    boolean visitAddBookmarks();

    boolean visitSortBookmarks();

    boolean visitImportBookmarks();

    boolean visitExportBookmarks();

    boolean visitClearBookmarksAndHistory();

    boolean visitDefault();

}
