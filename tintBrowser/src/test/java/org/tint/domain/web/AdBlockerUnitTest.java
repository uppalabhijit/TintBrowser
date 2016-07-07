package org.tint.domain.web;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class AdBlockerUnitTest {
    private static final List<String> domains = Arrays.asList("traffichunt.com", "trafficjunky.net", "travis.bosscasinos.com", "tsms-ad.tsms.com");

    @Test
    public void testIsAdHost() throws Exception {
        AdBlocker adBlocker = new AdBlocker() {
            @Override
            public boolean isEmpty(String host) {
                return host != null && host.length() > 0;
            }
        };
        adBlocker.addDomains(domains);
        Assert.assertTrue(adBlocker.isAdHost("abc.dev.traffichunt.com"));
    }
}