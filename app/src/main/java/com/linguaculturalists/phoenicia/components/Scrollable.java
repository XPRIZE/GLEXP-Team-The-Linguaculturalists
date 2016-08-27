package com.linguaculturalists.phoenicia.components;

import android.opengl.GLES20;

import com.linguaculturalists.phoenicia.PhoeniciaGame;
import com.linguaculturalists.phoenicia.util.PhoeniciaContext;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.CameraScene;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.util.GLState;
import org.andengine.util.Constants;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.math.MathUtils;

/**
 * Allows scrolling child \link Entity Entities \endlink  within it
 * A container Entity that allows the vertical and/or horizontal scrolling of \link Entity Entities \endlink placed within
 * it.
 */
public class Scrollable extends Entity implements ScrollDetector.IScrollDetectorListener, ITouchArea {

    public static final int SCROLL_BOTH = 0; /**< Allow scrolling both vertical and horizontal */
    public static final int SCROLL_VERTICAL = 1; /**< Lock scrolling direction to vertical only */
    public static final int SCROLL_HORIZONTAL = 2; /**< Lock scrolling direction to horizontal only */
    public IEntity contents; /**< Container for all attached child Entities */
    private SurfaceScrollDetector scrollDetector;
    private int scroll_lock;
    private boolean touch_was_scroll = false;
    private boolean is_scrolling = false;
    private boolean isInHUD = false;
    private boolean clip = true;
    private float padding = 0;
    private boolean show_scrollbars;
    private Color scrollbar_color;
    private Rectangle vertical_scrollbar;
    private Rectangle horizontal_scrollbar;
    private static final int scrollbar_size = 8;
    private float scroll_x = 0;
    private float scroll_y = 0;

    private boolean print_debug = false;

    public final ChildRect childRect = new ChildRect(); /**< Bounding box that holds all child Entities */

    /**
     * New Scrollable with with the desired width and height.
     * The width and height must be specified because a Scrollable will be smaller than the size
     * needed for all of it's children
     * @param x the X coordinate of the scene to place this Scrollable
     * @param y the Y coordinate of the scene to place this Scrollable
     * @param w the width for this Scrollable
     * @param h the height for this Scrollable
     */
    public Scrollable(float x, float y, float w, float h) {
        this(x, y, w, h, SCROLL_BOTH);
    }

    /**
     * New Scrollable with with the desired width and height.
     * The width and height must be specified because a Scrollable will be smaller than the size
     * needed for all of it's children
     * @param x the X coordinate of the scene to place this Scrollable
     * @param y the Y coordinate of the scene to place this Scrollable
     * @param w the width for this Scrollable
     * @param h the height for this Scrollable
     * @param scroll_lock what directions can be scrolled (default Scrollable.SCROLL_BOTH)
     */
    public Scrollable(float x, float y, float w, float h, int scroll_lock) {
        super(x, y, w, h);
        this.scroll_lock = scroll_lock;
        this.scrollDetector = new SurfaceScrollDetector(this);

        this.contents = new Entity(w/2, h/2, 0, 0);
        this.childRect.set(0, 0, 0, 0);

        super.attachChild(this.contents);

        this.scrollbar_color = new Color(0.0f, 0.0f, 0.0f, 0.25f);
        this.vertical_scrollbar = new Rectangle(this.getWidth()-(this.scrollbar_size /2)-2, this.getHeight()/2, scrollbar_size, this.getHeight(), PhoeniciaContext.vboManager);
        this.vertical_scrollbar.setColor(this.scrollbar_color);
        if (this.scroll_lock == SCROLL_HORIZONTAL) this.vertical_scrollbar.setVisible(false);
        this.scroll_y = 0;
        super.attachChild(this.vertical_scrollbar);

        this.horizontal_scrollbar = new Rectangle(this.getWidth()/2, (scrollbar_size/2)+2, this.getWidth(), scrollbar_size, PhoeniciaContext.vboManager);
        this.horizontal_scrollbar.setColor(this.scrollbar_color);
        if (this.scroll_lock == SCROLL_VERTICAL) this.horizontal_scrollbar.setVisible(false);
        this.scroll_x = 0;
        super.attachChild(this.horizontal_scrollbar);

    }

