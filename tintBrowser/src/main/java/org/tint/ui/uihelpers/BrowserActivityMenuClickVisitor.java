package org.tint.ui.uihelpers;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import org.tint.controllers.Controller;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.managers.UIManager;
import org.tint.ui.preferences.PreferencesActivity;
import org.tint.utils.Constants;

import java.lang.ref.WeakReference;

/**
 * Created by Abhijit on 2016-05-28.
 */
public class BrowserActivityMenuClickVisitor implements BrowserActivityMenuVisitor {
    private final WeakReference<TintBrowserActivity> tintBrowserActivityWeakReference;
    private final UIManager uiManager;
    private final MenuItem menuItem;


    public BrowserActivityMenuClickVisitor(WeakReference<TintBrowserActivity> tintBrowserActivityWeakReference, UIManager uiManager, MenuItem menuItem) {
        this.tintBrowserActivityWeakReference = tintBrowserActivityWeakReference;
        this.uiManager = uiManager;
        this.menuItem = menuItem;
    }

    @Override
    public boolean visitAddTab() {
        uiManager.addTab(true, PreferenceManager.getDefaultSharedPreferences(tintBrowserActivityWeakReference.get()).
                getBoolean(Constants.PREFERENCE_INCOGNITO_BY_DEFAULT, false));
        return true;
    }

    @Override
    public boolean visitCloseTab() {
        uiManager.closeCurrentTab();
        return true;
    }

    @Override
    public boolean visitAddBookmark() {
        uiManager.addBookmarkFromCurrentPage();
        return true;
    }

    @Override
    public boolean visitMenuBookmarks() {
        uiManager.openBookmarksActivityForResult();
        return true;
    }

    @Override
    public boolean visitIncognitoTab() {
        uiManager.togglePrivateBrowsing();
        return true;
    }

    @Override
    public boolean visitFullScreen() {
        uiManager.toggleFullScreen();
        return true;
    }

    @Override
    public boolean visitShare() {
        uiManager.shareCurrentPage();
        return true;
    }

    @Override
    public boolean visitSearch() {
        uiManager.startSearch();
        return true;
    }

    @Override
    public boolean visitSettings() {
        Intent intent = new Intent(tintBrowserActivityWeakReference.get(), PreferencesActivity.class);
        tintBrowserActivityWeakReference.get().startActivity(intent);
        return true;
    }

    @Override
    public boolean visitDefault() {
        if (Controller.getInstance().getAddonManager().onContributedMainMenuItemSelected(
                tintBrowserActivityWeakReference.get(),
                menuItem.getItemId(),
                uiManager.getCurrentWebView())) {
            return true;
        } else {
            return tintBrowserActivityWeakReference.get().getParent().onOptionsItemSelected(menuItem);
        }
    }
}
