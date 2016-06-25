package org.tint.storage;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import org.tint.utils.Callback;
import org.tint.utils.Predicate;

/**
 * User: Abhijit
 * Date: 2016-06-07
 */
abstract class BaseSharedPreferencesChangeListener implements OnSharedPreferenceChangeListener {
    private final Predicate<String> predicate;
    private final Callback callback;

    protected BaseSharedPreferencesChangeListener(Predicate<String> predicate, Callback callback) {
        this.predicate = predicate;
        this.callback = callback;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (predicate.isSatisfiedBy(key)) {
            callback.execute();
        }
    }
}
