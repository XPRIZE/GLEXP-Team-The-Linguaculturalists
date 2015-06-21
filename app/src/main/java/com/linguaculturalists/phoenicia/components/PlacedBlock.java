package com.linguaculturalists.phoenicia.components;

import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by mhall on 6/19/15.
 */
public class PlacedBlock extends ButtonSprite {
    public PlacedBlock(float pX, float pY, ITextureRegion pNormalTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pNormalTextureRegion, pVertexBufferObjectManager);
    }
}
