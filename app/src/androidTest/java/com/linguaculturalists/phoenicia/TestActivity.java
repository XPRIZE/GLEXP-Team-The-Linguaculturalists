package com.linguaculturalists.phoenicia;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.color.Color;

import java.io.IOException;

/**
 * Created by mhall on 3/1/16.
 */
public class TestActivity extends SimpleBaseGameActivity {

    public static final int CAMERA_WIDTH = 640;
    public static final int CAMERA_HEIGHT = 480;
    public ZoomCamera main_camera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

    public Scene scene;

    @Override
    public EngineOptions onCreateEngineOptions() {
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
                new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), main_camera);
        engineOptions.getAudioOptions().setNeedsSound(true);
        return engineOptions;
    }

    @Override
    protected void onCreateResources() throws IOException {

    }

    @Override
    protected Scene onCreateScene() {
        this.scene = new Scene();
        this.scene.setBackground(new Background(new Color(0, 0, 0)));
        return this.scene;
    }
}
