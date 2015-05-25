/**
 * 
 */
package org.andengine.extension.tmx.util.constants;

/**
 * Constants for Isometric maps
 * @author Paul Robinson
 */
public interface TMXIsometricConstants {

	// ===========================================================
	// Drawing Options
	// For use to help determine the draw method (for isometric tiles)
	// ===========================================================
	/**
	 * No culling takes place, draws every single tile on the map.
	 * <br>Alternatives
	 * <br> {@link #DRAW_METHOD_ISOMETRIC_CULLING_SLIM}
	 * <br> {@link #DRAW_METHOD_ISOMETRIC_CULLING_PADDING}
	 */
	public static final int DRAW_METHOD_ISOMETRIC_ALL = 1;
	/**
	 * Culling does take place, but only whole tiles are visible on the screen.
	 * <br>This is inefficient, as it loops through every single tile and checks
	 * that its centre point is within the space of the camera.
	 * <br>
	 * If you also want to draw a tile that is partly on the screen, use
	 * {@link #DRAW_METHOD_ISOMETRIC_CULLING_PADDING}
	 */
	public static final int DRAW_METHOD_ISOMETRIC_CULLING_SLIM = 2;
	/**
	 * Culling does take place, tiles which are partly on the screen are also drawn.
	 * <br>This is inefficient, as it loops through every single tile and checks
	 * that its centre point is within the space of the camera.
	 * <br>
	 * If you also want to draw complete tiles on the screen, use
	 * {@link #DRAW_METHOD_ISOMETRIC_CULLING_SLIM}
	 */
	public static final int DRAW_METHOD_ISOMETRIC_CULLING_PADDING = 3;
	/**
	 * Culling method taken from method paintLayer in IsoMapView.java 
	 * from Tiled 0.7.2 source code.  
	 * <br>Not to dissimilar to the function drawTileLayer in isometricrenderer.cpp 
	 * from Tiled 0.8.0 source code.  
	 * <br>Slight performance gain and draws tiles in a different order than the others,
	 * this does not appear to cause any problems.  The tiles original Z order
	 * are unaffected.
	 * <br>
	 * Copyright 2009-2011, Thorbjorn Lindeijer <thorbjorn@lindeijer.nl>
	 * <br><a href="http://sourceforge.net/projects/tiled/files/Tiled/0.7.2/tiled-0.7.2-src.zip/">Tiled 0.7.2 source code zip</a>
	 * <br><a href="https://github.com/bjorn/tiled/blob/master/src/libtiled/isometricrenderer.cpp">Tiled 0.8.0 source code - isometricrenderer.cpp on Github</a>
	 */
	public static final int DRAW_METHOD_ISOMETRIC_CULLING_TILED_SOURCE = 4;
}
