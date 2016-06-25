package org.tint.storage;

import java.util.HashSet;
import java.util.Set;

import org.tint.utils.Constants;

/**
 * User: Abhijit
 * Date: 2016-06-08
 */
public class TintBrowserActivityStorage extends BasePrefsStorage {
    public boolean isFirstRun() {
        return sharedPrefsStorage.getBoolean(Constants.TECHNICAL_PREFERENCE_FIRST_RUN, true);
    }

    public void setFirstRunDone() {
        sharedPrefsStorage.setBoolean(Constants.TECHNICAL_PREFERENCE_FIRST_RUN, false);
    }

    public void setAppVersionCode(int applicationVersionCode) {
        sharedPrefsStorage.setInt(Constants.TECHNICAL_PREFERENCE_LAST_RUN_VERSION_CODE, applicationVersionCode);
    }

    public int getAppVersionCode() {
        return sharedPrefsStorage.getInt(Constants.TECHNICAL_PREFERENCE_LAST_RUN_VERSION_CODE, -1);
    }

    public Set<String> getSavedTabs() {
        return sharedPrefsStorage.getStringSet(Constants.TECHNICAL_PREFERENCE_SAVED_TABS, new HashSet<String>());
    }

    public void deleteSavedTabs() {
        sharedPrefsStorage.removeKey(Constants.TECHNICAL_PREFERENCE_SAVED_TABS);
    }

    public String getRestoreTabsPreference() {
        return sharedPrefsStorage.getString(Constants.PREFERENCE_RESTORE_TABS, "ASK");
    }

    public void setRestoreTabsPreference(String restoreTabsPreference) {
        sharedPrefsStorage.setString(Constants.PREFERENCE_RESTORE_TABS, restoreTabsPreference);
    }

    public void saveTabs(Set<String> tabs) {
        sharedPrefsStorage.getStringSet(Constants.TECHNICAL_PREFERENCE_SAVED_TABS, tabs);
    }

    public String getUserHomePage() {
        return sharedPrefsStorage.getString(Constants.PREFERENCE_HOME_PAGE, Constants.URL_ABOUT_START);
    }

    public void setUserHomePage(String userHomePage) {
        sharedPrefsStorage.setString(Constants.PREFERENCE_HOME_PAGE, userHomePage);
    }

    public boolean getIncognitoByDefaultStatus() {
        return sharedPrefsStorage.getBoolean(Constants.PREFERENCE_INCOGNITO_BY_DEFAULT, false);
    }

    public boolean isFullScreen() {
        return sharedPrefsStorage.getBoolean(Constants.PREFERENCE_FULL_SCREEN, false);
    }

    public void setFullScreen(boolean fullScreen) {
        sharedPrefsStorage.setBoolean(Constants.PREFERENCE_FULL_SCREEN, fullScreen);
    }
}
