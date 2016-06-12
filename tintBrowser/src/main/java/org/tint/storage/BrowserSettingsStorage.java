package org.tint.storage;

import org.tint.utils.Constants;

/**
 * User: Abhijit
 * Date: 2016-06-09
 */
public class BrowserSettingsStorage extends BasePrefsStorage {
    public boolean isHomePageUrlUpdateNeeded() {
        return sharedPrefsStorage.getBoolean(Constants.TECHNICAL_PREFERENCE_HOMEPAGE_URL_UPDATE_NEEDED, false);
    }

    public void setHomePageUrlUpdateNeeded(boolean urlUpdateNeeded) {
        sharedPrefsStorage.setBoolean(Constants.TECHNICAL_PREFERENCE_HOMEPAGE_URL_UPDATE_NEEDED, urlUpdateNeeded);
    }
}
