package org.tint.storage;

import java.util.HashSet;
import java.util.Set;

import org.tint.utils.Constants;

/**
 * User: Abhijit
 * Date: 2016-06-07
 */
public class CommonPrefsStorage extends BasePrefsStorage {

    public long getLastHistoryTruncationTime() {
        return sharedPrefsStorage.getLong(Constants.TECHNICAL_PREFERENCE_LAST_HISTORY_TRUNCATION, -1L);
    }

    public void setLastHistoryTruncationTime(long lastHistoryTruncationTime) {
        sharedPrefsStorage.setLong(Constants.TECHNICAL_PREFERENCE_LAST_HISTORY_TRUNCATION, lastHistoryTruncationTime);
    }

    public String getHistorySize() {
        return sharedPrefsStorage.getString(Constants.PREFERENCE_HISTORY_SIZE, "30");
    }
}