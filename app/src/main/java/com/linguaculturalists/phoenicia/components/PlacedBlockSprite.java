package com.linguaculturalists.phoenicia.components;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 9/17/15.
 */
public class PlacedBlockSprite extends AnimatedSprite {

    private OnClickListener mOnClickListener;
    private long[] mFrameDurations = {500, 500, 500, 500};
    private int mTileId;
    private int mProgress;
    private int startTile;
    private boolean complete;

    public PlacedBlockSprite(final float pX, final float pY, final int pTileId, final ITiledTextureRegion pTiledTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager);
        this.mTileId = pTileId;
        this.startTile = pTileId;
        this.mProgress = 0;
        this.complete = false;
    }

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

    public boolean isComplete() {
        return this.complete;
    }
    public int getProgress() {
        return this.mProgress;
    }
    public void animate() {
        this.animate(this.mFrameDurations, this.startTile, this.startTile + mFrameDurations.length - 1, true);
    }

    public void setOnClickListener(final OnClickListener pOnClickListener) {
        this.mOnClickListener = pOnClickListener;
    }

    @Override
    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
        if (pSceneTouchEvent.isActionUp()) {

            if (this.mOnClickListener != null) {
                this.mOnClickListener.onClick(this, pTouchAreaLocalX, pTouchAreaLocalY);
            }
            return true;
        }
        return false;

    }

    public interface OnClickListener {
        public void onClick(final PlacedBlockSprite pPlacedBlockSprite, final float pTouchAreaLocalX, final float pTouchAreaLocalY);
    }
}
