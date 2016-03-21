package com.linguaculturalists.phoenicia;

import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityTestCase;
import android.test.ActivityUnitTestCase;

import com.linguaculturalists.phoenicia.ui.HUDManager;

import java.io.IOException;

/**
 * Created by mhall on 12/2/15.
 */
public class PhoeniciaGameTest extends ActivityInstrumentationTestCase2<GameActivity> {

    public GameActivity activity;
    public PhoeniciaGame game;
    public boolean isLoaded = false;
    public boolean isStarted = false;

    public PhoeniciaGameTest() {
        super(GameActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        assertNotNull("GameActivity is Null", activity);
        game = new PhoeniciaGame(activity, activity.main_camera);
    }

    public void loadGame() {
        this.loadGame("locales/en_us_test/manifest.xml");
    }
    public void loadGame(final String locale_pack_manifest) {
        if (isLoaded) return;
        try {
            game.load(locale_pack_manifest);
        } catch (IOException e) {
            assertNull("Game loading failed: "+e.getMessage(), e);
        }
        isLoaded = true;
    }

    public void startGame() {
        if (isStarted) return;
        if (!isLoaded) this.loadGame();
        game.start();
        isStarted = true;
    }
}
