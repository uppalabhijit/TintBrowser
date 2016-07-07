package org.tint.domain.web;

import java.io.IOException;
import java.util.List;

import android.os.AsyncTask;
import android.webkit.WebResourceResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tint.controllers.ContextRegistry;
import org.tint.utils.IOUtils;

/**
 * User: Abhijit
 * Date: 2016-07-04
 */
public interface IAdBlocker {
    boolean isAd(String url);

    WebResourceResponse createEmptyResource();

    void addDomains(List<String> domains);

    class Factory {
        private static IAdBlocker iAdBlocker;

        public static void init() {
            if (iAdBlocker == null) {
                iAdBlocker = new AdBlocker();
            }
            loadDomainsFromAssets();
        }

        private static void loadDomainsFromAssets() {
            new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... params) {
                    try {
                        iAdBlocker.addDomains(IOUtils.readLinesFromAssets(ContextRegistry.get(), "ad-hosts.txt"));
                    } catch (IOException e) {
                        getLogger().error("[AdBlocker][loadDomainsFromAssets] could not read domains from assets file", e);
                    }
                    return null;
                }
            }.execute((Object[]) null);
        }

        private static Logger getLogger() {
            return LoggerFactory.getLogger("IAdBlocker.Factory");
        }

        public static IAdBlocker get() {
            return iAdBlocker;
        }
    }
}
