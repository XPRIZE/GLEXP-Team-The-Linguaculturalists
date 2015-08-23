package com.linguaculturalists.phoenicia.components;

import android.graphics.PointF;
import android.opengl.GLES20;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.clip.ClipEntity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.util.GLState;
import org.andengine.util.Constants;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathUtils;

/**
 * Created by mhall on 8/21/15.
 */
public class Scrollable extends Entity implements ScrollDetector.IScrollDetectorListener, ITouchArea {

    public static final int SCROLL_BOTH = 0;
    public static final int SCROLL_VERTICAL = 1;
    public static final int SCROLL_HORIZONTAL = 2;
    public IEntity contents;
    private SurfaceScrollDetector scrollDetector;
    private int scroll_lock;
    private boolean touch_was_scroll = false;
    private boolean is_scrolling = false;
    private boolean isInHUD = false;

    public Scrollable(float x, float y, float w, float h) {
        this(x, y, w, h, SCROLL_BOTH);
    }

    public Scrollable(float x, float y, float w, float h, int scroll_lock) {
        super(x, y, w, h);
        this.scroll_lock = scroll_lock;
        this.scrollDetector = new SurfaceScrollDetector(this);

        this.contents = new Entity(x, y, w, h);
        super.attachChild(this.contents);

    }

    @Override
    public boolean onAreaTouched(final TouchEvent pTouchEvent, final float touchX, final float touchY) {
        boolean handled = this.scrollDetector.onManagedTouchEvent(pTouchEvent);
        Debug.d("scrollDetector: "+handled);
        if (this.touch_was_scroll || this.is_scrolling) {
            Debug.d("Touch was scroll ");
            this.touch_was_scroll = false;
            return true;
        } else  {
            Debug.d("Touch was not scroll ");
            this.touch_was_scroll = false;
            return false;
        }
    }

    @Override
    public void onScrollStarted(ScrollDetector scrollDetector, int i, float v, float v2) {
        this.is_scrolling = true;
    }

    @Override
    public void onScroll(ScrollDetector scrollDetector, int pointerId, float dx, float dy) {
        Debug.d("Scrollable.onScroll(scrollDetector, "+pointerId+", "+dx+", "+dy+")");
        this.touch_was_scroll = true;

        float newX = this.contents.getX();
        float newY = this.contents.getY();
        if (this.scroll_lock != SCROLL_VERTICAL) {
            newX = newX + dx;
        }
        if (this.scroll_lock != SCROLL_HORIZONTAL) {
            newY = newY - dy;
        }
        // Stop at bounds
        float left = newX+(this.contents.getWidth()/2);
        float bottom = newY+(this.contents.getHeight()/2);
        Debug.d("Checking bounds for "+left+", "+bottom);
        if (newX-(this.contents.getWidth()/2) < this.getX()-(this.getWidth()/2) || newX+(this.contents.getWidth()/2) > this.getX()+(this.getWidth()/2)) {
            //newX = this.contents.getX();
        }
        if (newY-(this.contents.getHeight()/2) < this.getY()-(this.getHeight()/2) || newY+(this.contents.getHeight()/2) > this.getY()+(this.getHeight()/2)) {
            //newY = this.contents.getY();
        }
        this.contents.setPosition(newX, newY);
        Debug.d("this.contents.setPosition("+newX+", "+newY+")");
    }

    @Override
    public void onScrollFinished(ScrollDetector scrollDetector, int i, float v, float v2) {
        this.is_scrolling = false;
    }

    @Override
    public void attachChild(IEntity child) {
        Debug.d("Scrollable.attachChild("+child+")");
        this.contents.attachChild(child);

    }

