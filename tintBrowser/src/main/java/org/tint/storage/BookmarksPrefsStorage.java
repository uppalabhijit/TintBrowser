package org.tint.storage;

import org.tint.providers.BookmarksWrapper;
import org.tint.utils.Callback;
import org.tint.utils.Constants;
import org.tint.utils.Predicate;

/**
 * User: Abhijit
 * Date: 2016-06-07
 */
public class BookmarksPrefsStorage extends BasePrefsStorage {

    public BookmarksPrefsStorage() {
        super();
    }

    public void updateBookmarkSortMode(int mode) {
        sharedPrefsStorage.setInt(Constants.PREFERENCE_BOOKMARKS_SORT_MODE, mode);
    }

    public int getBookmarkSortMode() {
        return sharedPrefsStorage.getInt(Constants.PREFERENCE_BOOKMARKS_SORT_MODE, BookmarksWrapper.BookmarkSortMode.MOST_USED.ordinal());
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
