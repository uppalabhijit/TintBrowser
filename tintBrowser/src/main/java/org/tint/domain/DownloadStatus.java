package org.tint.domain;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import org.tint.R;
import org.tint.controllers.Controller;
import org.tint.model.DownloadItem;
import org.tint.utils.NotificationUtils;

/**
 * Created by Abhijit on 2016-05-28.
 */
public enum DownloadStatus {
    SUCCESSFUL(DownloadManager.STATUS_SUCCESSFUL) {
        @Override
        public void execute(Context context, Cursor cursor, DownloadItem downloadItem) {
            int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            String localUri = cursor.getString(localUriIndex);
            Toast.makeText(context, String.format(context.getString(R.string.DownloadComplete), localUri), Toast.LENGTH_SHORT).show();
            Controller.getInstance().getDownloadsList().remove(downloadItem);
            NotificationUtils.showDownloadCompleteNotification(context, context.getString(R.string.DownloadComplete), downloadItem.getFileName(), context.getString(R.string.DownloadComplete));
        }
    }, FAILED(DownloadManager.STATUS_FAILED) {
        @Override
        public void execute(Context context, Cursor cursor, DownloadItem downloadItem) {
            int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int reason = cursor.getInt(reasonIndex);
            String message;
            switch (reason) {
                case DownloadManager.ERROR_FILE_ERROR:
                case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                    message = context.getString(R.string.DownloadErrorDisk);
                    break;
                case DownloadManager.ERROR_HTTP_DATA_ERROR:
                case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                    message = context.getString(R.string.DownloadErrorHttp);
                    break;
                case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                    message = context.getString(R.string.DownloadErrorRedirection);
                    break;
                default:
                    message = context.getString(R.string.DownloadErrorUnknown);
                    break;
            }

            Toast.makeText(context, String.format(context.getString(R.string.DownloadFailedWithErrorMessage), message), Toast.LENGTH_SHORT).show();
            Controller.getInstance().getDownloadsList().remove(downloadItem);
        }
    };

    private final int status;

    DownloadStatus(int status) {
        this.status = status;
    }

    public static DownloadStatus getByStatus(int status) {
        for (DownloadStatus downloadStatus : values()) {
            if (downloadStatus.status == status) {
                return downloadStatus;
            }
        }
        return FAILED;
    }

    public abstract void execute(Context context, Cursor cursor, DownloadItem downloadItem);
}
