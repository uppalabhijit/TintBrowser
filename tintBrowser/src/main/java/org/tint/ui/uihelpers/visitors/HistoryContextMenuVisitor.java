package org.tint.ui.uihelpers.visitors;

/**
 * User: Abhijit
 * Date: 2016-06-04
 */
public interface HistoryContextMenuVisitor {
    boolean visitOpenInTab();

    boolean visitCopyUrl();

    boolean visitShareUrl();

    boolean visitDeleteItem();

    boolean visitDefault();
}
