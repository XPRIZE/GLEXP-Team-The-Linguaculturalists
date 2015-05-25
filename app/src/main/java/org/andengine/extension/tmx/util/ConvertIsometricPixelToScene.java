package org.andengine.extension.tmx.util;

import org.andengine.extension.tmx.TMXObject;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.shape.TMXPolygon;
import org.andengine.extension.tmx.shape.TMXRectangleF;
import org.andengine.extension.tmx.util.constants.TMXObjectType;
import android.R.array;
import android.R.integer;
import android.util.Log;

/**
 * This object can convert TMXObject pixels coordinates to scene coordinates.
 * Ideally you should use this class by accessing {@link #objectToScene(TMXObject)}
 * Although you can call other methods freely.
 * Currently this does not take into account an Isometric draw origin.
 * 
 * @author Paul Robinson
 *
 */
public class ConvertIsometricPixelToScene {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private final String TAG = "ConvertIsometricPixelToScene";
	private final TMXTiledMap mMap;
	private final int tileHeight;
	private final int tileHeightHalf;
	private final float tileHeightF;
	private final float tileHeightHalfF;
	// ===========================================================
	// Constructors
	// ===========================================================

	public ConvertIsometricPixelToScene(final TMXTiledMap pTMXTiledMap){
		this.mMap = pTMXTiledMap;
		this.tileHeight = this.mMap.getTileHeight();
		this.tileHeightHalf = (this.tileHeight /2);
		this.tileHeightF = this.tileHeight;
		this.tileHeightHalfF = this.tileHeightHalf;
	}

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
	 * Convert a given TMXObject into scene coordinates, suitable for drawing or
	 * perhaps using physics?
	 * <br> This can automatically handle TMXObject of different types, ie polygons 
	 * rectangles, polylines.
	 * @param pTMXObject {@link TMXObject} to convert
	 * @return 2D {@link Float} {@link array} of points.
	 * <br><i>Element [j][k]</i>
	 * <br><i>J is a single point</i>
	 * <br><i>K Element [0] = X</i>
	 * <br><i>K Element [1] = Y</i>
	 * <br><b>NOTE</b> this can return NULL if the {@link TMXObjectType} is not 
	 * supported.
	 */
	public float[][] objectToScene(final TMXObject pTMXObject){
		if(pTMXObject.getObjectType().equals(TMXObjectType.RECTANGLE)){
			return this.rectangleObjectToScene(pTMXObject.getX(), pTMXObject.getY(),
					pTMXObject.getWidth(), pTMXObject.getHeight());
		}else if(pTMXObject.getObjectType().equals(TMXObjectType.POLYGON)){
			return this.polyPointObjectToScene(pTMXObject.getX(), pTMXObject.getY(), pTMXObject.getPolygonPoints());
		}else if(pTMXObject.getObjectType().equals(TMXObjectType.POLYLINE)){
			return this.polyPointObjectToScene(pTMXObject.getX(), pTMXObject.getY(), pTMXObject.getPolylinePoints());
		}else{
			Log.w(TAG, String.format("The TMXObjectType: %s is not currently supported to scene", pTMXObject.getObjectType().toString()));
			return null;
		}
	}
	/**
	 * Convert a rectangle pixel coordinates so it can be placed on a scene.
	 * @param pPixelCoordinates {@link integer} {@link array} of position of top left 
	 * <br><i> Element [0] = X</i> 
	 * <br><i>Element [1] = Y</i>
	 * @param pSize {@link integer} {@link array} of Size 
	 * <br> <i> Element [0] = Width</i>
	 * <br> <i> Element [1] = Height</i>
	 * @return 2D {@link Float} {@link array} of points.
	 * <br><i>Element [j][k]</i>
	 * <br><i>J is a single point</i>
	 * <br><i>K Element [0] = X</i>
	 * <br><i>K Element [1] = Y</i>
	 */
	public float[][] rectangleObjectToScene(final int[] pPixelCoordinates, final int[] pSize){
		return this.rectangleObjectToScene(pPixelCoordinates[0], pPixelCoordinates[1], pSize[0], pSize[1]);
	}
	/**
	 * Convert a rectangle pixel coordinates so it can be placed on a scene.
	 * @param pX {@link integer} of top left X position
	 * @param pY {@link integer} of top left Y position
	 * @param pWidth {@link integer} width of rectangle
	 * @param pHeight {@link integer} height of rectangle
	 * @return 2D {@link Float} {@link array} of points.
	 * <br><i>Element [j][k]</i>
	 * <br><i>J is a single point</i>
	 * <br><i>K Element [0] = X</i>
	 * <br><i>K Element [1] = Y</i>
	 */
	public float[][] rectangleObjectToScene(final int pX, final int pY, final int pWidth, final int pHeight){
		float[] postion = this.pixelToTileCoordinate(new int[]{ pX, pY});
		float[] size = this.pixelToTileCoordinate(new int[] { pWidth, pHeight });
		TMXRectangleF rectFloat = new TMXRectangleF(postion, size);
		TMXPolygon polygon = new TMXPolygon();
		polygon.addPoint(this.tileToSceneCoordinate(rectFloat.getTopLeft()));
		polygon.addPoint(this.tileToSceneCoordinate(rectFloat.getTopRight()));
		polygon.addPoint(this.tileToSceneCoordinate(rectFloat.getBottomRight()));
		polygon.addPoint(this.tileToSceneCoordinate(rectFloat.getBottomLeft()));
		return polygon.getPoints();	
	}
	/**
	 * Convert a polygon/polyline and its points so it can be placed on a scene
	 * @param pX {@link Integer} X Origin.
	 * @param pY {@link integer} Y Origin
	 * @param pPoints 2D {@link integer} array of the points.
	 * <br><i>Element [j][k]</i>
	 * <br><i>J is a single point</i>
	 * <br><i>K Element [0] = X</i>
	 * <br><i>K Element [1] = Y</i>
	 * @return 2D {@link Float} {@link array} of points.
	 * <br><i>Element [j][k]</i>
	 * <br><i>J is a single point</i>
	 * <br><i>K Element [0] = X</i>
	 * <br><i>K Element [1] = Y</i>
	 */
	public float[][] polyPointObjectToScene(final int pX, final int pY, final int[][] pPoints){
		//Set up a new array to take correctly work out each point in pixels
		int[][] locations = new int[pPoints.length +1][2];
		locations[0][0] = pX;
		locations[0][1] = pY;
		int k = 1;
		//workout the pixel points, relative to the origin
		for(int i=0; i<pPoints.length;i++){
			int X = pX;
			int Y = pY;
			X += pPoints[i][0];
			Y += pPoints[i][1];
			locations[k][0] = X;
			locations[k][1] = Y;
			k++;
		}
		//Convert the pixels into tile coordinates
		float[][] tileLocations = new float[locations.length][2];
		for(int i=0; i<locations.length;i++){
			float[] temp = this.pixelToTileCoordinate(locations[i][0], locations[i][1]);
			tileLocations[i][0] = temp[0];
			tileLocations[i][1] = temp[1];
		}
		//Convert tile coordinates to scene coordinates
		TMXPolygon polygon = new TMXPolygon();
		for(int i=0; i<tileLocations.length;i++){
			polygon.addPoint(this.tileToSceneCoordinate(tileLocations[i][0], tileLocations[i][1]));
		}

		return polygon.getPoints();
	}

