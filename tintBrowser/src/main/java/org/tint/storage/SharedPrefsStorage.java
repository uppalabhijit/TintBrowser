package org.tint.storage;

import java.util.Set;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import org.tint.controllers.Controller;

/**
 * User: Abhijit
 * Date: 2016-06-07
 */
final class SharedPrefsStorage {
    private final SharedPreferences sharedPreferences;

    public SharedPrefsStorage() {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Controller.getInstance().getContext());
    }

    public int getInt(String key, int defVal) {
        return sharedPreferences.getInt(key, defVal);
    }

    public void setInt(String key, int val) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, val);
        editor.commit();
    }

    public String getString(String key, String defVal) {
        return sharedPreferences.getString(key, defVal);
    }

    public void setString(String key, String val) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defVal) {
        return sharedPreferences.getBoolean(key, defVal);
    }

    public void setBoolean(String key, boolean val) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    public long getLong(String key, long defVal) {
        return sharedPreferences.getLong(key, defVal);
    }

    public void setLong(String key, long val) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, val);
        editor.commit();
    }

    public void setFloat(String key, float val) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, val);
        editor.commit();
    }

    public float getFloat(String key, float defVal) {
        return sharedPreferences.getFloat(key, defVal);
    }

    public void removeKey(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public Set<String> getStringSet(String key, Set<String> defaultValue) {
        return sharedPreferences.getStringSet(key, defaultValue);
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }
}
