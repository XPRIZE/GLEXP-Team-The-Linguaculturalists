package com.linguaculturalists.phoenicia;

import android.view.MotionEvent;

import com.linguaculturalists.phoenicia.util.SystemUiHider;

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

import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class GameActivity extends BaseGameActivity {

    public static final int CAMERA_WIDTH = 800;
    public static final int CAMERA_HEIGHT = 480;

    private ZoomCamera main_camera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
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
    public void onPopulateScene(Scene scene, OnPopulateSceneCallback onPopulateSceneCallback) throws IOException {

        game = new PhoeniciaGame(this, main_camera);
        mEngine.registerUpdateHandler(new TimerHandler(1f, new ITimerCallback()
        {
            public void onTimePassed(final TimerHandler pTimerHandler)
            {
                mEngine.unregisterUpdateHandler(pTimerHandler);
                try {
                    mEngine.setTouchController(new MultiTouchController());
                    game.load();
                    splash.detachSelf();
                    mEngine.setScene(game.scene);
                    mEngine.registerUpdateHandler(game);
                    game.start(main_camera);
                } catch (final IOException e) {
                    System.err.println("Failed to load game!");
                    e.printStackTrace(System.err);
                }
            }
        }));


        onPopulateSceneCallback.onPopulateSceneFinished();
    }
}