	/**
	 * Convert a pixel coordinate to a tile coordinate.
	 * <br>This is the first stage, the result of this should go to the second 
	 * stage at  {@link #tileToSceneCoordinate(float[])}
	 * <br>This divides the coordinate by the map tile height.
	 * <br><b>Example</b>
	 * <br> Map tile height = 32, X = 71, result = 2.218750 
	 * <br> Y = 6, result =  0.187500
	 * @param pX {@link integer} of pixel X location
	 * @param pY {@link integer} of pixel Y location
	 * @return {@link Float} {@link array} of pixel X and Y as tile coordinates
	 * <br><i>element [0] = X</i>
	 * <br><i>element [1] = Y</i>
	 * @see #pixelToTileCoordinate(int[])
	 */
	public float[] pixelToTileCoordinate(final int pX, final int pY){
		return this.pixelToTileCoordinate(new int[] {pX,pY});
	}

	/**
	 * Convert a pixel coordinate to a tile coordinate.
	 * <br>This is the first stage, the result of this should go to the second 
	 * stage at  {@link #tileToSceneCoordinate(float[])}
	 * <br>This divides the coordinate by the map tile height.
	 * <br><b>Example</b>
	 * <br> Map tile height = 32, X = 71, result = 2.218750 
	 * <br> Y = 6, result =  0.187500
	 * @param pPixelCoordinates {@link integer} {@link array} of coordinate
	 * <br><i>element [0] = X</i>
	 * <br><i>element [1] = Y</i>
	 * @return {@link Float} {@link array} of pixel X and Y as tile coordinates
	 * <br><i>element [0] = X</i>
	 * <br><i>element [1] = Y</i>
	 * @see #pixelToTileCoordinate(int, int)
	 */
	public float[] pixelToTileCoordinate(final int[] pPixelCoordinates){	
		return new float[] {pPixelCoordinates[0] / this.tileHeightF, 
				pPixelCoordinates[1] / this.tileHeightF };
	}

