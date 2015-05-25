package org.andengine.extension.tmx.shape;

import android.R.array;
import android.R.integer;

/**
 * A rectangle class to store pixel coordinates(as {@link integer} values) of a
 * TMX rectangle object. Beware this should not be used for transformed coordinates.
 * @author Paul Robinson
 *
 */
public class TMXRectangle {
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
	private int[] position = {0,0};
	private int x = 0;
	private int y = 0;
	/**
	 * [0] = width <br>
	 * [1] = height
	 */
	private int[] size = {0,0};
	private int height = 0;
	private int width = 0;
	// ===========================================================
	// Constructors
	// ===========================================================
	
	/**
	 * Create a rectangle using the top left coordinate and size
	 * @param pPostion {@link integer} {@link array} of position of top left 
	 * <br><i> Element [0] = X</i> 
	 * <br><i>Element [1] = Y</i>
	 * @param pSize {@link integer} {@link array} of Size 
	 * <br> <i> Element [0] = Width</i>
	 * <br> <i> Element [1] = Height</i>
	 */
	public TMXRectangle(final int[] pPosition, final int[] pSize){
		this.position = pPosition;
		this.size = pSize;
		this.x = this.position[0];
		this.y = this.position[1];
		this.width = this.size[0];
		this.height = this.size[1];
	}
	/**
	 * Create a rectangle using the top left coordinate and size
	 * @param pX {@link integer} X location of top left.
	 * @param pY {@link integer} Y location of top left
	 * @param pWidth {@link integer} width of rectangle
	 * @param pHeight {@link integer} height of rectangle
	 */
	public TMXRectangle(final int pX, final int pY, final int pWidth, final int pHeight){
		this.position[0] = pX;
		this.position[1] = pY;
		this.x = pX;
		this.y = pY;
		this.size[0] = pWidth;
		this.size[1] = pHeight;
		this.height = pHeight;
		this.width = pWidth;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	/**
	 * Get the top left coordinates
	 * @return {@link integer} {@link array} of coordinates 
	 */
	public int[] getTopLeft(){
		return this.position;
	}
	/**
	 * Get the top right coordinates
	 * @return {@link integer} {@link array} of coordinates 
	 */
	public int[] getTopRight(){
		return new int[] { this.x + this.width, this.y};
	}
	/**
	 * Get the bottom right coordinates
	 * @return {@link integer} {@link array} of coordinates 
	 */
	public int[] getBottomRight(){
		return new int[] { this.x + this.width, this.y + this.height};
	}
	
	/**
	 * Get the bottom left coordinates
	 * @return {@link integer} {@link array} of coordinates 
	 */
	public int[] getBottomLeft(){
		return new int[] { this.x, this.y + this.height};
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
