package org.tint.domain;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import org.tint.R;
import org.tint.controllers.ContextRegistry;
import org.tint.controllers.Controller;
import org.tint.storage.CursorManager;
import org.tint.ui.model.DownloadRequest;
import org.tint.ui.model.DownloadResponse;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Function;

/**
 * User: Abhijit
 * Date: 2016-06-13
 */
public class TintDownloadManager {

    public void startDownload(String url) {
        Context context = ContextRegistry.get();
        DownloadRequest item = new DownloadRequest(url);
        long id = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(item);
        item.setId(id);
        Controller.getInstance().getDownloadsList().add(item);
        Toast.makeText(context, String.format(ApplicationUtils.getStringFromResource(R.string.DownloadStart), item.getFileName()), Toast.LENGTH_SHORT).show();
    }

    public DownloadResponse queryById(long id) {
        final DownloadManager downloadManager = (DownloadManager) ContextRegistry.get().getSystemService(Context.DOWNLOAD_SERVICE);
        Query query = new Query();
        query.setFilterById(id);
        Cursor cursor = downloadManager.query(query);

        CursorManager.SingleItemCursor<DownloadResponse> integerSingleItemCursor = new CursorManager.SingleItemCursor<DownloadResponse>(new Function<Cursor, DownloadResponse>() {
            @Override
            public DownloadResponse apply(Cursor cursor) {
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(statusIndex);

                int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String localUri = cursor.getString(localUriIndex);

                int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(reasonIndex);
                return new DownloadResponse(status, reason, localUri);
            }
        });
        DownloadResponse downloadResponse = integerSingleItemCursor.query(cursor);
        return downloadResponse;
    }

    public void showDownloads() {
        Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ContextRegistry.get().startActivity(intent);
    }
}
