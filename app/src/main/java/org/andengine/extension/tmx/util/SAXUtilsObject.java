package org.andengine.extension.tmx.util;

import org.andengine.util.SAXUtils;
import org.xml.sax.Attributes;

import android.R.array;
import android.R.integer;
import android.util.Log;

/**
 * A class with a single method to parse the points attribute of an object.
 * Ideally this would go into SAXUtils.java in the engine, but to fork 
 * the main repo just for one method is not worth it.
 * @author Paul Robinson
 *
 */
public final class SAXUtilsObject {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	
	/**
	 * Get a set of integer points from a given XML line.
	 * <br>
	 * This first splits up an attribute properties, such as <i>0,0 0,-32 32,-32</i>
	 * on its white space, into a {@link String} {@link array}.  It then splits again, 
	 * based on the comma between the numbers, into another {@link String} {@link array}
	 * Then each element is then parsed as an {@link integer} and placed into
	 * a 2D {@link integer} {@link array};
	 * <br><b>Negative numbers are kept negative</b>
	 * <br>
	 * There is the possibility of a NullPointerException on splitting strings,
	 * and NumberFormatException on parsing to integers.  These are handled so 
	 * <b><code>NULL</code></b> is returned instead.
	 * 
	 * @param pAttributes {@link Attributes}
	 * @param pAttributeName {@link String} of attribute to get.
	 * @return 2 Dimensional  {@link integer} array of points or <code>NULL</code>
	 * if an exception occurs.
	 */
	public static final int[][] getIntPoints(final Attributes pAttributes, final String pAttributeName){
		final String pointsLine = SAXUtils.getAttribute(pAttributes, pAttributeName, "null");
		int[][] points = null;
		try{
			final String[] uncleanPoints = pointsLine.split("\\s+");
			points = new int[uncleanPoints.length][2];
			for(int i = 0; i < uncleanPoints.length; i++){
				final String[] strTemp = uncleanPoints[i].split(",");
				points[i][0] = Integer.parseInt(strTemp[0]);
				points[i][1] = Integer.parseInt(strTemp[1]);
			}
		}catch (NullPointerException npe){
			Log.e("TMXObject", "Could not split up polygon XML line to get points");
			return null;
		}catch (NumberFormatException nfe){
			Log.e("TMXObject", "Could not get integer point from polygon XML");
			return null;
		}
		return points;
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