    /**
     * Set GL clipping of child Entities.
     * OpenGL clipping is used to hide sprites that are outside the bounds of the Scrollable itself.
     * This can be disabled if you want the children to remain visible
     * @param clip whether or not to hide child entities outside of the Scrollable
     */
    public void setClip(boolean clip) {
        this.clip = clip;
    }


    public float getPadding() {
        return padding;
    }

    public void setPadding(float padding) {
        this.padding = padding;
    }

    public void showScrollbars(boolean show) {
        this.show_scrollbars = show;
    }

    public boolean showScrollbars() {
        return this.show_scrollbars;
    }

    /**
     * Holds the current minimum boundary coordinates that hold all child Entities.
     */
    public class ChildRect {
        public float left; /**< The X coordinate for the left edge of the boundary rectangle */
        public float right; /**< The X coordinate for the right edge of the boundary rectangle */
        public float top; /**< The Y coordinate for the top edge of the boundary rectangle */
        public float bottom; /**< The Y coordinate for the bottom edge of the boundary rectangle */

        public ChildRect() {

        }

        /**
         * Set the size and location of the rectangle
         * @param x the X coordinate from the Scene of the boundary rectangle
         * @param y the Y coordinate from the Scene of the boundary rectangle
         * @param w the width if the boundary rectangle
         * @param h the height of the boundary rectangle
         */
        public void set(float x, float y, float w, float h) {
            this.left = x;
            this.right = x + w;
            this.bottom = y;
            this.top = y + h;
        }

        /**
         * Current width of the boundary rectangle
         * @return the difference between the right and left edges
         */
        public float getWidth() {
            return this.right - this.left;
        }

        /**
         * Current height of the boundary rectangle
         * @return the difference between the top and bottom edges
         */
        public float getHeight() {
            return this.top - this.bottom;
        }
    }

    /**
     * Recalculate the ChildRect values for child Entities
     * Whenever an Entity is added or removed from the Scrollable, it's ChildRect needs to be
     * re-calculated to find the new boundary rectangle that will contain them
     */
    private void recalculateContentBounds() {
        this.childRect.set(0, 0, 0, 0);
        for(int i = 0; i < this.contents.getChildCount(); i++) {
            IEntity child = this.contents.getChildByIndex(i);
            //Debug.d("Scrollable Recalculating with child: "+child);
            final float childLeft = child.getX()-(child.getWidth()/2)-this.padding;
            final float childRight = child.getX()+(child.getWidth()/2)+this.padding;
            final float childBottom = child.getY()-(child.getHeight()/2)-this.padding;
            final float childTop = child.getY()+(child.getHeight()/2)+this.padding;
            if (childLeft   < this.childRect.left)   { this.childRect.left = childLeft; }
            if (childRight  > this.childRect.right)  { this.childRect.right = childRight; }
            if (childTop    > this.childRect.top)    { this.childRect.top = childTop; }
            if (childBottom < this.childRect.bottom) { this.childRect.bottom = childBottom; }

        }
        //Debug.d("Scrollable childRect: x(" + this.childRect.left + "," + this.childRect.right + ") y(" + this.childRect.bottom + "," + this.childRect.top + ")");
        this.contents.setWidth(this.childRect.getWidth());
        this.contents.setHeight(this.childRect.getHeight());
        this.contents.setPosition(this.childRect.getWidth() / 2, this.childRect.getHeight() / 2);

        float new_height = this.getHeight() * (this.getHeight() / this.contents.getHeight());
        this.vertical_scrollbar.setSize(scrollbar_size, new_height);
        this.vertical_scrollbar.setPosition(this.vertical_scrollbar.getX(), this.getHeight()-(this.vertical_scrollbar.getHeight()/2)-this.scroll_y);
        if (this.vertical_scrollbar.getHeight() >= this.getHeight() || this.scroll_lock == SCROLL_HORIZONTAL) {
            this.vertical_scrollbar.setVisible(false);
        } else {
            this.vertical_scrollbar.setVisible(true);
        }

        float new_width = this.getWidth() * (this.getWidth() / this.contents.getWidth());
        this.horizontal_scrollbar.setSize(new_width, scrollbar_size);
        this.horizontal_scrollbar.setPosition((this.horizontal_scrollbar.getWidth()/2)+scroll_x, this.horizontal_scrollbar.getY());
        if (this.horizontal_scrollbar.getWidth() >= this.getWidth() || this.scroll_lock == SCROLL_VERTICAL) {
            this.horizontal_scrollbar.setVisible(false);
        } else {
            this.horizontal_scrollbar.setVisible(true);
        }
    }

