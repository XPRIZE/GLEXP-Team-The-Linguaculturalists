package com.linguaculturalists.phoenicia.components;

import com.linguaculturalists.phoenicia.ui.SpriteMoveHUD;
import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.input.touch.detector.HoldDetector;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBackOut;
import org.andengine.util.modifier.ease.EaseLinear;

/**
 * An AnimatedSprite that represents a block on the game map.
 */
public class MapBlockSprite extends AnimatedSprite implements ClickDetector.IClickDetectorListener, HoldDetector.IHoldDetectorListener {

    protected long[] mFrameDurations = {500, 500, 500, 500};
    protected int mTileId;
    protected int startTile;
    protected String restriction;

    protected OnClickListener mOnClickListener;
    protected ClickDetector clickDetector;
    protected HoldDetector holdDetector;

    /**
     * Create a new PlacedBlockSprite.
     * @param pX the X coordinate of the scene to place this PlacedBlockSprite
     * @param pY the Y coordinate of the scene to place this PlacedBlockSprite
     * @param pTileId the index of first tile of the first animation set from pTiledTextureRegion
     * @param pTiledTextureRegion region containing the tile set for this PlacedBlockSprite
     * @param pVertexBufferObjectManager the game's VertexBufferObjectManager
     */
    public MapBlockSprite(final float pX, final float pY, final int pTileId, final ITiledTextureRegion pTiledTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager);
        this.mTileId = pTileId;
        this.startTile = pTileId;
        this.setCurrentTileIndex(pTileId);

        this.clickDetector = new ClickDetector(this);
        this.holdDetector = new HoldDetector(this);
        this.holdDetector.setTriggerHoldMinimumMilliseconds(1000);
    }

    public String getRestriction() {
        return this.restriction;
    }
    /**
     * Start the animation of this sprite.
     * AnimatedSprites do not begin their animation sequence at the time they are created, they must
     * be started by other code.
     */
    public void animate() {
        this.animate(this.mFrameDurations, this.startTile, this.startTile + mFrameDurations.length - 1, true);
    }

    /**
     * Add a Click listener for this sprite.
     * @param pOnClickListener listener to be called when this sprite is clicked
     */
    public void setOnClickListener(final OnClickListener pOnClickListener) {
        this.mOnClickListener = pOnClickListener;
    }

    @Override
    public void onClick(ClickDetector clickDetector, int pointerId, float touchX, float touchY) {
        if (this.mOnClickListener != null) {
            this.mOnClickListener.onClick(this, touchX, touchY);
        }
    }

    @Override
    public void onHold(HoldDetector holdDetector, long holdTime, int pointerId, float touchX, float touchY) {
        Debug.d("Holding");
        return;
    }

    @Override
    public void onHoldFinished(HoldDetector holdDetector, long holdTime, int pointerId, float touchX, float touchY) {
        Debug.d("Hold finished");
        return;
    }

    @Override
    public void onHoldStarted(HoldDetector holdDetector, int pointerId, float touchX, float touchY) {
        Debug.d("Hold started");
        if (this.mOnClickListener != null) {
            this.mOnClickListener.onHold(this, touchX, touchY);
        }
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
        boolean handled = this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
        return this.holdDetector.onManagedTouchEvent(pSceneTouchEvent) || handled;
    }

    /**
     * Callback interface for handling Click events on a MapBlockSprite.
     */
    public interface OnClickListener {
        /**
         * Called when this sprite is clicked
         * @param pPlacedBlockSprite
         * @param pTouchAreaLocalX
         * @param pTouchAreaLocalY
         */
        public void onClick(final MapBlockSprite pPlacedBlockSprite, final float pTouchAreaLocalX, final float pTouchAreaLocalY);
        public void onHold(final MapBlockSprite pPlacedBlockSprite, final float pTouchAreaLocalX, final float pTouchAreaLocalY);
    }
}
