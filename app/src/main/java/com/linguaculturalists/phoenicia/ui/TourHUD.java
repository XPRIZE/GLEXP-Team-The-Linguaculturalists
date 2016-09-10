package com.linguaculturalists.phoenicia.ui;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.components.MapBlockSprite;
import com.linguaculturalists.phoenicia.locale.tour.Stop;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;

/**
 * Created by mhall on 9/8/16.
 */
public class TourHUD extends PhoeniciaHUD {
    private PhoeniciaGame game;
    private Stop stop;
    private Sprite guideSprite;
    private ClickDetector clickDetector;

    public TourHUD(final PhoeniciaGame game, final Stop stop) {
        super(game.camera);
        this.game = game;
        this.stop = stop;

        this.setBackgroundEnabled(false);
        this.clickDetector = new ClickDetector(new ClickDetector.IClickDetectorListener() {
            @Override
            public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
                game.hudManager.pop();
            }
        });

        this.guideSprite = new Sprite(-128, 192, game.personTiles.get(stop.tour.guide), PhoeniciaContext.vboManager);
        this.attachChild(guideSprite);

        // TODO: Display message box
    }

    @Override
    public void open() {
        if (this.stop.hasFocus()) {
            MapBlockSprite focus = this.stop.getFocus();
            this.getCamera().setCenter(focus.getX(), focus.getY());
            ((SmoothCamera)this.getCamera()).setZoomFactor(2.0f);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void show() {
        this.guideSprite.registerEntityModifier(new MoveXModifier(0.5f, -128, 128));
        // TODO: Display messages
    }

    /**
     * Capture scene touch events and look for click/touch events on the map to trigger placement.
     * All other touch events are passed through.
     *
     * @param pSceneTouchEvent
     * @return
     */
    @Override
    public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
        final boolean handled = super.onSceneTouchEvent(pSceneTouchEvent);
        if (handled) return true;

        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent) || true;
    }
}
