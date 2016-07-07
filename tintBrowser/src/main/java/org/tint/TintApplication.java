package org.tint;

import android.app.Application;

import org.tint.controllers.ContextRegistry;
import org.tint.domain.web.IAdBlocker;

/**
 * User: Abhijit
 * Date: 2016-07-04
 */
public class TintApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ContextRegistry.init(this);
        IAdBlocker.Factory.init();
    }
}
