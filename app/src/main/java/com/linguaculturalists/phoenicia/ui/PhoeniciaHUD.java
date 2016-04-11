package com.linguaculturalists.phoenicia.ui;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.CameraScene;

/**
 * Base class for all game HUDs
 */
public abstract class PhoeniciaHUD extends CameraScene {

    public PhoeniciaHUD(Camera camera) {
        super(camera);
        this.setOnAreaTouchTraversalFrontToBack();
    }

    /**
     * Called only when this HUD is pushed onto the stack
     */
    public void open() {}

    /**
     * Called when the HUD is displayed.
     * Can happen either by being pushed into the HUDManager stack, or having the instance above
     * it in the stack popped off
     */
    public void show() {}

    /**
     * Called when the HUD is hidden.
     * Can happen either by having another instance pushed into the HUDManager stack, or being
     * itself popped off the stack
     */
    public void hide() {}

    /**
     * Called only when this HUD is popped off the stack
     */
    public void close() {}
}
