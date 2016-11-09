package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.input.touch.TouchEvent;

/**
 * Base class for all game HUDs
 */
public abstract class PhoeniciaHUD extends CameraScene {

    protected PhoeniciaGame game;

    /**
     * Common setup for game HUDs
     *
     * @param game Instance of the PhoeniciaGame this HUD is running in
     */
    public PhoeniciaHUD(PhoeniciaGame game) {
        super(game.camera);
        this.game = game;
        this.setOnAreaTouchTraversalFrontToBack();
    }

    /**
     * Called only when this HUD is pushed onto the stack
     */
    public void open() {
    }

    /**
     * Called when the HUD is displayed.
     * Can happen either by being pushed into the HUDManager stack, or having the instance above
     * it in the stack popped off
     */
    public void show() {
    }

    /**
     * Called when the HUD is hidden.
     * Can happen either by having another instance pushed into the HUDManager stack, or being
     * itself popped off the stack
     */
    public void hide() {
    }

    /**
     * Called only when this HUD is popped off the stack
     */
    public void close() {
    }

    public abstract void finish();

//    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
//        if (this.hasOnSceneTouchListener()) {
//            return this.getOnSceneTouchListener().onSceneTouchEvent(this, pSceneTouchEvent);
//        } else {
//            return super.onSceneTouchEvent(pSceneTouchEvent);
//        }
//    }
}