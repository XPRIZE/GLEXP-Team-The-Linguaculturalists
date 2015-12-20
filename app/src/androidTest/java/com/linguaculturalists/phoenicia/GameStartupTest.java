package com.linguaculturalists.phoenicia;

/**
 * Created by mhall on 12/2/15.
 */
public class GameStartupTest extends PhoeniciaGameTest {

    public void setUp() throws Exception {
        super.setUp();
        this.startGame();
    }

    public void testActivityExists() {
        assertNotNull("GameActivity is Null", activity);
    }

    public void testGameStart() {
        assertEquals("HUDManager not set on game start", game.hudManager, activity.main_camera.getHUD());
    }
}
