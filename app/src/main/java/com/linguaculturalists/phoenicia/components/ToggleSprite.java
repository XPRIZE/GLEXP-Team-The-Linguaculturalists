package com.linguaculturalists.phoenicia.components;

import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by mhall on 1/1/17.
 */
public class ToggleSprite extends TiledSprite implements ClickDetector.IClickDetectorListener {
    private ClickDetector clickDetector;
    private boolean enabled;

    public ToggleSprite(final float pX, final float pY, ITiledTextureRegion textureRegion, VertexBufferObjectManager vertexBufferObjectManager) {
        super(pX, pY, textureRegion, vertexBufferObjectManager);
        this.clickDetector = new ClickDetector(this);
        this.enabled = true;
        this.setCurrentTileIndex(0);
    }

    @Override
    public void onClick(ClickDetector clickDetector, int i, float v, float v1) {
        this.setEnabled(!this.enabled);
        this.onToggled();
    }

    public void onToggled() {

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (this.enabled) {
            this.setCurrentTileIndex(0);
        } else {
            this.setCurrentTileIndex(1);
        }
    }

    @Override
    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent) || super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
    }
}
