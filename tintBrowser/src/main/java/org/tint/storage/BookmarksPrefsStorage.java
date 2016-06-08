package org.tint.storage;

import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import org.tint.utils.Callback;
import org.tint.utils.Constants;
import org.tint.utils.Predicate;

/**
 * User: Abhijit
 * Date: 2016-06-07
 */
public class BookmarksPrefsStorage {
    private final SharedPrefsStorage sharedPrefsStorage;

    public BookmarksPrefsStorage() {
        this.sharedPrefsStorage = new SharedPrefsStorage();
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener mPreferenceChangeListener) {
        sharedPrefsStorage.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener mPreferenceChangeListener) {
        sharedPrefsStorage.unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    public void updateBookmarkSortMode(int mode) {
        sharedPrefsStorage.setInt(Constants.PREFERENCE_BOOKMARKS_SORT_MODE, mode);
    }

    public int getBookmarkSortMode() {
        return sharedPrefsStorage.getInt(Constants.PREFERENCE_BOOKMARKS_SORT_MODE, 0);
    }

    public static class BookmarksPreferenceChangeListener extends BaseSharedPreferencesChangeListener {

        public BookmarksPreferenceChangeListener(final Callback callback) {
            super(new Predicate<String>() {
                @Override
                public boolean isSatisfiedBy(String key) {
                    return Constants.PREFERENCE_BOOKMARKS_SORT_MODE.equals(key);
                }
            }, callback);
        }
    }
}