    /**
     * \todo Implement code to snap the Scrollable's contents back into view when they change
     */
    private void returnToBounds() {

    }

    private void positionScrollbars() {

    }

    @Override
    public boolean onAreaTouched(final TouchEvent pTouchEvent, final float touchX, final float touchY) {
        boolean handled = this.scrollDetector.onManagedTouchEvent(pTouchEvent);
        //Debug.d("scrollDetector: "+handled);
        if (this.touch_was_scroll || this.is_scrolling) {
            //Debug.d("Touch was scroll ");
            this.touch_was_scroll = false;
            return true;
        } else  {
            //Debug.d("Touch was not scroll ");
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
        //Debug.d("Scrollable.onScroll(scrollDetector, "+pointerId+", "+dx+", "+dy+")");
        this.touch_was_scroll = true;
        dx = dx / 2;
        dy = dy / 2;

        final float currentX = this.contents.getX();
        final float currentY = this.contents.getY();
        float newX = currentX;
        float newY = currentY;
        if (this.scroll_lock != SCROLL_VERTICAL) {
            float newLeft  = currentX - (this.contents.getWidth()/2) + dx;
            float newRight = currentX + (this.contents.getWidth()/2) + dx;
            //Debug.d("Scrollable Checking X bounds ["+this.childRect.left+", "+this.getWidth()+"] contains ("+newLeft+", "+newRight+")");
            if (newLeft+this.childRect.left <= 0 && newRight >= this.getWidth()) {
                newX = currentX + dx;
                float scrollX = (dx / this.childRect.getWidth()) * this.getWidth();
                this.horizontal_scrollbar.setPosition(this.horizontal_scrollbar.getX()-scrollX, this.horizontal_scrollbar.getY());
            }
        }
        if (this.scroll_lock != SCROLL_HORIZONTAL) {
            float newTop    = currentY + (this.contents.getHeight()/2) - dy;
            float newBottom = currentY - (this.contents.getHeight()/2) - dy;
            //Debug.d("Scrollable Checking Y bounds ["+this.childRect.bottom+ ", "+this.getHeight()+"] contains ("+newBottom+", "+newTop+")");
            if (newBottom+this.childRect.bottom <= 0 && newTop-this.contents.getHeight() >= 0) {
                newY = currentY - dy;
                float scrollY = (dy / this.childRect.getHeight()) * this.getHeight();
                this.vertical_scrollbar.setPosition(this.vertical_scrollbar.getX(), this.vertical_scrollbar.getY()+scrollY);
            }
        }
        // Stop at bounds
        //Debug.d("Scrollable newX="+newX);
        //Debug.d("Scrollable newY="+newY);
        this.contents.setPosition(newX, newY);
        //Debug.d("this.contents.setPosition("+newX+", "+newY+")");
    }

    @Override
    public void onScrollFinished(ScrollDetector scrollDetector, int i, float v, float v2) {
        this.is_scrolling = false;
    }

    @Override
    public void detachChildren() {
        this.contents.detachChildren();
        this.recalculateContentBounds();
    }

    @Override
    public void attachChild(IEntity child) {
        //Debug.d("Scrollable.attachChild("+child+")");
        this.contents.attachChild(child);
        this.recalculateContentBounds();
    }

    @Override
    public boolean detachChild(IEntity pEntity) {
        final boolean ret = this.contents.detachChild(pEntity);
        this.recalculateContentBounds();
        return ret;
    }

    /**
     * Because clipping uses different coordinate transformation depending on whether the Scrollable
     * is in a normal Scene or a HUD, we have to check where this Scrollable is being attached and
     * set the isInHUD member appropriately.
     */
    @Override
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

    /**
     * Perform clipping using normal Scene coordinate transformations
     * @param pGLState
     * @param pCamera
     */
    private void clipForCamera(GLState pGLState, Camera pCamera) {
        final int surfaceHeight = pCamera.getSurfaceHeight();

			/* In order to apply clipping, we need to determine the the axis aligned bounds in OpenGL coordinates. */

			/* Determine clipping coordinates of each corner in surface coordinates. */
        final float[] lowerLeftSurfaceCoordinates = pCamera.getSurfaceCoordinatesFromSceneCoordinates(this.convertLocalCoordinatesToSceneCoordinates(0, 0, new float[2]));
        final int lowerLeftX = (int)Math.round(lowerLeftSurfaceCoordinates[Constants.VERTEX_INDEX_X]);
        final int lowerLeftY = surfaceHeight - (int)Math.round(lowerLeftSurfaceCoordinates[Constants.VERTEX_INDEX_Y]);

        final float[] upperLeftSurfaceCoordinates = pCamera.getSurfaceCoordinatesFromSceneCoordinates(this.convertLocalCoordinatesToSceneCoordinates(0, this.mHeight, new float[2]));
        final int upperLeftX = (int)Math.round(upperLeftSurfaceCoordinates[Constants.VERTEX_INDEX_X]);
        final int upperLeftY = surfaceHeight - (int)Math.round(upperLeftSurfaceCoordinates[Constants.VERTEX_INDEX_Y]);

        final float[] upperRightSurfaceCoordinates = pCamera.getSurfaceCoordinatesFromSceneCoordinates(this.convertLocalCoordinatesToSceneCoordinates(this.mWidth, this.mHeight, new float[2]));
        final int upperRightX = (int)Math.round(upperRightSurfaceCoordinates[Constants.VERTEX_INDEX_X]);
        final int upperRightY = surfaceHeight - (int)Math.round(upperRightSurfaceCoordinates[Constants.VERTEX_INDEX_Y]);

        final float[] lowerRightSurfaceCoordinates = pCamera.getSurfaceCoordinatesFromSceneCoordinates(this.convertLocalCoordinatesToSceneCoordinates(this.mWidth, 0, new float[2]));
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

    /**
     * Perform clipping using HUD coordinate transformations
     * @param pGLState
     * @param pCamera
     */
    private void clipForHUD(GLState pGLState, Camera pCamera) {
        float[] coordinates = this.getParent().convertLocalCoordinatesToSceneCoordinates(this.mX, this.mY, new float[2]);
        float[] size = this.getParent().convertLocalCoordinatesToSceneCoordinates(this.mWidth, this.mHeight, new float[2]);
        final float zoom = ((ZoomCamera)pCamera).getZoomFactor();
        final float screenRatioX = pCamera.getSurfaceWidth()/pCamera.getWidth();
        final float screenRatioY = pCamera.getSurfaceHeight()/pCamera.getHeight();
        final float left = (this.mX - (this.mWidth/2)) * screenRatioX / zoom;
        final float bottom = (this.mY - (this.mHeight/2)) * screenRatioY / zoom;
        final float width = this.mWidth * screenRatioX / zoom;
        final float height = this.mHeight * screenRatioY / zoom;
        if (print_debug) {
            Debug.d("Scrollable X: " + this.mX);
            Debug.d("Scrollable Y: " + this.mY);
            Debug.d("Scrollable W: " + this.mWidth);
            Debug.d("Scrollable H: " + this.mHeight);
            Debug.d("Scrollable x,y: "+coordinates[Constants.VERTEX_INDEX_X]+","+coordinates[Constants.VERTEX_INDEX_Y]);
            Debug.d("Scrollable w,h: " + size[Constants.VERTEX_INDEX_X]+","+size[Constants.VERTEX_INDEX_Y]);
            Debug.d("clipForHUD: GLES20.glScissor("+left+", "+bottom+", "+width+", "+height+")");
            Debug.d("Scrollable camera zoom: " + zoom);
            Debug.d("Scrollable screenRatioX: " + pCamera.getSurfaceWidth()/pCamera.getWidth());
            Debug.d("Scrollable screenRatioY: " + pCamera.getSurfaceHeight()/pCamera.getHeight());
            print_debug = false;
        }
        GLES20.glScissor((int)left, (int)bottom, (int)width, (int)height);
        //        Math.round(((clipX + point.x)) * screenRatioX),
        //        Math.round((cameraH - ((clipY + point.y) + clipH)) * screenRatioY),
        //        Math.round(clipW * screenRatioX),
        //        Math.round(clipH * screenRatioY));

    }

    @Override
    protected void onManagedDraw(GLState pGLState, Camera pCamera) {

        if (!this.clip) {
            super.onManagedDraw(pGLState, pCamera);
        }

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
