package org.tint.domain.web;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;

/**
 * User: Abhijit
 * Date: 2016-07-04
 */
public class AdBlocker implements IAdBlocker {
    private Set<String> adDomains = new HashSet<String>();

    @Override
    public void addDomains(List<String> domains) {
        adDomains.addAll(domains);
    }

    @Override
    public boolean isAd(String url) {
        Uri uri = Uri.parse(url);
        return isAdHost(uri.getHost());
    }

    protected boolean isAdHost(String host) {
        if (isEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        int indexPlusOne = index + 1;
        while (index != -1) {
            if (adDomains.contains(host)) {
                return true;
            }
            if (indexPlusOne >= host.length()) {
                return false;
            }
            host = stripSubDomain(host, indexPlusOne);
            index = host.indexOf(".");
        }
        return false;
    }

    public boolean isEmpty(String host) {
        return TextUtils.isEmpty(host);
    }

    private String stripSubDomain(String host, int index) {
        return host.substring(index);
    }

    @Override
    public WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
}
