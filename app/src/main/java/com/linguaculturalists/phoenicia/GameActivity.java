package com.linguaculturalists.phoenicia;

import com.linguaculturalists.phoenicia.models.DefaultTile;
import com.linguaculturalists.phoenicia.models.GameSession;
import com.linguaculturalists.phoenicia.models.InventoryItem;
import com.linguaculturalists.phoenicia.models.LetterBuilder;
import com.linguaculturalists.phoenicia.models.LetterTile;
import com.linguaculturalists.phoenicia.models.MarketRequest;
import com.linguaculturalists.phoenicia.models.RequestItem;
import com.linguaculturalists.phoenicia.models.WordBuilder;
import com.linguaculturalists.phoenicia.models.WordTileBuilder;
import com.linguaculturalists.phoenicia.models.WordTile;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.controller.MultiTouchController;
import org.andengine.ui.activity.BaseGameActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Android Activity for PhoeniciaGame.
 */
public class GameActivity extends BaseGameActivity {

    public static final int CAMERA_WIDTH = 1280;
    public static final int CAMERA_HEIGHT = 750;

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

        mEngine.registerUpdateHandler(new TimerHandler(0.5f, new ITimerCallback()
        {
            public void onTimePassed(final TimerHandler pTimerHandler)
            {
                mEngine.unregisterUpdateHandler(pTimerHandler);
                mEngine.setTouchController(new MultiTouchController());
                // Load phoeniciaGame session
                splash.detachSelf();
                mEngine.setScene(new SessionSelectionScene(game, splash));
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
        models.add(MarketRequest.class);
        models.add(RequestItem.class);
        models.add(InventoryItem.class);
        models.add(DefaultTile.class);
        models.add(LetterTile.class);
        models.add(WordTile.class);
        models.add(LetterBuilder.class);
        models.add(WordTileBuilder.class);
        models.add(WordBuilder.class);

        DatabaseAdapter.setDatabaseName("game_db");
        DatabaseAdapter adapter = DatabaseAdapter.getInstance(PhoeniciaContext.context);
        adapter.setModels(models);
    }

}
