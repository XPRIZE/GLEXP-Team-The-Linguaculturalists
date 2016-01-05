package com.linguaculturalists.phoenicia.ui;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.CameraScene;

/**
 * Created by mhall on 1/1/16.
 */
public abstract class PhoeniciaHUD extends CameraScene {

    public PhoeniciaHUD(Camera camera) {
        super(camera);
    }
    public void open() {}
    public void show() {}
    public void hide() {}
    public void close() {}
}
