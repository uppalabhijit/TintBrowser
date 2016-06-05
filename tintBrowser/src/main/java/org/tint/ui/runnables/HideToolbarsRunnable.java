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

package org.tint.ui.runnables;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.tint.ui.managers.LegacyPhoneUIManager;

public class HideToolbarsRunnable implements Runnable {

    private LegacyPhoneUIManager uiManager;
    private int duration;
    private boolean disabled;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if ((!disabled) && (uiManager != null)) {
                uiManager.hideToolbars();
            }
        }
    };

    public HideToolbarsRunnable(LegacyPhoneUIManager uiManager, int duration) {
        this.uiManager = uiManager;
        this.duration = duration;
        disabled = false;
    }

    public void disable() {
        disabled = true;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(duration);
            mHandler.sendEmptyMessage(0);
        } catch (InterruptedException e) {
            Log.d("HideToolbarsRunnable", e.getMessage());
            mHandler.sendEmptyMessage(0);
        }
    }
}
