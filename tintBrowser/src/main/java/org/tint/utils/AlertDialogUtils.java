package org.tint.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.tint.utils.ApplicationUtils;
import org.tint.utils.Callback;

/**
 * User: Abhijit
 * Date: 2016-06-18
 */
public class AlertDialogUtils {
    public static void showOkcancelDialog(Activity activity, String title, String message, int posButtonRes, final Callback okCallback,
                                          final Callback cancelCallback) {
        showOkcancelDialog(activity, title, message, ApplicationUtils.getStringFromResource(posButtonRes), okCallback, cancelCallback);
    }

    public static void showOkcancelDialog(Activity activity, String title, String message, String posButton, final Callback okCallback, final Callback
            cancelCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title).setMessage(message).setPositiveButton(posButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                okCallback.execute();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cancelCallback.execute();
            }
        }).show();
    }

}
