package org.tint.ui.activities;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;

/**
 * Created by Abhijit on 2016-05-28.
 */
abstract class BaseActivity extends Activity {

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getLayoutId();
        if (layoutId != -1) {
            setContentView(layoutId);
        }
        int titleId = getTitleId();
        if (titleId != -1) {
            setTitle(titleId);
        }
        initActionBar(savedInstanceState);
        doOnCreate(savedInstanceState);
    }

    protected abstract void doOnCreate(Bundle savedInstanceState);

    protected abstract void initActionBar(Bundle savedInstanceState);

    protected abstract int getLayoutId();

    protected abstract int getTitleId();

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        doCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public final void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected abstract void doCreateOptionsMenu(final Menu menu);
}
