package org.tint.domain;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import org.tint.R;
import org.tint.controllers.ContextRegistry;
import org.tint.controllers.Controller;
import org.tint.storage.CursorManager;
import org.tint.ui.model.DownloadItem;
import org.tint.ui.model.DownloadModelItem;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Function;

/**
 * User: Abhijit
 * Date: 2016-06-13
 */
public class DownloadManagerWrapper {
    public void startDownload(String url) {
        Context context = ContextRegistry.get();
        DownloadItem item = new DownloadItem(url);
        long id = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(item);
        item.setId(id);
        Controller.getInstance().getDownloadsList().add(item);
        Toast.makeText(context, String.format(ApplicationUtils.getStringFromResource(R.string.DownloadStart), item.getFileName()), Toast.LENGTH_SHORT).show();
    }

    public DownloadModelItem queryById(long id) {
        final DownloadManager downloadManager = (DownloadManager) ContextRegistry.get().getSystemService(Context.DOWNLOAD_SERVICE);
        Query query = new Query();
        query.setFilterById(id);
        Cursor cursor = downloadManager.query(query);

        CursorManager.SingleItemCursor<DownloadModelItem> integerSingleItemCursor = new CursorManager.SingleItemCursor<DownloadModelItem>(new Function<Cursor, DownloadModelItem>() {
            @Override
            public DownloadModelItem apply(Cursor cursor) {
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(statusIndex);

                int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String localUri = cursor.getString(localUriIndex);

                int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                int reason = cursor.getInt(reasonIndex);
                return new DownloadModelItem(status, reason, localUri);
            }
        });
        DownloadModelItem downloadModelItem = integerSingleItemCursor.query(cursor);
        return downloadModelItem;
    }

}
