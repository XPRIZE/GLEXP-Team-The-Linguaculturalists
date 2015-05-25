
package org.andengine.extension.tmx;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.util.constants.TMXConstants;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;
import org.andengine.util.exception.AndEngineRuntimeException;
import org.xml.sax.Attributes;

import android.R.integer;
import android.util.Log;

/**
 * Currently only Isometric tile objects is supported.
 * extends {@link TMXLayer} since its pretty similar stuff.
 * This process a given object group, which the user believes are tiles,
 * into a TMXLayer.  Be warned when operating on these layers, always check for
 * null values first.
 * @author Paul Robinson
 *
 */
public class TMXLayerObjectTiles extends TMXLayer {

	//TODO need to support opacity and visibile on object layers
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private final TMXObjectGroup mTMXObjectGroup;
	private final String TAG = "TMXObjectTileLayer";
	/**
	 * How many objects have been drawn?
	 */
	private int count = 0;

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * Create a new TMXLayerObjectTiles based on the {@link TMXLayer} class
	 * This will take in an {@link TMXObjectGroup} and produce a replica {@link TMXLayer}
	 * to be drawn.
	 * 
	 * @param pTMXTiledMap {@link TMXTiledMap}
	 * @param pAttributes For this just pass null as it is not used.
	 * @param pVertexBufferObjectManager {@link VertexBufferObjectManager}
	 */
	public TMXLayerObjectTiles(TMXTiledMap pTMXTiledMap, Attributes pAttributes,
			VertexBufferObjectManager pVertexBufferObjectManager, final TMXObjectGroup pTMXObjectGroup) {
		super(pTMXTiledMap, pVertexBufferObjectManager, pTMXObjectGroup, true);
		this.mTMXObjectGroup = pTMXObjectGroup;
		this.processTMXObject();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public TMXObjectGroup getTMXObjectGroup() {
		return mTMXObjectGroup;
	}
	
	/**
	 * Return how many items that have been produced that can be drawn.
	 * @return {@link integer} of items to be drawn. If zero then do not attach
	 */
	public int getCount(){
		return this.count;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	@Override
	void initializeTMXTileFromXML(Attributes pAttributes,
			ITMXTilePropertiesListener pTMXTilePropertyListener) {
		return; //Do nothing
	}

	@Override
	void initializeTMXTilesFromDataString(String pDataString,
			String pDataEncoding, String pDataCompression,
			ITMXTilePropertiesListener pTMXTilePropertyListener)
					throws IOException, IllegalArgumentException {
		return; //Do nothing
	}

	private void processTMXObject(){
		if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)){
			this.processIsometricTMXObject();
		}else if(this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)){
			Log.w(TAG, "Currently Orthographic is not supported for drawing tile objects");
		}else{
			Log.w(TAG, String.format("processTMXObject - Orientation not supported: '%s'. Will use normal Orthogonal draw method",
					this.mTMXTiledMap.getOrientation()));
		}
	}

	private void processIsometricTMXObject(){
		/*
		 * Implemented by - Paul Robinson
		 * Also in TMXLayer.java
		 * Referenced work - athanazio - "Working with Isometric Maps"
		 * http://www.athanazio.com/2008/02/21/working-with-isometric-maps/
		 * http://www.athanazio.com/wp-content/uploads/2008/02/isomapjava.txt
		 */ 
		final int tileHeight = this.mTMXTiledMap.getTileHeight();
		final int tileWidth = this.mTMXTiledMap.getTileWidth();
		final int maxColumnPixel = tileHeight * this.getTileColumns();
		final int maxRowPixel = tileHeight * this.getTileRows();
		ArrayList<TMXObject> objects = this.mTMXObjectGroup.getTMXObjects();
		for (TMXObject tmxObject : objects) {
			final int columnPixel = tmxObject.getX();
			final int rowPixel = tmxObject.getY();
			//Check if its pixel can be divided by tile height
			int columnRemainder = columnPixel % tileHeight;
			int rowRemainder = rowPixel % tileHeight;
			if(columnPixel <= 0 || rowPixel <= 0 || columnPixel > maxColumnPixel || rowPixel > maxRowPixel){
				//We've got a tile outside the map, we won't draw it so skip it.
				continue;
			}

			if(columnRemainder != 0 || rowRemainder != 0){
				/*
				 * Since there is a remainder that means we're not on a tile
				 * This means someone had held ctrl when placing the object tile.
				 * Drawing this tile is currently not supported.
				 */
				continue;
			}

			/*
			 * Work out what row and column this object should exist on.
			 * -1 is magic! This is because our row and column start at 0(Thanks java?!?)
			 * So taking away 1 accounts for starting at 0;
			 */
			final int column = (columnPixel / tileHeight) -1;
			final int row = (rowPixel / tileHeight) -1;
			final int width = this.mTMXTiledMap.getTileColumns();
			final ITextureRegion tmxTileTextureRegion;
			final int pGlobalTileID = tmxObject.getGID();
			//If -1 it means its not a tile object, just a normal object
			if(pGlobalTileID == 0){
				tmxTileTextureRegion = null;
			} else if ( pGlobalTileID == -1) {
				Log.w(TAG, "Global Tile ID is -1 this means it is not a tile object that can be draw");
				continue;
			} else if(tmxObject.getPolygonPoints() != null){
				Log.w(TAG, "This object has polygon points, cannot draw these");
				continue;
			}else if(tmxObject.getPolylinePoints() != null){
				Log.w(TAG, "This object has polyline points, cannot draw these");
				continue;
			}else{
				tmxTileTextureRegion = this.mTMXTiledMap.getTextureRegionFromGlobalTileID(pGlobalTileID);
			}

			if (tmxTileTextureRegion != null) {
				// Unless this is a transparent tile, setup the texture
				if (this.mTexture == null) {
					this.mTexture = tmxTileTextureRegion.getTexture();
					super.initBlendFunction(this.mTexture);
				} else {
					if (this.mTexture != tmxTileTextureRegion.getTexture()) {
						throw new AndEngineRuntimeException("All TMXTiles in a TMXObjetTileLayer ("+ this.getName() + ") need to be in the same TMXTileSet.");
					}
				}
			}
			/*
			 * Since objects layers aren't structured like normal layers,
			 * we have to work out the z index based on the tile row and column
			 */
			final int pZindex = (row * width) + column;
			TMXTile tmxTile = new TMXTile(this.mTMXTiledMap.getOrientation(), pGlobalTileID, pZindex, column, row, tileWidth, tileHeight, tmxTileTextureRegion);
			tmxTile.setTMXObject(tmxObject);

			//Get the offset for the tileset and the tileset size
			/*
			 * element[0] is the X offset.
			 * element[1] is the Y offset.
			 * element[2] is the tile width.
			 * element[3] is the tile height.
			 */
			int[] offset_tilesize = {0,0,tileWidth,tileHeight};
			offset_tilesize = this.mTMXTiledMap.checkTileSetOffsetAndSize(pGlobalTileID);
			
			float xRealIsoPos = (column * this.mIsoHalfTileWidth); 
			xRealIsoPos = xRealIsoPos - (row * this.mIsoHalfTileWidth);
			float yRealIsoPos = (column * this.mIsoHalfTileHeight);
			yRealIsoPos = yRealIsoPos + (row * this.mIsoHalfTileHeight);
			
			/*
			 * We need to apply the offset different here, in TMXLayer we wanted
			 * to drop the offset down, here we need to bring it up
			 * the X offset shouldn't really matter
			 */
			float xOffsetPos = xRealIsoPos - Math.abs(offset_tilesize[0]);
			float yOffsetPos = yRealIsoPos - offset_tilesize[1];
			
			tmxTile.setTileXIso(xOffsetPos);
			tmxTile.setTileYIso(yOffsetPos);
			/*
			 * Suppose we could skip this
			 * If you want to touch this object then click on the tile space it
			 * occupies 
			int xCentre = xRealIsoPos + this.mIsoHalfTileWidth;
			int yCentre = yRealIsoPos + this.mIsoHalfTileHeight;
			tmxTile.setTileXIsoCentre(xCentre);
			tmxTile.setTileYIsoCentre(yCentre);	
			 */
			this.mTMXTiles[row][column] = tmxTile;
			this.count++;
			if(pGlobalTileID != 0) {
				this.setIndex(this.getSpriteBatchIndex(column, row));
				//Before we were drawing to the map tile size, not the tileset size
				this.drawWithoutChecks(tmxTileTextureRegion, tmxTile.getTileX(), tmxTile.getTileY(), offset_tilesize[2], offset_tilesize[3], Color.WHITE_ABGR_PACKED_FLOAT);
				this.submit(); // TODO Doesn't need to be called here, but should rather be called in a "init" step, when parsing the XML is complete.
				// Notify the ITMXTilePropertiesListener if it exists. 
				/*
				Not supporting this, would require a slight change or passing null for TMXLayer argument.
				if(pTMXTilePropertyListener != null) {
					final TMXProperties<TMXTileProperty> tmxTileProperties = this.mTMXTiledMap.getTMXTileProperties(pGlobalTileID);
					if(tmxTileProperties != null) {
						pTMXTilePropertyListener.onTMXTileWithPropertiesCreated(this.mTMXTiledMap, this, tmxTile, tmxTileProperties);
						//Log.i(TAG, "tmxTileProperties created, size " + tmxTileProperties.size());
					}
				}
				 */
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