    public void onAttached() {
        IEntity node = this;
        while (node.hasParent()) {
            node = node.getParent();
            if (node instanceof HUD || node instanceof CameraScene) {
                this.isInHUD = true;
                return;
            }
        }

    }
    private void clipForCamera(GLState pGLState, Camera pCamera) {
        final int surfaceHeight = pCamera.getSurfaceHeight();

			/* In order to apply clipping, we need to determine the the axis aligned bounds in OpenGL coordinates. */

			/* Determine clipping coordinates of each corner in surface coordinates. */
        final float[] lowerLeftSurfaceCoordinates = pCamera.getSurfaceCoordinatesFromSceneCoordinates(this.convertLocalCoordinatesToSceneCoordinates(0, 0));
        final int lowerLeftX = (int)Math.round(lowerLeftSurfaceCoordinates[Constants.VERTEX_INDEX_X]);
        final int lowerLeftY = surfaceHeight - (int)Math.round(lowerLeftSurfaceCoordinates[Constants.VERTEX_INDEX_Y]);

        final float[] upperLeftSurfaceCoordinates = pCamera.getSurfaceCoordinatesFromSceneCoordinates(this.convertLocalCoordinatesToSceneCoordinates(0, this.mHeight));
        final int upperLeftX = (int)Math.round(upperLeftSurfaceCoordinates[Constants.VERTEX_INDEX_X]);
        final int upperLeftY = surfaceHeight - (int)Math.round(upperLeftSurfaceCoordinates[Constants.VERTEX_INDEX_Y]);

        final float[] upperRightSurfaceCoordinates = pCamera.getSurfaceCoordinatesFromSceneCoordinates(this.convertLocalCoordinatesToSceneCoordinates(this.mWidth, this.mHeight));
        final int upperRightX = (int)Math.round(upperRightSurfaceCoordinates[Constants.VERTEX_INDEX_X]);
        final int upperRightY = surfaceHeight - (int)Math.round(upperRightSurfaceCoordinates[Constants.VERTEX_INDEX_Y]);

        final float[] lowerRightSurfaceCoordinates = pCamera.getSurfaceCoordinatesFromSceneCoordinates(this.convertLocalCoordinatesToSceneCoordinates(this.mWidth, 0));
        final int lowerRightX = (int)Math.round(lowerRightSurfaceCoordinates[Constants.VERTEX_INDEX_X]);
        final int lowerRightY = surfaceHeight - (int)Math.round(lowerRightSurfaceCoordinates[Constants.VERTEX_INDEX_Y]);

			/* Determine minimum and maximum x clipping coordinates. */
        final int minClippingX = MathUtils.min(lowerLeftX, upperLeftX, upperRightX, lowerRightX);
        final int maxClippingX = MathUtils.max(lowerLeftX, upperLeftX, upperRightX, lowerRightX);

			/* Determine minimum and maximum y clipping coordinates. */
        final int minClippingY = MathUtils.min(lowerLeftY, upperLeftY, upperRightY, lowerRightY);
        final int maxClippingY = MathUtils.max(lowerLeftY, upperLeftY, upperRightY, lowerRightY);

			/* Determine clipping width and height. */
        final int clippingWidth = maxClippingX - minClippingX;
        final int clippingHeight = maxClippingY - minClippingY;
        //Debug.d("ClippingY: min="+minClippingY+", max="+maxClippingY);
        //Debug.d("clipForCamera: GLES20.glScissor("+minClippingX+", "+minClippingY+", "+clippingWidth+", "+clippingHeight+")");
        GLES20.glScissor(minClippingX, minClippingY, clippingWidth, clippingHeight);
    }

    private void clipForHUD(GLState pGLState, Camera pCamera) {
        final float screenRatioX = 1.53333f;
        final float screenRatioY = 1.53333f;//pCamera.getWidth()/pCamera.getHeight();
        final float left = (this.getX() - (this.getWidth()/2)) * screenRatioX;
        final float bottom = (this.getY() - (this.getHeight()/2)) * screenRatioY;
        final float width = this.getWidth() * screenRatioX;
        final float height = this.getHeight() * screenRatioY;
        //Debug.d("clipForHUD: GLES20.glScissor("+left+", "+bottom+", "+width+", "+height+")");
        GLES20.glScissor((int)left, (int)bottom, (int)width, (int)height);
        //        Math.round(((clipX + point.x)) * screenRatioX),
        //        Math.round((cameraH - ((clipY + point.y) + clipH)) * screenRatioY),
        //        Math.round(clipW * screenRatioX),
        //        Math.round(clipH * screenRatioY));

    }

    @Override
    protected void onManagedDraw(GLState pGLState, Camera pCamera) {

        final boolean wasScissorTestEnabled = pGLState.enableScissorTest();

        if (this.isInHUD) {
            this.clipForHUD(pGLState, pCamera);
        } else {
            this.clipForCamera(pGLState, pCamera);
        }

        /* Draw children, etc... */
        super.onManagedDraw(pGLState, pCamera);

        /* Revert scissor test to previous state. */
        pGLState.setScissorTestEnabled(wasScissorTestEnabled);

    }
}
