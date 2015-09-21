package com.linguaculturalists.phoenicia.components;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by mhall on 9/17/15.
 */
public class PlacedBlockSprite extends AnimatedSprite {

    private OnClickListener mOnClickListener;
    private long[] mFrameDurations = {1000, 1000, 1000, 1000};
    private int mTileId;

    public PlacedBlockSprite(final float pX, final float pY, final int pTileId, final ITiledTextureRegion pTiledTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager);
        this.mTileId = pTileId;
    }

    public void animate() {
        this.animate(this.mFrameDurations, this.mTileId, this.mTileId+mFrameDurations.length-1, true);
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
