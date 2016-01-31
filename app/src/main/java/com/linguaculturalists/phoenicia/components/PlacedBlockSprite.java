package com.linguaculturalists.phoenicia.components;

import com.linguaculturalists.phoenicia.util.GameFonts;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.modifier.MoveYModifier;
import org.andengine.entity.modifier.ScaleAtModifier;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ClickDetector;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseBackOut;
import org.andengine.util.modifier.ease.EaseLinear;

/**
 * An AnimatedSprite that represents a block on the game map.
 * PlacedBlockSprites maintain their own build progress and images tiles, and will update the set
 * used in the animation based on their current progress.
 *
 * PlacedBlockSprites assume 4 sets of animation tiles, which are used for 0-33%, 34-66%, 67-100%
 * and 100% onward.
 */
public class PlacedBlockSprite extends AnimatedSprite implements ClickDetector.IClickDetectorListener {

    private OnClickListener mOnClickListener;
    private long[] mFrameDurations = {500, 500, 500, 500};
    private int mTileId;
    private int mProgress;
    private int mTime;
    private int startTile;
    private boolean complete;

    private ClickDetector clickDetector;
    /**
     * Create a new PlacedBlockSprite.
     * @param pX the X coordinate of the scene to place this PlacedBlockSprite
     * @param pY the Y coordinate of the scene to place this PlacedBlockSprite
     * @param pTileId the index of first tile of the first animation set from pTiledTextureRegion
     * @param pTiledTextureRegion region containing the tile set for this PlacedBlockSprite
     * @param pVertexBufferObjectManager the game's VertexBufferObjectManager
     */
    public PlacedBlockSprite(final float pX, final float pY, final int pTime, final int pTileId, final ITiledTextureRegion pTiledTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager);
        this.mTileId = pTileId;
        this.startTile = pTileId;
        this.setCurrentTileIndex(pTileId);
        this.mTime = pTime;
        this.mProgress = 0;
        this.complete = false;

        this.clickDetector = new ClickDetector(this);
    }

    /**
     * Update the current progress of the build for this PlacedBlockSprite.
     * Updating the progress may change the animation set in use. The current percentage of progress
     * is determined by pProgress/pTime.
     * @param pProgress new progress time in seconds
     * @param pTime the total time the build should take
     */
    public void setProgress(int pProgress, int pTime) {
        this.mProgress = pProgress;
        int completed = (pProgress*100) / pTime;
        int newStartTile = 0;
        if (completed < 33) {
            newStartTile = mTileId;
        } else if (completed < 66) {
            newStartTile = mTileId+4;
        } else if (completed < 100) {
            newStartTile = mTileId+8;
        } else {
            newStartTile = mTileId+12;
            this.complete = true;
        }

        if (newStartTile != this.startTile) {
            this.startTile = newStartTile;
            this.animate(this.mFrameDurations, startTile, startTile+mFrameDurations.length-1, true);
        }
    }

    /**
     * Determine if this PlacedBlockSprite's construction is finished.
     * @return true if the build has finished, otherwise false
     */
    public boolean isComplete() {
        return this.complete;
    }

    /**
     * Get the current progress time for this PlacedBlockSprite's construction.
     * @return time (in seconds) that the build has been running
     */
    public int getProgress() {
        return this.mProgress;
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
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
        return this.clickDetector.onManagedTouchEvent(pSceneTouchEvent);
    }

    /**
     * Callback interface for handling Click events on a PlacedBlockSprite.
     */
    public interface OnClickListener {
        /**
         * Called when this sprite is clicked
         * @param pPlacedBlockSprite
         * @param pTouchAreaLocalX
         * @param pTouchAreaLocalY
         */
        public void onClick(final PlacedBlockSprite pPlacedBlockSprite, final float pTouchAreaLocalX, final float pTouchAreaLocalY);
    }
}
