package org.tint.ui.uihelpers.visitors.browser;

/**
 * Created by Abhijit on 2016-05-28.
 */
public interface BrowserActivityMenuVisitor {
    boolean visitAddTab();

    boolean visitCloseTab();

    boolean visitAddBookmark();

    boolean visitMenuBookmarks();

    boolean visitIncognitoTab();

    boolean visitFullScreen();

    boolean visitShare();

    boolean visitSearch();

    boolean visitSettings();

    boolean visitDefault();
}
