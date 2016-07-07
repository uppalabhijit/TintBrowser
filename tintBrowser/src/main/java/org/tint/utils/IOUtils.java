/*
 * Tint Browser for Android
 * 
 * Copyright (C) 2012 - to infinity and beyond J. Devauchelle and contributors.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.tint.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import org.tint.R;

public class IOUtils {

    /**
     * Get the list of xml files in the bookmark export folder.
     *
     * @return The list of xml files in the bookmark export folder.
     */
    public static List<String> getExportedBookmarksFileList() {
        List<String> result = new ArrayList<String>();

        File folder = Environment.getExternalStorageDirectory();

        if (folder != null) {

            FileFilter filter = new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if ((pathname.isFile()) &&
                            (pathname.getPath().toLowerCase(Locale.US).endsWith(".xml") ||
                                    pathname.getPath().toLowerCase(Locale.US).endsWith(".json"))) {
                        return true;
                    }
                    return false;
                }
            };

            File[] files = folder.listFiles(filter);

            for (File file : files) {
                result.add(file.getName());
            }
        }

        Collections.sort(result, new Comparator<String>() {

            @Override
            public int compare(String arg0, String arg1) {
                return arg1.compareTo(arg0);
            }
        });

        return result;
    }

    public static String checkCardState(Context context) {
        // Check to see if we have an SDCard
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            // Check to see if the SDCard is busy, same as the music app
            if (status.equals(Environment.MEDIA_SHARED)) {
                return context.getString(R.string.SDCardErrorSDUnavailable);
            } else {
                return context.getString(R.string.SDCardErrorNoSDMsg);
            }
        }

        return null;
    }

    public static void closeQuietly(Cursor cursor) {
        try {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static List<String> readLinesFromAssets(Context context, String fileName) throws IOException {
        InputStream inputStream = null;
        List<String> result = new ArrayList<String>();
        try {
            inputStream = context.getAssets().open(fileName);
            byte[] buffer = new byte[8192];
            StringBuffer stringBuffer = new StringBuffer();
            while (inputStream.read(buffer) != -1) {
                stringBuffer.append(new String(buffer));
            }
            String[] lines = stringBuffer.toString().split("\\r\\n");
            for (String str : lines) {
                result.add(str);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return result;
    }
}