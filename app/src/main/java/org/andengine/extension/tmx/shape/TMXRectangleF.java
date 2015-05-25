package org.andengine.extension.tmx.shape;

import android.R.array;

/**
 * A rectangle class to store {@link Float} values of a TMX rectangle object.
 * This will automatically work out the other 3 corner coordinates when they are 
 * requested.
 * @author Paul Robinson
 *
 */
public class TMXRectangleF {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	/**
	 * [0] = X <br> 
	 * [1] = Y
	 */
	private float[] position = {0,0};
	/**
	 * [0] = width <br>
	 * [1] = height
	 */
	private float[] size = {0,0};

	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * Create a rectangle using the top left coordinate and size
	 * @param pPostion {@link float} {@link array} of position of top left 
	 * <br><i> Element [0] = X</i> 
	 * <br><i>Element [1] = Y</i>
	 * @param pSize {@link float} {@link array} of Size 
	 * <br> <i> Element [0] = Width</i>
	 * <br> <i> Element [1] = Height</i>
	 */
	public TMXRectangleF(final float[] pPosition, final float[] pSize){
		this.position = pPosition;
		this.size = pSize;
	}
	/**
	 * Create a rectangle using the top left coordinate and size
	 * @param pX {@link float} X location of top left.
	 * @param pY {@link float} Y location of top left
	 * @param pWidth {@link float} width of rectangle
	 * @param pHeight {@link float} height of rectangle
	 */
	public TMXRectangleF(final float pX, final float pY, 
			final float pWidth, final float pHeight){
		this.position[0] = pX;
		this.position[1] = pY;
		this.size[0] = pWidth;
		this.size[1] = pHeight;
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	/**
	 * Get the top left coordinates
	 * @return {@link float} {@link array} of coordinates 
	 */
	public float[] getTopLeft(){
		return this.position;
	}
	/**
	 * Get the top right coordinates
	 * @return {@link float} {@link array} of coordinates 
	 */
	public float[] getTopRight(){
		return new float[] { this.position[0] + size[0], this.position[1]};
	}
	/**
	 * Get the bottom right coordinates
	 * @return {@link float} {@link array} of coordinates 
	 */
	public float[] getBottomRight(){
		return new float[] { this.position[0] + size[0], this.position[1] + this.size[1]};
	}
	
	/**
	 * Get the bottom left coordinates
	 * @return {@link float} {@link array} of coordinates 
	 */
	public float[] getBottomLeft(){
		return new float[] { this.position[0], this.position[1] + this.size[1]};
	}
	// ===========================================================
	// Methods for/from SuperClass/interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
