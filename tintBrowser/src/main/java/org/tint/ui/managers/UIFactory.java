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

package org.tint.ui.managers;

import android.content.Context;
import android.preference.PreferenceManager;

import org.tint.R;
import org.tint.ui.activities.TintBrowserActivity;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

public class UIFactory {

    public enum UIType {
        TABLET("TABLET") {
            @Override
            protected int getMainLayout() {
                return R.layout.tablet_main_activity;
            }

            @Override
            protected int getMainMenuLayout() {
                return R.menu.main_activity_menu_tablet;
            }

            @Override
            protected UIManager createUIManager(TintBrowserActivity activity) {
                return new TabletUIManager(activity);
            }
        },
        PHONE("PHONE") {
            @Override
            protected int getMainLayout() {
                return R.layout.phone_main_activity;
            }

            @Override
            protected UIManager createUIManager(TintBrowserActivity activity) {
                return new PhoneUIManager(activity);
            }
        },
        LEGACY_PHONE("LEGACY_PHONE") {
            @Override
            protected int getMainLayout() {
                return R.layout.legacy_phone_main_activity;
            }

            @Override
            protected UIManager createUIManager(TintBrowserActivity activity) {
                return new LegacyPhoneUIManager(activity);
            }
        };
        private final String name;

        private UIType(String name) {
            this.name = name;
        }

        protected abstract int getMainLayout();

        protected abstract UIManager createUIManager(TintBrowserActivity activity);

        protected int getMainMenuLayout() {
            return R.menu.main_activity_menu;
        }

        private static UIType getFromName(String name) {
            for (UIType uiType : values()) {
                if (uiType.name.equals(name)) {
                    return uiType;
                }
            }
            return PHONE;
        }
    }

    private static boolean isInitialized = false;

    private static UIType sUIType;

    public static UIType getUIType(Context context) {
        checkInit(context);
        return sUIType;
    }

    public static boolean isPhone(Context context) {
        return !isTablet(context);
    }

    public static boolean isTablet(Context context) {
        checkInit(context);
        return sUIType == UIType.TABLET;
    }

    public static int getMainLayout(Context context) {
        checkInit(context);
        return sUIType.getMainLayout();
    }

    public static int getMainMenuLayout(Context context) {
        checkInit(context);
        return sUIType.getMainMenuLayout();
    }

    public static UIManager createUIManager(TintBrowserActivity activity) {
        checkInit(activity);
        return sUIType.createUIManager(activity);
    }

    private static void init(Context context) {
        try {
            String uiTypePref = PreferenceManager.getDefaultSharedPreferences(context).getString(Constants.PREFERENCE_UI_TYPE, "AUTO");
            if ("AUTO".equals(uiTypePref)) {
                if (ApplicationUtils.isATablet(context)) {
                    sUIType = UIType.TABLET;
                } else {
                    sUIType = UIType.PHONE;
                }
                return;
            }
            sUIType = UIType.getFromName(uiTypePref);
        } finally {
            isInitialized = true;
        }
    }

    private static void checkInit(Context context) {
        if (!isInitialized) {
            init(context);
        }
    }
}
