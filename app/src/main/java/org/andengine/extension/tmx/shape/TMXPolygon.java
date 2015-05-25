package org.andengine.extension.tmx.shape;

import java.util.ArrayList;
import android.R.array;

/**
 * A class to store polygon points as floats.
 * @author Paul Robinson
 *
 */
public class TMXPolygon {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private ArrayList<float[]> mPoints;
	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * Create an empty polygon
	 */
	public TMXPolygon(){
		this.mPoints = new ArrayList<float[]>();
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================
	/**
	 * Add a new point.
	 * @param pX {@link Float} of X coordinate 
	 * @param pY {@link Float} of Y coordinate 
	 */
	public void addPoint(final float pX, final float pY){
		this.mPoints.add(new float[] {pX, pY});
	}
	
	/**
	 * Add a new point 
	 * @param pPosition {@link Float} {@link array} of X and Y coordinate
	 * <br><i>Element [0] = X</i>
	 * <br><i>Element [1] = Y</i>
	 */
	public void addPoint(final float[] pPosition){
		this.mPoints.add(pPosition);
	}
	
	/**
	 * Get points as 2D {@link Float} {@link array}
	 * @return {@link Float} 2D array of points 
	 * <br><i>Element [j][k]</i>
	 * <br><i>J is a single point</i>
	 * <br><i>K Element [0] = X</i>
	 * <br><i>K Element [1] = Y</i>
	 */
	public float[][] getPoints(){
		float[][] asArray = new float[this.mPoints.size()][2];
		for(int i=0; i < this.mPoints.size(); i++){
			float[] tempPofloat = this.mPoints.get(i);
			asArray[i][0] = tempPofloat[0];
			asArray[i][1] = tempPofloat[1];
		}
		return asArray;
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
