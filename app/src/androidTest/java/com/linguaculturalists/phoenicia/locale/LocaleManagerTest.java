package com.linguaculturalists.phoenicia.locale;

import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;

import com.linguaculturalists.phoenicia.PhoeniciaGameTest;
import com.linguaculturalists.phoenicia.TestActivity;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.util.debug.Debug;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by mhall on 1/31/16.
 */
public class LocaleManagerTest extends ActivityInstrumentationTestCase2<TestActivity> {

    private LocaleManager localeManager;
    private Map<String, String> locales;

    public LocaleManagerTest() {
        super(TestActivity.class);
        this.localeManager = new LocaleManager();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        PhoeniciaContext.assetManager = this.getActivity().getAssets();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testLocaleScanning() {

        try {
            Map<String, String> locales = this.localeManager.scan("locales");

            assertEquals(2, locales.size());
            assertTrue(locales.containsKey("locales/en_us_test/manifest.xml"));
            assertTrue(locales.containsValue("US English, Testing"));

            assertTrue(locales.containsKey("locales/en_us_rural/manifest.xml"));
            assertTrue(locales.containsValue("US English, Rural Setting"));
        } catch (IOException e) {
            assertNull(e);
        }
    }

}
