package org.tint.ui.uihelpers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.managers.UIManager;
import org.tint.utils.Constants;

import java.lang.ref.WeakReference;

/**
 * Created by Abhijit on 2016-05-28.
 */
public class TintActivityResultHandler {
    private final WeakReference<TintBrowserActivity> tintBrowserActivityWeakReference;
    private final UIManager uiManager;

    public TintActivityResultHandler(WeakReference<TintBrowserActivity> tintBrowserActivityWeakReference, UIManager uiManager) {
        this.tintBrowserActivityWeakReference = tintBrowserActivityWeakReference;
        this.uiManager = uiManager;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        TintBrowserActivity tintBrowserActivity = tintBrowserActivityWeakReference.get();
        if (requestCode == Constants.ACTIVITY_BOOKMARKS) {
            if (resultCode == tintBrowserActivity.RESULT_OK) {
                handleOpeningBookmark(intent, tintBrowserActivity);
            }
        } else if (requestCode == Constants.ACTIVITY_OPEN_FILE_CHOOSER) {
            if (uiManager.getUploadMessage() == null) {
                return;
            }
            Uri result = intent == null || resultCode != tintBrowserActivity.RESULT_OK ? null : intent.getData();
            uiManager.getUploadMessage().onReceiveValue(result);
            uiManager.setUploadMessage(null);
        }
        uiManager.onActivityResult(requestCode, resultCode, intent);
    }

    private void handleOpeningBookmark(Intent intent, TintBrowserActivity tintBrowserActivity) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.getBoolean(Constants.EXTRA_NEW_TAB)) {
                    uiManager.addTab(false, PreferenceManager.getDefaultSharedPreferences(tintBrowserActivity).
                            getBoolean(Constants.PREFERENCE_INCOGNITO_BY_DEFAULT, false));
                }
                uiManager.loadUrl(bundle.getString(Constants.EXTRA_URL));
            }
        }
    }
}
