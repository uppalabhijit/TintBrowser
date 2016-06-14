package org.tint.ui.uihelpers;

import java.util.Set;

import android.view.View;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.storage.TintBrowserActivityStorage;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.ui.dialogs.YesNoRememberDialog;
import org.tint.utils.Callback;

/**
 * User: Abhijit
 * Date: 2016-06-13
 */
public enum TabRestoreMode {
    ASK {
        @Override
        public void execute(final TintBrowserActivity tintBrowserActivity, final TintBrowserActivityStorage
                tintBrowserActivityStorage, final Set<String> tabs) {
            final YesNoRememberDialog dialog = new YesNoRememberDialog(tintBrowserActivity);
            dialog.setTitle(R.string.RestoreTabsDialogTitle);
            dialog.setMessage(R.string.RestoreTabsDialogMessage);
            dialog.setPositiveButtonListener(new DialogButtonClickListener(dialog, tintBrowserActivityStorage, "ALWAYS", new Callback() {
                @Override
                public void execute() {
                    restoreTabs(tabs);
                }
            }));
            dialog.setNegativeButtonListener(new DialogButtonClickListener(dialog, tintBrowserActivityStorage, "NEVER", null));
            dialog.show();
        }
    }, ALWAYS {
        @Override
        public void execute(TintBrowserActivity tintBrowserActivity, TintBrowserActivityStorage tintBrowserActivityStorage, Set<String> tabs) {
            restoreTabs(tabs);
        }
    };

    public abstract void execute(TintBrowserActivity tintBrowserActivity, TintBrowserActivityStorage tintBrowserActivityStorage, Set<String> tabs);

    public static TabRestoreMode getFromString(String name) {
        for (TabRestoreMode tabRestoreMode : values()) {
            if (tabRestoreMode.name().equals(name)) {
                return tabRestoreMode;
            }
        }
        return ASK;
    }

    protected void restoreTabs(Set<String> tabs) {
        boolean first = true;
        for (String url : tabs) {
            if (first) {
                Controller.getInstance().getUIManager().loadUrl(url);
                first = false;
            } else {
                Controller.getInstance().getUIManager().addTab(url, !first, false);
            }
        }
    }

    private static class DialogButtonClickListener implements View.OnClickListener {
        private final YesNoRememberDialog dialog;
        private final TintBrowserActivityStorage tintBrowserActivityStorage;
        private final String restoreTabPreference;
        private final Callback callback;

        private DialogButtonClickListener(YesNoRememberDialog dialog, TintBrowserActivityStorage tintBrowserActivityStorage,
                                          String restoreTabPreference, Callback callback) {
            this.dialog = dialog;
            this.tintBrowserActivityStorage = tintBrowserActivityStorage;
            this.restoreTabPreference = restoreTabPreference;
            this.callback = callback;
        }

        @Override
        public void onClick(View view) {
            dialog.dismiss();
            if (dialog.isRememberChecked()) {
                tintBrowserActivityStorage.setRestoreTabsPreference(restoreTabPreference);
            }
            if (callback != null) {
                callback.execute();
            }
        }
    }
}
