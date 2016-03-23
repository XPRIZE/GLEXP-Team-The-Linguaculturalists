package com.linguaculturalists.phoenicia;

import android.view.MotionEvent;

import com.linguaculturalists.phoenicia.models.DefaultTile;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.LetterBuilder;
import com.linguaculturalists.phoenicia.models.LetterTile;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.linguaculturalists.phoenicia.util.SystemUiHider;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.controller.MultiTouchController;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.ui.IGameInterface;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Android Activity for PhoeniciaGame.
 */
public class GameActivity extends BaseGameActivity {

    public static final int CAMERA_WIDTH = 1280;
    public static final int CAMERA_HEIGHT = 800;

    public ZoomCamera main_camera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
    private PhoeniciaGame game;
    private SplashScene splash;

    @Override
    public EngineOptions onCreateEngineOptions() {
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
                new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), main_camera);
        engineOptions.getAudioOptions().setNeedsSound(true);
        return engineOptions;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback onCreateResourcesCallback) throws IOException {
        splash = new SplashScene(getTextureManager(), getAssets(), getVertexBufferObjectManager(), main_camera);
        onCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback onCreateSceneCallback) throws IOException {

        onCreateSceneCallback.onCreateSceneFinished(splash);
    }

    @Override
    public void onBackPressed() {
        game.onBackPressed();
    }

    @Override
    public void onPopulateScene(Scene scene, OnPopulateSceneCallback onPopulateSceneCallback) throws IOException {
        // Prime the static context utility
        PhoeniciaContext.activity = this;
        PhoeniciaContext.context = this.getApplicationContext();
        PhoeniciaContext.textureManager = this.getTextureManager();
        PhoeniciaContext.assetManager = this.getAssets();
        PhoeniciaContext.vboManager = this.getVertexBufferObjectManager();
        PhoeniciaContext.soundManager = this.getSoundManager();
        PhoeniciaContext.fontManager = this.getFontManager();

        this.syncDB();
        game = new PhoeniciaGame(this, main_camera);

        mEngine.registerUpdateHandler(new TimerHandler(1f, new ITimerCallback()
        {
            public void onTimePassed(final TimerHandler pTimerHandler)
            {
                mEngine.unregisterUpdateHandler(pTimerHandler);
                try {
                    mEngine.setTouchController(new MultiTouchController());
                    // Load phoeniciaGame session
                    GameSession session;
                    try {
                        session = GameSession.objects(PhoeniciaContext.context).all().toList().get(0);
                        if (session.locale_pack.get().equals("en_us_rural")) {
                            session.locale_pack.set("locales/en_us_rural/manifest.xml");
                            session.save(PhoeniciaContext.context);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        session = GameSession.start("locales/en_us_rural/manifest.xml", "1");
                    }
                    session.save(PhoeniciaContext.context);
                    game.load(session);
                    splash.detachSelf();
                    mEngine.setScene(game.scene);
                    mEngine.registerUpdateHandler(game);
                    game.start();
                } catch (final IOException e) {
                    Debug.e("Failed to load game!", e);
                }
            }
        }));


        onPopulateSceneCallback.onPopulateSceneFinished();
    }

    /**
     * Tell AndOrm about the Models that will be used to read and write to the database.
     */
    public void syncDB() {
        List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
        models.add(GameSession.class);
        models.add(InventoryItem.class);
        models.add(DefaultTile.class);
        models.add(LetterTile.class);
        models.add(WordTile.class);
        models.add(LetterBuilder.class);
        models.add(WordBuilder.class);

        DatabaseAdapter.setDatabaseName("game_db");
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(PhoeniciaContext.context);
        adapter.setModels(models);
    }

}
