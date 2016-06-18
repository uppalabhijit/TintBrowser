package org.tint.ui.webview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import org.tint.R;
import org.tint.ui.managers.UIManager;
import org.tint.utils.Callback;

/**
 * User: Abhijit
 * Date: 2016-06-13
 */
class JsPromptsManager implements IJsPromptsManager {
    private final UIManager mUIManager;

    JsPromptsManager(UIManager mUIManager) {
        this.mUIManager = mUIManager;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        showAlertDialog(R.string.JavaScriptConfirmDialog, message, new Callback() {
            @Override
            public void execute() {
                result.confirm();
            }
        }, new Callback() {
            @Override
            public void execute() {
                result.cancel();
            }
        });
        return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
        final LayoutInflater factory = LayoutInflater.from(mUIManager.getMainActivity());
        final View v = factory.inflate(R.layout.javascript_prompt_dialog, null);
        ((TextView) v.findViewById(R.id.JavaScriptPromptMessage)).setText(message);
        ((EditText) v.findViewById(R.id.JavaScriptPromptInput)).setText(defaultValue);

        new AlertDialog.Builder(mUIManager.getMainActivity())
                .setTitle(R.string.JavaScriptPromptDialog)
                .setView(v)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = ((EditText) v.findViewById(R.id.JavaScriptPromptInput)).getText()
                                        .toString();
                                result.confirm(value);
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                result.cancel();
                            }
                        })
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                result.cancel();
                            }
                        })
                .show();
        return true;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        showAlertDialog(R.string.JavaScriptAlertDialog, message, new Callback() {
            @Override
            public void execute() {
                result.confirm();
            }
        }, new Callback() {
            @Override
            public void execute() {
                result.cancel();
            }
        });
        return true;
    }

    private void showAlertDialog(int titleId, String message, final Callback okCallback, final Callback cancelCallback) {
        new AlertDialog.Builder(mUIManager.getMainActivity())
                .setTitle(titleId)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        okCallback.execute();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelCallback.execute();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        cancelCallback.execute();
                    }
                })
                .create()
                .show();
    }
}
