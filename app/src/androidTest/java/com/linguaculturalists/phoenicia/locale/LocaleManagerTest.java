package com.linguaculturalists.phoenicia.locale;

import android.test.AndroidTestCase;

import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 1/31/16.
 */
public class LocaleManagerTest extends AndroidTestCase {

    private LocaleManager localeManager;
    private Map<String, String> locales;

    LocaleManagerTest() {
        super();
        this.localeManager = new LocaleManager();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testLocaleScanning() {
        Map<String, String> locales = this.localeManager.scan(new File("locales/"));

        assertEquals(2, locales.size());
        assertTrue(locales.containsKey("locales/en_us_test/manifest.xml"));
        assertTrue(locales.containsValue("US English, Testing"));
    }

}
