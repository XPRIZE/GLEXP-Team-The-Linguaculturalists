package com.linguaculturalists.phoenicia;

import android.test.ActivityInstrumentationTestCase2;
import android.test.ActivityTestCase;
import android.test.ActivityUnitTestCase;

import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.ui.HUDManager;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.Filter;

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
        activity.syncDB();
        assertNotNull("GameActivity is Null", activity);
        game = new PhoeniciaGame(activity, activity.main_camera);
    }

    public void loadGame() {
        this.loadGame("locales/en_us_test/manifest.xml");
    }
    public void loadGame(final String locale_pack_manifest) {
        if (isLoaded) return;
        try {
            GameSession session;
            try {
                Filter byLocale = new Filter();
                byLocale.is("locale_pack", locale_pack_manifest);
                session = GameSession.objects(PhoeniciaContext.context).filter(byLocale).toList().get(0);
            } catch (IndexOutOfBoundsException e) {
                session = GameSession.start(locale_pack_manifest);
            }
            session.save(PhoeniciaContext.context);
            game.load(session);
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
