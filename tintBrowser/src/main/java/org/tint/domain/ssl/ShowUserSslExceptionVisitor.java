package org.tint.domain.ssl;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.http.SslError;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.widget.CheckBox;
import android.widget.Toast;

import org.tint.R;
import org.tint.controllers.ContextRegistry;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Callback;

/**
 * Created by abhijituppal on 2016-08-16.
 */
public class ShowUserSslExceptionVisitor implements SslAuthorityStatus.SslExceptionVisitor {
    private final Context context;
    private final Resources resources;
    private final String authority;
    private final SslErrorHandler sslErrorhandler;
    private final SslError error;
    private boolean askUser = true;

    public ShowUserSslExceptionVisitor(String authority, SslErrorHandler sslErrorhandler, SslError error) {
        context = ContextRegistry.get();
        resources = context.getResources();
        this.authority = authority;
        this.sslErrorhandler = sslErrorhandler;
        this.error = error;
    }

    @Override
    public void visitAuthorityUnknown() {
        askUser = true;
    }

    @Override
    public void visitAuthorityAllowed() {
        askUser = false;
        sslErrorhandler.proceed();
        Toast.makeText(context, String.format(context.getResources().getString(R.string.SslExceptionAccessAllowedByUserToast), authority), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void visitAuthorityDisallowed() {
        askUser = false;
        sslErrorhandler.cancel();
        Toast.makeText(context, String.format(resources.getString(R.string.SslExceptionAccessDisallowedByUserToast), authority), Toast.LENGTH_SHORT).show();
    }

    public void askUserIfNeeded() {
        if (askUser) {
            int errorCode = SslExceptionsWrapper.sslErrorToInt(error);

            String message = createMessage(errorCode);

            showDialogBox(errorCode, message, authority);
        }
    }

    @NonNull
    private String createMessage(int errorCode) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(String.format(resources.getString(R.string.SslWarningsHeader), authority));
        stringBuilder.append("\n\n");

        stringBuilder.append(Html.fromHtml(SslExceptionsWrapper.sslErrorReasonToString(context, errorCode)));
        return stringBuilder.toString();
    }

    private void showDialogBox(final int errorCode, String message, final String finalAuthority) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(resources.getString(R.string.SslWarning));
        builder.setMessage(message);

        View checkBoxLayout = ApplicationUtils.inflateView(R.layout.checkbox_layout);
        final CheckBox rememberCheckBox = (CheckBox) checkBoxLayout.findViewById(R.id.RemenberChoiceCheckBox);

        builder.setView(checkBoxLayout);

        builder.setInverseBackgroundForced(true);
        DialogBtnClick dialogBtnClick = new DialogBtnClick(errorCode, rememberCheckBox);
        builder.setPositiveButton(resources.getString(R.string.Continue), dialogBtnClick);
        builder.setNegativeButton(resources.getString(R.string.Continue), dialogBtnClick);
        builder.show();
    }

    private class DialogBtnClick implements DialogInterface.OnClickListener {
        private final int errorCode;
        private final CheckBox rememberCheckBox;
        private boolean allow;
        private Callback callback;

        private DialogBtnClick(int errorCode, CheckBox rememberCheckBox) {
            this.errorCode = errorCode;
            this.rememberCheckBox = rememberCheckBox;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    allow = true;
                    callback = new Callback() {
                        @Override
                        public void execute() {
                            sslErrorhandler.proceed();
                        }
                    };
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    allow = false;
                    callback = new Callback() {
                        @Override
                        public void execute() {
                            sslErrorhandler.cancel();
                        }
                    };
                    break;
            }
            if (rememberCheckBox.isChecked()) {
                SslExceptionsWrapper.setSslException(context.getContentResolver(), authority, errorCode, allow);
            }
            dialogInterface.dismiss();
            callback.execute();

        }
    }
}