	/**
	 * Convert a tile coordinate to a scene position.
	 * <br>This is the second stage, the arguments should have first gone through
	 * the first stage {@link #pixelToTileCoordinate(int[])}
	 * <br>This takes care of handling columns, by adding an extra tile height.
	 * <br><b>NOTE</b> do not alter the coordinates between stage one and two!
	 * <br><b>Example</b>
	 * <br>X =  2.218750, result = 97
	 * <br>Y = 0.187500, result = 38.5
	 * @param pX {@link Float} of X after its been through {@link #pixelToTileCoordinate(int, int)}
	 * @param pY {@link Float} of Y after its been through {@link #pixelToTileCoordinate(int, int)}
	 * @return {@link Float} array of transformed coordinates.
	 * <br><i>element [0] = X</i>
	 * <br><i>element [1] = Y</i>
	 * @see #tileToSceneCoordinate(float[])
	 */
	public float[] tileToSceneCoordinate(final float pX, final float pY){
		return this.tileToSceneCoordinate(new float[]{pX,pY});
	}

	/**
	 * Convert a tile coordinate to a scene position.
	 * <br>This is the second stage, the arguments should have first gone through
	 * the first stage {@link #pixelToTileCoordinate(int[])}
	 * <br>This takes care of handling columns, by adding an extra tile height.
	 * <br><b>NOTE</b> do not alter the coordinates between stage one and two!
	 * <br><b>Example</b>
	 * <br>X =  2.218750, result = 97
	 * <br>Y = 0.187500, result = 38.5
	 * @param pOdd {@link Float} {@link array} of X and Y afters it been through {@link #pixelToTileCoordinate(int, int)}
	 * <br><i>element [0] = X</i>
	 * <br><i>element [1] = Y</i>
	 * @return {@link Float} array of transformed coordinates.
	 * <br><i>element [0] = X</i>
	 * <br><i>element [1] = Y</i>
	 * @see #tileToSceneCoordinate(float, float)
	 */
	public float[] tileToSceneCoordinate(final float[] pOdd){
		float X = (pOdd[0] - pOdd[1]) * this.tileHeightF;
		float Y = (pOdd[0] + pOdd[1]) * this.tileHeightHalfF;	
		X += this.tileHeightF;
		return new float[] { X, Y };
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
