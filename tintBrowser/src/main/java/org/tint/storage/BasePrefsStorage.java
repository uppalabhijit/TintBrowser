package org.tint.storage;

import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

/**
 * User: Abhijit
 * Date: 2016-06-07
 */
abstract class BasePrefsStorage {
    protected final SharedPrefsStorage sharedPrefsStorage;

    public BasePrefsStorage() {
        this.sharedPrefsStorage = new SharedPrefsStorage();
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener mPreferenceChangeListener) {
        sharedPrefsStorage.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener mPreferenceChangeListener) {
        sharedPrefsStorage.unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }
}
