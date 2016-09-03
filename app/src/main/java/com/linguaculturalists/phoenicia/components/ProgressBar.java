package com.linguaculturalists.phoenicia.components;

import android.opengl.GLES20;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.vbo.ISpriteVertexBufferObject;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

/**
 * Created by mhall on 8/30/16.
 */
public class ProgressBar extends Sprite implements ProgressDisplay {
    private float progress = 0.0f;

    public ProgressBar(float pX, float pY, ITextureRegion pTextureRegion, VertexBufferObjectManager pVertexBufferObjectManager) {
        super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
        this.progress = 0.5f;
        this.setVisible(true);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        Debug.d("ProgressBar.progress: " + progress);
        this.progress = progress;
    }

    @Override
    protected void onManagedDraw(GLState pGLState, Camera pCamera) {
        final boolean wasScissorTestEnabled = pGLState.enableScissorTest();

        this.clipToProgress(pGLState, pCamera);

        /* Draw children, etc... */
        super.onManagedDraw(pGLState, pCamera);

        /* Revert scissor test to previous state. */
        pGLState.setScissorTestEnabled(wasScissorTestEnabled);

    }

    private void clipToProgress(GLState pGLState, Camera pCamera) {
        final float zoom = ((ZoomCamera)pCamera).getZoomFactor();
        final float screenRatioX = pCamera.getSurfaceWidth()/pCamera.getWidth();
        final float screenRatioY = pCamera.getSurfaceHeight()/pCamera.getHeight();
        final float left = (this.mX - (this.mWidth/2)) * screenRatioX / zoom;
        final float bottom = (this.mY - (this.mHeight/2)) * screenRatioY / zoom;
        final float width = (this.mWidth * screenRatioX / zoom) * this.progress;
        final float height = this.mHeight * screenRatioY / zoom;
        GLES20.glScissor((int) left, (int) bottom, (int) width, (int) height);
    }

}
