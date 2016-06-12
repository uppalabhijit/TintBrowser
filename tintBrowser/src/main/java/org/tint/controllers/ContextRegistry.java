package org.tint.controllers;

import android.content.Context;

/**
 * User: Abhijit
 * Date: 2016-06-09
 */
public class ContextRegistry {
    private static final ContextRegistry contextRegistry = new ContextRegistry();
    private static Context context;

    private ContextRegistry() {
    }

    public static Context get() {
        return context;
    }

    public static void init(Context context) {
        contextRegistry.context = context.getApplicationContext();
    }
}
