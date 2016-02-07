package org.andengine.extension.tmx;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.batch.SpriteBatch;
import org.andengine.entity.sprite.batch.vbo.HighPerformanceSpriteBatchVertexBufferObject;
import org.andengine.entity.sprite.batch.vbo.ISpriteBatchVertexBufferObject;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.util.constants.TMXConstants;
import org.andengine.extension.tmx.util.constants.TMXIsometricConstants;
import org.andengine.opengl.shader.PositionColorTextureCoordinatesShaderProgram;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.SAXUtils;
import org.andengine.util.StreamUtils;
import org.andengine.util.adt.color.Color;
import org.andengine.util.base64.Base64;
import org.andengine.util.base64.Base64InputStream;
import org.andengine.util.exception.AndEngineRuntimeException;
import org.andengine.util.exception.MethodNotSupportedException;
import org.andengine.util.math.MathUtils;
import org.xml.sax.Attributes;

import android.R.integer;
import android.opengl.GLES20;
import android.util.Log;

/**
 * Since this is Anchor Centre branch, the coordinates are from 
 * the bottom left increasing up, where as before it was top left increasing down
 * This mucks up the original calculations. So to fix this problem, 
 * if the Y is negative make it positive, if positive make negative.
 * 
 * (c) 2010 Nicolas Gramlich (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 20:27:31 - 20.07.2010
 */
public class TMXLayer extends SpriteBatch implements TMXConstants {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final TMXTiledMap mTMXTiledMap;
	private final String TAG = "TMXLayer";
	private final String mName;
	private final int mTileColumns;
	private final int mTileRows;
	/**
	 * Row, then Columns
	 */
	protected final TMXTile[][] mTMXTiles;
	/**
	 * The global tile ID for each tile. <br>
	 * Declared as new int[{@link #mTileRows} ][{@link #mTileColumns}]
	 */
	protected int[][] mTileGID;

	private int mTilesAdded;
	private final int mGlobalTileIDsExpected;

	private final float[] mCullingVertices = new float[2 * Sprite.VERTICES_PER_SPRITE];

	private final TMXProperties<TMXLayerProperty> mTMXLayerProperties = new TMXProperties<TMXLayerProperty>();

	private final int mWidth;
	private final int mHeight;

	private double tileratio = 0;
	/**
	 * Half the width of the isometric tile
	 */
	protected int mIsoHalfTileWidth = 0;
	/**
	 * Half the height of the isometric tile
	 */
	protected int mIsoHalfTileHeight = 0;
	/**
	 * Count how many tiles on the row axis has been added.
	 */
	private int mAddedTilesOnRow = 0;
	/**
	 * Count how many tile on the columns axis has been added.
	 */
	private int mAddedRows = 0;
	/**
	 * What draw method to use for Isometric layers.<br>
	 * Default draw method is defined in the TMXTiledMap
	 * {@link TMXTiledMap#getIsometricDrawMethod()}
	 */
	private int DRAW_METHOD_ISOMETRIC = TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_ALL;
	/**
	 * Are we allocating TMXTiles when creating a layer
	 */
	private boolean mAllocateTMXTiles = false;
	private boolean mStoreGID = false;

	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * This reads in the attributes and creates a standard {@link SpriteBatch}
	 * which uses a {@link HighPerformanceSpriteBatchVertexBufferObject}
	 * 
	 * @param pTMXTiledMap
	 * @param pAttributes
	 * @param pVertexBufferObjectManager
	 * @param pAllocateTiles
	 */
	public TMXLayer(final TMXTiledMap pTMXTiledMap, final Attributes pAttributes,
			final VertexBufferObjectManager pVertexBufferObjectManager, boolean pAllocateTiles) {
		super(null, SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_WIDTH)
				* SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_HEIGHT),
				pVertexBufferObjectManager);

		this.mTMXTiledMap = pTMXTiledMap;
		this.mName = pAttributes.getValue("", TMXConstants.TAG_LAYER_ATTRIBUTE_NAME);
		this.mTileColumns = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_WIDTH);
		this.mTileRows = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_HEIGHT);
		if (pAllocateTiles) {
			this.mAllocateTMXTiles = true;
			this.mTMXTiles = new TMXTile[this.mTileRows][this.mTileColumns];
		} else {
			this.mAllocateTMXTiles = false;
			this.mTMXTiles = null;
		}
		this.mTileGID = new int[this.mTileRows][this.mTileColumns];
		this.mStoreGID = this.mTMXTiledMap.getStoreGID();

		this.mWidth = pTMXTiledMap.getTileWidth() * this.mTileColumns;
		this.mHeight = pTMXTiledMap.getTileHeight() * this.mTileRows;

		this.mRotationCenterX = this.mWidth * 0.5f;
		this.mRotationCenterY = this.mHeight * 0.5f;

		this.mScaleCenterX = this.mRotationCenterX;
		this.mScaleCenterY = this.mRotationCenterY;

		this.mGlobalTileIDsExpected = this.mTileColumns * this.mTileRows;

		this.setVisible(SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_VISIBLE,
				TMXConstants.TAG_LAYER_ATTRIBUTE_VISIBLE_VALUE_DEFAULT) == 1);
		this.setAlpha(SAXUtils.getFloatAttribute(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_OPACITY,
				TMXConstants.TAG_LAYER_ATTRIBUTE_OPACITY_VALUE_DEFAULT));

		if (this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)) {
			// Paul Robinson
			// Calculate the half of the tile height and width, saves doing it
			// later
			this.mIsoHalfTileHeight = this.mTMXTiledMap.getTileHeight() / 2;
			this.mIsoHalfTileWidth = this.mTMXTiledMap.getTileWidth() / 2;
			this.tileratio = this.mTMXTiledMap.getTileWidth() / this.mTMXTiledMap.getTileHeight();
			this.setIsometricDrawMethod(this.mTMXTiledMap.getIsometricDrawMethod());
		}
	}

	/**
	 * Create a new TMXLayer with a custom
	 * {@link ISpriteBatchVertexBufferObject}
	 * 
	 * @param pTMXTiledMap
	 * @param pAttributes
	 * @param pCapacity
	 * @param pSpriteBatchVertexBufferObject
	 * @param pVertexBufferObjectManager
	 * @param pAllocateTiles
	 */
	public TMXLayer(final TMXTiledMap pTMXTiledMap, final Attributes pAttributes, final int pCapacity,
			final ISpriteBatchVertexBufferObject pSpriteBatchVertexBufferObject,
			final VertexBufferObjectManager pVertexBufferObjectManager, boolean pAllocateTiles) {
		super(0, 0, null, pCapacity, pSpriteBatchVertexBufferObject, PositionColorTextureCoordinatesShaderProgram
				.getInstance());

		this.mTMXTiledMap = pTMXTiledMap;
		this.mName = pAttributes.getValue("", TMXConstants.TAG_LAYER_ATTRIBUTE_NAME);
		this.mTileColumns = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_WIDTH);
		this.mTileRows = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_HEIGHT);
		if (pAllocateTiles) {
			this.mAllocateTMXTiles = true;
			this.mTMXTiles = new TMXTile[this.mTileRows][this.mTileColumns];
		} else {
			this.mAllocateTMXTiles = false;
			this.mTMXTiles = null;
		}
		this.mTileGID = new int[this.mTileRows][this.mTileColumns];
		this.mStoreGID = this.mTMXTiledMap.getStoreGID();

		this.mWidth = pTMXTiledMap.getTileWidth() * this.mTileColumns;
		this.mHeight = pTMXTiledMap.getTileHeight() * this.mTileRows;

		this.mRotationCenterX = this.mWidth * 0.5f;
		this.mRotationCenterY = this.mHeight * 0.5f;

		this.mScaleCenterX = this.mRotationCenterX;
		this.mScaleCenterY = this.mRotationCenterY;

		this.mGlobalTileIDsExpected = this.mTileColumns * this.mTileRows;

		this.setVisible(SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_VISIBLE,
				TMXConstants.TAG_LAYER_ATTRIBUTE_VISIBLE_VALUE_DEFAULT) == 1);
		this.setAlpha(SAXUtils.getFloatAttribute(pAttributes, TMXConstants.TAG_LAYER_ATTRIBUTE_OPACITY,
				TMXConstants.TAG_LAYER_ATTRIBUTE_OPACITY_VALUE_DEFAULT));

		if (this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)) {
			// Paul Robinson
			// Calculate the half of the tile height and width, saves doing it
			// later
			this.mIsoHalfTileHeight = this.mTMXTiledMap.getTileHeight() / 2;
			this.mIsoHalfTileWidth = this.mTMXTiledMap.getTileWidth() / 2;
			this.tileratio = this.mTMXTiledMap.getTileWidth() / this.mTMXTiledMap.getTileHeight();
			this.setIsometricDrawMethod(this.mTMXTiledMap.getIsometricDrawMethod());
		}
	}

	/**
	 * TMXLayer which uses a
	 * {@link HighPerformanceSpriteBatchVertexBufferObject}
	 * 
	 * @param pTMXTiledMap
	 * @param pVertexBufferObjectManager
	 * @param pTMXObjectGroup
	 * @param pAllocateTiles
	 */
	public TMXLayer(final TMXTiledMap pTMXTiledMap, final VertexBufferObjectManager pVertexBufferObjectManager,
			final TMXObjectGroup pTMXObjectGroup, boolean pAllocateTiles) {
		super(null, pTMXTiledMap.getTileWidth() * pTMXTiledMap.getTileHeight(), pVertexBufferObjectManager);

		this.mTMXTiledMap = pTMXTiledMap;
		this.mName = pTMXObjectGroup.getName();
		this.mTileColumns = pTMXTiledMap.getTileColumns();
		this.mTileRows = pTMXTiledMap.getTileRows();
		if (pAllocateTiles) {
			this.mAllocateTMXTiles = true;
			this.mTMXTiles = new TMXTile[this.mTileRows][this.mTileColumns];
		} else {
			this.mAllocateTMXTiles = false;
			this.mTMXTiles = null;
		}
		this.mTileGID = new int[this.mTileRows][this.mTileColumns];
		this.mStoreGID = this.mTMXTiledMap.getStoreGID();

		this.mWidth = pTMXTiledMap.getTileWidth() * this.mTileColumns;
		this.mHeight = pTMXTiledMap.getTileHeight() * this.mTileRows;

		this.mRotationCenterX = this.mWidth * 0.5f;
		this.mRotationCenterY = this.mHeight * 0.5f;

		this.mScaleCenterX = this.mRotationCenterX;
		this.mScaleCenterY = this.mRotationCenterY;

		this.mGlobalTileIDsExpected = this.mTileColumns * this.mTileRows;

		this.setVisible(true);
		this.setAlpha(1.0f);

		if (this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)) {
			// Paul Robinson
			// Calculate the half of the tile height and width, saves doing it
			// later
			this.mIsoHalfTileHeight = this.mTMXTiledMap.getTileHeight() / 2;
			this.mIsoHalfTileWidth = this.mTMXTiledMap.getTileWidth() / 2;
			this.tileratio = this.mTMXTiledMap.getTileWidth() / this.mTMXTiledMap.getTileHeight();
			this.setIsometricDrawMethod(this.mTMXTiledMap.getIsometricDrawMethod());
		}
	}

	/**
	 * Copy constructor, some areas may or may not be able to copy textures. <br>
	 * 
	 * @param pTMXTiledMap
	 *            {@link TMXTiledMap} parent of this {@link TMXLayer} copy.
	 * @param pTMXLayer
	 *            {@link TMXLayer} to copy.
	 * @param pVertexBufferObjectManager
	 *            {@link VertexBufferObjectManager}, should be able to pass null
	 *            if not required to attach to a scene.
	 * @param pDeepCopyTextures
	 *            {@link Boolean} Should we attempted to copy textures?
	 * @throws Exception
	 *             IF the passed TMXLayer is null.
	 */
	public TMXLayer(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer,
			final VertexBufferObjectManager pVertexBufferObjectManager, final boolean pDeepCopyTextures)
			throws Exception {
		super(null, pTMXTiledMap.getTileWidth() * pTMXTiledMap.getTileHeight(), pVertexBufferObjectManager);

		if (pTMXLayer == null) {
			throw new Exception("The pased TMXLayer to copy is null");
		}

		this.mTMXTiledMap = pTMXTiledMap;
		this.mName = new String(pTMXLayer.getName());
		this.mTileColumns = pTMXLayer.getTileColumns();
		this.mTileRows = pTMXLayer.getTileRows();

		if (pTMXLayer.getAllocateTiles()) {
			this.mTMXTiles = new TMXTile[this.mTileRows][this.mTileColumns];
			for (int i = 0; i < this.mTileRows; i++) {
				for (int j = 0; j < this.mTileColumns; j++) {
					this.mTMXTiles[i][j] = new TMXTile(pTMXLayer.getTMXTile(i, j), pDeepCopyTextures);
				}
			}
		}else{
			this.mTMXTiles = null;
		}
		this.mTileGID = pTMXLayer.getTMXTileGlobalIDs();
		this.mTilesAdded = pTMXLayer.getTMXTilesAdded();
		this.mGlobalTileIDsExpected = pTMXLayer.getGlobalTileIDsExpected();
		TMXProperties<TMXLayerProperty> toCopyTMXLayerProperties = pTMXLayer.getTMXLayerProperties();
		for (TMXLayerProperty toCopyTMXLayerProperty : toCopyTMXLayerProperties) {
			this.mTMXLayerProperties.add(new TMXLayerProperty(toCopyTMXLayerProperty));
		}
		this.mWidth = pTMXTiledMap.getTileWidth() * this.mTileColumns;
		this.mHeight = pTMXTiledMap.getTileHeight() * this.mTileRows;
		this.tileratio = pTMXLayer.getTileRatio();
		this.mIsoHalfTileHeight = pTMXLayer.getIsometricHalfTileHeight();
		this.mIsoHalfTileWidth = pTMXLayer.getIsometricHalfTileWidth();
		this.mAddedTilesOnRow = pTMXLayer.getAddedTilesOnRow();
		this.mAddedRows = pTMXLayer.getAddedRows();
		this.DRAW_METHOD_ISOMETRIC = pTMXLayer.getIsometricDrawMethod();
		this.mAllocateTMXTiles = pTMXLayer.getAllocateTiles();
		this.mStoreGID = pTMXLayer.getStoreGID();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public String getName() {
		return this.mName;
	}

	public float getWidth() {
		return this.mWidth;
	}

	public float getHeight() {
		return this.mHeight;
	}

	public int getTileColumns() {
		return this.mTileColumns;
	}

	public int getTileRows() {
		return this.mTileRows;
	}

	public TMXTile[][] getTMXTiles() {
		return this.mTMXTiles;
	}

	/**
	 * Get the {@link TMXTile} at a given location based on a row and column
	 * number.
	 * 
	 * @param pTileColumn
	 *            {@link integer} of column location
	 * @param pTileRow
	 *            {@link integer} of row location
	 * 
	 * @return {@link TMXTile} tile at given location <b>OR</b>
	 *         <code>NULL</code> if no such tile exists (such as when the layer
	 *         is {@link TMXLayerObjectTiles})<br>
	 *         <code>null</code> may be returned if {@link #getAllocateTiles()}
	 *         returns false.
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public TMXTile getTMXTile(final int pTileColumn, final int pTileRow) throws ArrayIndexOutOfBoundsException {
		if (this.mAllocateTMXTiles) {
			return this.mTMXTiles[pTileRow][pTileColumn];
		} else {
			return null;
		}
	}

	/**
	 * Same as the standard getTMXTile method but returns null if out of bounds. <br>
	 * The original method just threw and array index out of bounds exception,
	 * this first checks if the desired row and column are within range, if not
	 * then null is returned instead.
	 * 
	 * @param pTileColumn
	 *            {@link integer} of column location
	 * @param pTileRow
	 *            {@link integer} of row location
	 * @return {@link TMXTile} if tile is within bounds <b>OR</b>
	 *         <code>NULL</code> if out of bounds. <br>
	 *         <code>null</code> may be returned if {@link #getAllocateTiles()}
	 *         returns false.
	 * @see #getTMXTile(int, int) for a bit more info.
	 */
	public TMXTile getTMXTileCanReturnNull(final int pTileColumn, final int pTileRow) {
		if (this.mAllocateTMXTiles) {
			if (pTileColumn >= 0 && pTileColumn < this.mTileColumns && pTileRow >= 0 && pTileRow < this.mTileRows) {
				return this.mTMXTiles[pTileRow][pTileColumn];
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * For this layer set the desired render method as defined in
	 * {@link TMXIsometricConstants} <br>
	 * <b>Available draw methods:</b> <br>
	 * {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_ALL} <br>
	 * {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_CULLING_SLIM} <br>
	 * {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_CULLING_PADDING} <br>
	 * {@link TMXIsometricConstants#DRAW_METHOD_ISOMETRIC_CULLING_TILED_SOURCE}
	 * 
	 * @param pMethod
	 *            {@link integer} of the method to use.
	 */
	public void setIsometricDrawMethod(final int pMethod) {
		this.DRAW_METHOD_ISOMETRIC = pMethod;
	}

	/**
	 * Get a TMXTile at a given location. <br>
	 * This takes into account the map orientation. This currently supports
	 * <b>ORTHOGONAL</b> and <b>ISOMETRIC</b>, Check the Javadoc for each
	 * related method if there are any instructions. <br>
	 * <b>Note</b> If the map orientation is not supported, an error is logged
	 * and the normal orthogonal calculations used. <br>
	 * <b>Call: </b> <br>
	 * {@link #getTMXTileAtOrthogonal(float, float)} <br>
	 * {@link #getTMXTileAtIsometric(float, float)} <br>
	 * <b>Isometric Note:</b> You can also call
	 * {@link #getTMXTileAtIsometricAlternative(float[])} if you feel the
	 * standard implementation isn't working correctly.
	 * 
	 * @param pX
	 *            {@link Float} x touch location.
	 * @param pY
	 *            {@link Float} y touch location.
	 * @return {@link TMXTile} of found location <b>OR</b> returns
	 *         <code>null</code> if not found, or the location is out side the
	 *         bounds of the tmx file. <br>
	 *         <code>null</code> may be returned if {@link #getAllocateTiles()}
	 *         returns false.
	 */
	public TMXTile getTMXTileAt(final float pX, final float pY) {
		if (this.mAllocateTMXTiles) {
			// Modification by Paul Robinson
			if (this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)) {
				return this.getTMXTileAtOrthogonal(pX, pY);
			} else if (this.mTMXTiledMap.getOrientation().equals(
					TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)) {
				return this.getTMXTileAtIsometric(pX, pY);
			} else {
				Log.w(TAG, String.format("Orientation not supported: '%s'. "
						+ "Will use normal Orthogonal getTMXTileAt method", this.mTMXTiledMap.getOrientation()));
				return this.getTMXTileAtOrthogonal(pX, pY);
			}
		} else {
			return null;
		}
	}

	/**
	 * Standard method to calculating the selected tile at a given location, on
	 * a Orthogonal map<br>
	 * <b>Note</b> The contents of this method was originally in
	 * {@link #getTMXTileAt(float, float)}
	 * 
	 * @param pX
	 *            {@link Float} x touch location.
	 * @param pY
	 *            {@link Float} y touch location.
	 * @return {@link TMXTile} of found location <b>OR</b> returns
	 *         <code>null</code> if not found, or the location is out side the
	 *         bounds of the tmx file. <br>
	 *         <code>null</code> may be returned if {@link #getAllocateTiles()}
	 *         returns false.
	 */
	private TMXTile getTMXTileAtOrthogonal(final float pX, final float pY) {
		if (this.mAllocateTMXTiles) {
			final float[] localCoords = this.convertSceneCoordinatesToLocalCoordinates(pX, pY);
			final TMXTiledMap tmxTiledMap = this.mTMXTiledMap;

			final int tileColumn = (int) (localCoords[SpriteBatch.VERTEX_INDEX_X] / tmxTiledMap.getTileWidth());
			if (tileColumn < 0 || tileColumn > this.mTileColumns - 1) {
				return null;
			}
			final int tileRow = (int) (localCoords[SpriteBatch.VERTEX_INDEX_Y] / tmxTiledMap.getTileWidth());
			if (tileRow < 0 || tileRow > this.mTileRows - 1) {
				return null;
			}

			return this.mTMXTiles[tileRow][tileColumn];
		} else {
			return null;
		}
	}

	/**
	 * Standard method to calculating the selected tile at a given location on
	 * an isometric map <br>
	 * <b>Usage</b> <br>
	 * From the touch event, execute <code>convertLocalToSceneCoordinates</code>
	 * on the current scene and pass to this method the returned {@link Float}
	 * array. <br>
	 * <b>Accessed via </b> The only way to access this method is through
	 * {@link #getTMXTileAt(float, float)}. <br>
	 * <b>Why</b> <br>
	 * This is method is a mix of {@link #getTMXTileAtOrthogonal(float, float)}
	 * and {@link #getTMXTileAtIsometricAlternative(float[])}. This method calls
	 * <code>convertSceneToLocalCoordinates</code>
	 * 
	 * @param pX
	 *            {@link Float} x touch location.
	 * @param pY
	 *            {@link Float} y touch location.
	 * @return {@link TMXTile} of found location <b>OR</b> returns
	 *         <code>null</code> if not found, or the location is out side the
	 *         bounds of the tmx file. <br>
	 *         <code>null</code> may be returned if {@link #getAllocateTiles()}
	 *         returns false.
	 */
	private TMXTile getTMXTileAtIsometric(final float pX, final float pY) {
		/*
		 * Implemented by Paul Robinson
		 * Referenced work Christian Knudsen of Laserbrain Studios - "The basics of isometric programming" 
		 * http://laserbrainstudios.com/2010/08/the-basics-of-isometric-programming/
		 */
		if (this.mAllocateTMXTiles) {
			float[] localCoords = this.convertSceneCoordinatesToLocalCoordinates(pX, pY);
			final TMXTiledMap tmxTiledMap = this.mTMXTiledMap;

			float screenX = localCoords[SpriteBatch.VERTEX_INDEX_X] - this.mTMXTiledMap.getTileHeight();
			float screenY = localCoords[SpriteBatch.VERTEX_INDEX_Y] < 0 ? Math.abs(pY) : 0 - localCoords[SpriteBatch.VERTEX_INDEX_Y];
			float tileColumn = (screenY / tmxTiledMap.getTileHeight())
					+ (screenX / tmxTiledMap.getTileWidth());
			float tileRow = (screenY / tmxTiledMap.getTileHeight())
					- (screenX / tmxTiledMap.getTileWidth());
			if (tileColumn < 0 || tileColumn > this.mTileColumns) {
				return null;
			}
			if (tileRow < 0 || tileRow > this.mTileRows) {
				return null;
			}
			return this.mTMXTiles[(int) tileRow][(int) tileColumn];
		} else {
			return null;
		}
	}

	/**
	 * Alternative method to calculating the selected tile at a given location
	 * on an isometric map <br>
	 * <br>
	 * <b>Usage</b> <br>
	 * From the touch event, execute <code>convertLocalToSceneCoordinates</code>
	 * on the current scene and pass to this method the returned {@link Float}
	 * array <br>
	 * <b>Why</b> <br>
	 * {@link #getTMXTileAtIsometric(float, float)} is very similar to this
	 * method, but calls <code>convertSceneToLocalCoordinates</code> first.
	 * 
	 * @param pTouch
	 *            {@link Float} array of touch result from
	 *            <code>convertLocalToSceneCoordinates</code> <br>
	 *            <i>element [0]</i> is the X location <br>
	 *            <i>element [1]</i> is the Y location
	 * @return {@link TMXTile} of found location <b>OR</b> returns
	 *         <code>null</code> if not found, or the location is out side the
	 *         bounds of the tmx file.<br>
	 *         <code>null</code> may be returned if {@link #getAllocateTiles()}
	 *         returns false.
	 */
	public TMXTile getTMXTileAtIsometricAlternative(final float[] pTouch) {
		/*
		 * Implemented by Paul Robinson
		 * Referenced work Christian Knudsen of Laserbrain Studios - "The basics of isometric programming" 
		 * http://laserbrainstudios.com/2010/08/the-basics-of-isometric-programming/
		 */
		if (this.mAllocateTMXTiles) {
			int[] found = this.getRowColAtIsometric(pTouch);
			if(found != null){
				return this.mTMXTiles[found[0]][found[1]];
			}else{
				return null;
			}
		} else {
			return null;
		}
	}

	public void addTMXLayerProperty(final TMXLayerProperty pTMXLayerProperty) {
		this.mTMXLayerProperties.add(pTMXLayerProperty);
	}

	public TMXProperties<TMXLayerProperty> getTMXLayerProperties() {
		return this.mTMXLayerProperties;
	}

	/**
	 * Are {@link TMXTile} being allocated when creating {@link TMXLayer}
	 * 
	 * @return <code>true</code> if we are allocating, <code>false</code> if
	 *         not.
	 */
	public boolean getAllocateTiles() {
		return this.mAllocateTMXTiles;
	}

	/**
	 * Get the tile centre coordinates for the given column and row. <br>
	 * <b>note</b> Only isometric is currently supported.
	 * 
	 * @param pTileColumn
	 *            {@link Integer} column of tile
	 * @param pTileRow
	 *            {@link Integer} row of tile.
	 * @return {@link Float} array <br>
	 *         <b>Element[0]</b> is X <br>
	 *         <b>Element[1]</b> is Y
	 */
	public float[] getTileCentre(final int pTileColumn, final int pTileRow) {
		if (this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)) {
			return null;
		} else if (this.mTMXTiledMap.getOrientation()
				.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)) {
			return this.getIsoTileCentreAt(pTileColumn, pTileRow);
		} else {
			Log.w(TAG, String.format("getTileCentre: Orientation not supported: '%s'. " + "will return null.",
					this.mTMXTiledMap.getOrientation()));
			return null;
		}
	}

	/**
	 * Get the tile isometric centre coordinates for the given column and row.
	 * 
	 * @param pTileColumn
	 *            {@link Integer} column of tile
	 * @param pTileRow
	 *            {@link Integer} row of tile.
	 * @return {@link Float} array <br>
	 *         <b>Element[0]</b> is X <br>
	 *         <b>Element[1]</b> is Y
	 */
	public float[] getIsoTileCentreAt(final int pTileColumn, final int pTileRow) {
		/*
		 * Get the first tile.
		 * Get the first tile iso X and Y for the given pTileRow
		 * Then do the adding to get the required tile in pTileColumn.
		 */
		float firstTileXCen = + this.mIsoHalfTileWidth;
		float firstTileYCen = + this.mIsoHalfTileHeight;
		float isoX = 0;
		float isoY = 0;

		isoX = firstTileXCen - (pTileRow * this.mIsoHalfTileWidth);
		isoY = firstTileYCen + (pTileRow * this.mIsoHalfTileHeight);

		isoX = isoX + (pTileColumn * this.mIsoHalfTileWidth);
		isoY = isoY + (pTileColumn * this.mIsoHalfTileHeight);
		isoY = 0 - isoY;
		return new float[] { isoX, isoY };
	}

	/**
	 * Get the row and column at the given touch location. <br>
	 * Derived from {@link #getTMXTileAtIsometricAlternative(float[])}
	 * 
	 * @param pTouch
	 *            {@link Float} array of touch coordinates as they are or after
	 *            using <code>convertSceneToLocalCoordinates</code>
	 * @return {@link Integer} array. <br>
	 *         <b>Element[0]:</b> Tile Row <br>
	 *         <b>Element[1}</b> Tile Column
	 */
	public int[] getRowColAtIsometric(final float[] pTouch) {
		float pX = pTouch[0];
		float pY = pTouch[1];
		float screenX = pX - this.mTMXTiledMap.getTileHeight();
		float screenY = pY < 0 ? Math.abs(pY) : 0 - pY;

		float tileColumn = (screenY / this.mTMXTiledMap.getTileHeight()) + (screenX / this.mTMXTiledMap.getTileWidth());
		float tileRow = (screenY / this.mTMXTiledMap.getTileHeight()) - (screenX / this.mTMXTiledMap.getTileWidth());

		if (tileColumn < 0 || tileColumn > this.mTileColumns) {
			return null;
		}
		if (tileRow < 0 || tileRow > this.mTileRows) {
			return null;
		}
		return new int[] { (int) tileRow, (int) tileColumn };
	}

	/**
	 * Get the global tile id for a give tile location.
	 * 
	 * @param pTileRow
	 *            {@link Integer} of tile row.
	 * @param pTileColumn
	 *            {@link Integer} of tile column.
	 * @return {@link Integer} of global tile ID, <code>-1</code> if out of
	 *         bounds. <code>-2</code> if global tile id's aren't stored
	 */
	public int getTileGlobalID(int pTileRow, int pTileColumn) {
		if (pTileColumn < 0 || pTileColumn >= this.mTileColumns) {
			return -1;
		}
		if (pTileRow < 0 || pTileRow >= this.mTileRows) {
			return -1;
		}
		if (this.mStoreGID) {
			return this.mTileGID[pTileRow][pTileColumn];
		} else {
			return -2;
		}
	}

	/**
	 * Get the ever global tile IDs for every {@link TMXTile}
	 * 
	 * @return {@link Integer} array of each tile global tile id.
	 */
	public int[][] getTMXTileGlobalIDs() {
		return this.mTileGID;
	}

	/**
	 * Get how many tiles have been added to the layer.
	 * 
	 * @return {@link Integer} of how many tiles added to the layer.
	 */
	public int getTMXTilesAdded() {
		return this.mTilesAdded;
	}

	/**
	 * Get how many global tile IDs expected
	 * 
	 * @return {@link Integer} of global tile IDs expected
	 */
	public int getGlobalTileIDsExpected() {
		return this.mGlobalTileIDsExpected;
	}

	public double getTileRatio() {
		return this.tileratio;
	}

	/**
	 * 
	 * @see #mIsoHalfTileWidth
	 */
	public int getIsometricHalfTileWidth() {
		return this.mIsoHalfTileWidth;
	}

	/**
	 * 
	 * @see #mIsoHalfTileHeight
	 */
	public int getIsometricHalfTileHeight() {
		return this.mIsoHalfTileHeight;
	}

	/**
	 * 
	 * @see #mAddedRows
	 */
	public int getAddedRows() {
		return this.mAddedRows;
	}

	/**
	 * 
	 * @see #mAddedTilesOnRow;
	 */
	public int getAddedTilesOnRow() {
		return this.mAddedTilesOnRow;
	}

	/**
	 * 
	 * @see #DRAW_METHOD_ISOMETRIC
	 * @see TMXIsometricConstants
	 */
	public int getIsometricDrawMethod() {
		return this.DRAW_METHOD_ISOMETRIC;
	}

	/**
	 * 
	 * @see TMXTiledMap#getStoreGID()
	 */
	public boolean getStoreGID() {
		return this.mStoreGID;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void initBlendFunction(final ITexture pTexture) {

	}

	@Override
	@Deprecated
	public void setRotation(final float pRotation) throws MethodNotSupportedException {
		throw new MethodNotSupportedException();
	}

	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		/* Nothing. */
	}

	/**
	 * Modified to take in account the map orientation. <br>
	 * If orientation is not supported then a warning will be thrown to the log
	 * and will call {@link #drawOrthogonal(GLState, Camera)}
	 * 
	 */
	@Override
	protected void draw(final GLState pGLState, final Camera pCamera) {
		// Modified by Paul Robinson
		if (this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)) {
			this.drawOrthogonal(pGLState, pCamera);
		} else if (this.mTMXTiledMap.getOrientation()
				.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)) {
			this.drawIsometric(pGLState, pCamera);
		} else {
			Log.w(TAG, String.format("Orientation not supported: '%s'. " + "Will use normal Orthogonal draw method",
					this.mTMXTiledMap.getOrientation()));
			this.drawOrthogonal(pGLState, pCamera);
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	void initializeTMXTileFromXML(final Attributes pAttributes,
			final ITMXTilePropertiesListener pTMXTilePropertyListener) {
		this.addTileByGlobalTileID(SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_TILE_ATTRIBUTE_GID),
				pTMXTilePropertyListener);
	}

	void initializeTMXTilesFromDataString(final String pDataString, final String pDataEncoding,
			final String pDataCompression, final ITMXTilePropertiesListener pTMXTilePropertyListener)
			throws IOException, IllegalArgumentException {
		DataInputStream dataIn = null;
		try {
			InputStream in = new ByteArrayInputStream(pDataString.getBytes("UTF-8"));

			/* Wrap decoding Streams if necessary. */
			if (pDataEncoding != null && pDataEncoding.equals(TMXConstants.TAG_DATA_ATTRIBUTE_ENCODING_VALUE_BASE64)) {
				in = new Base64InputStream(in, Base64.DEFAULT);
			}
			if (pDataCompression != null) {
				if (pDataCompression.equals(TMXConstants.TAG_DATA_ATTRIBUTE_COMPRESSION_VALUE_GZIP)) {
					in = new GZIPInputStream(in);
				} else {
					throw new IllegalArgumentException("Supplied compression '" + pDataCompression
							+ "' is not supported yet.");
				}
			}
			dataIn = new DataInputStream(in);

			while (this.mTilesAdded < this.mGlobalTileIDsExpected) {
				final int globalTileID = this.readGlobalTileID(dataIn);
				this.addTileByGlobalTileID(globalTileID, pTMXTilePropertyListener);
			}
			this.submit();
		} finally {
			StreamUtils.close(dataIn);
		}
	}

	/**
	 * Add a tile to an orthogonal or isometric map. <br>
	 * <br>
	 * <b>Note </b> <br>
	 * <i>
	 * {@link #addTileByGlobalTileIDOrthogonal(int, ITMXTilePropertiesListener)}
	 * </i> does not implement offsets. <br>
	 * <i>
	 * {@link #addTileByGlobalTileIDIsometric(int, ITMXTilePropertiesListener)}
	 * </i> Does implement offsets so watch out for tile displacements (tiles in
	 * the incorrect position) <br>
	 * <br>
	 * For more information about Isometric tile maps see
	 * {@link #addTileByGlobalTileIDIsometric(int, ITMXTilePropertiesListener)}
	 * <br>
	 * If the map orientation is not supported then
	 * {@link #addTileByGlobalTileIDOrthogonal(int, ITMXTilePropertiesListener)}
	 * is used instead, but a warning will be thrown to the log.
	 *
	 * @param pGlobalTileID
	 *            {@link integer} of the global tile id
	 * @param pTMXTilePropertyListener
	 *            {@link ITMXTilePropertiesListener}
	 */
	private void addTileByGlobalTileID(final int pGlobalTileID,
			final ITMXTilePropertiesListener pTMXTilePropertyListener) {
		if (this.mTMXTiledMap.getOrientation().equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)) {
			this.addTileByGlobalTileIDOrthogonal(pGlobalTileID, pTMXTilePropertyListener);
		} else if (this.mTMXTiledMap.getOrientation()
				.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)) {
			this.addTileByGlobalTileIDIsometric(pGlobalTileID, pTMXTilePropertyListener);
		} else {
			Log.w(TAG, String.format("Orientation not supported: '%s'. "
					+ "Will use original addTileByGlobalTileIDOriginal method ", this.mTMXTiledMap.getOrientation()));
			this.addTileByGlobalTileIDOrthogonal(pGlobalTileID, pTMXTilePropertyListener);
		}
	}

	/**
	 * Add a tile to an orthogonal map. <br>
	 * A slightly modified version of the original implementation in that it can
	 * now have transparent tiles. <br>
	 * <b>Note </b> This does not implement any offsets!. <br>
	 * <b>Note </b> This does not implement {@link #getAllocateTiles()}
	 * 
	 * @param pGlobalTileID
	 * @param pTMXTilePropertyListener
	 */
	private void addTileByGlobalTileIDOrthogonal(final int pGlobalTileID,
			final ITMXTilePropertiesListener pTMXTilePropertyListener) {
		final TMXTiledMap tmxTiledMap = this.mTMXTiledMap;

		final int tilesHorizontal = this.mTileColumns;

		final int column = this.mTilesAdded % tilesHorizontal;
		final int row = this.mTilesAdded / tilesHorizontal;

		final TMXTile[][] tmxTiles = this.mTMXTiles;

		final ITextureRegion tmxTileTextureRegion;
		if (pGlobalTileID == 0) {
			tmxTileTextureRegion = null;
		} else {
			tmxTileTextureRegion = tmxTiledMap.getTextureRegionFromGlobalTileID(pGlobalTileID);
		}
		final int tileHeight = this.mTMXTiledMap.getTileHeight();
		final int tileWidth = this.mTMXTiledMap.getTileWidth();

		if (tmxTileTextureRegion != null) {
			// Unless this is a transparent tile, setup the texture
			if (this.mTexture == null) {
				this.mTexture = tmxTileTextureRegion.getTexture();
				super.initBlendFunction(this.mTexture);
			} else {
				if (this.mTexture != tmxTileTextureRegion.getTexture()) {
					throw new AndEngineRuntimeException("All TMXTiles in a TMXLayer (" + mName
							+ ") need to be in the same TMXTileSet.");
				}
			}
		}

		final TMXTile tmxTile = new TMXTile(this.mTMXTiledMap.getOrientation(), pGlobalTileID, this.mTilesAdded,
				column, row, tileWidth, tileHeight, tmxTileTextureRegion);
		tmxTiles[row][column] = tmxTile;

		if (pGlobalTileID != 0) {
			this.setIndex(this.getSpriteBatchIndex(column, row));
			this.drawWithoutChecks(tmxTileTextureRegion, tmxTile.getTileX(), tmxTile.getTileY(), tileWidth, tileHeight,
					Color.WHITE_ABGR_PACKED_FLOAT);

			// Notify the ITMXTilePropertiesListener if it exists.
			if (pTMXTilePropertyListener != null) {
				final TMXProperties<TMXTileProperty> tmxTileProperties = tmxTiledMap
						.getTMXTileProperties(pGlobalTileID);
				if (tmxTileProperties != null) {
					pTMXTilePropertyListener.onTMXTileWithPropertiesCreated(tmxTiledMap, this, tmxTile,
							tmxTileProperties);
				}
			}
		}

		this.mTilesAdded++;
	}

	/**
	 * Add a tile to an isometric map. <br>
	 * <br>
	 * 
	 * This can work with maps that use a global tile id of 0 (ie transparent) <br>
	 * Derived from the original code in
	 * {@link #addTileByGlobalTileIDOrthogonal(int, ITMXTilePropertiesListener)}
	 * <br>
	 * <br>
	 * <b>NOTE </b>Tileset offsets are implemented! <i>Watch out for tile
	 * displacements when using offsets!</i> <br>
	 * <b>NOTE </b> X is the row on the left hand side. Y is the columns on the
	 * right. <br>
	 * <b>NOTE</b> when using X offsets in Tiled, the X offset should be a
	 * negative number! <br>
	 * <br>
	 * Tiled renders and stores tile positions in the TMX file a certain way.
	 * The tiles are drawn in rows going left to right, top to bottom. In
	 * addition, when tiles are larger than the tile grid of the map, they are
	 * aligned to the bottom-left corner of their cell and will stick out to the
	 * top and to the right. This is where the offset comes into play for a
	 * tileset. <i>source: somewhere at the tiled github site</i> <br>
	 * <br>
	 * This also determines the draw position (with offsets applied) for the
	 * {@link SpriteBatch} along with the centre of the tile. This makes life a
	 * bit easier for Paul Robinson! <br>
	 * 
	 * @param pGlobalTileID
	 *            {@link integer} of the global tile id
	 * @param pTMXTilePropertyListener
	 *            {@link ITMXTilePropertiesListener}
	 */
	private void addTileByGlobalTileIDIsometric(final int pGlobalTileID,
			final ITMXTilePropertiesListener pTMXTilePropertyListener) {
		/*
		 * Implemented by - Paul Robinson
		 * Referenced work - athanazio - "Working with Isometric Maps"
		 * http://www.athanazio.com/2008/02/21/working-with-isometric-maps/
		 * http://www.athanazio.com/wp-content/uploads/2008/02/isomapjava.txt
		 */
		final TMXTiledMap tmxTiledMap = this.mTMXTiledMap;
		final int tilesHorizontal = this.mTileColumns;
		// Tile height and width of the map not the tileset!
		final int tileHeight = this.mTMXTiledMap.getTileHeight();
		final int tileWidth = this.mTMXTiledMap.getTileWidth();
		final int column = this.mTilesAdded % tilesHorizontal;
		final int row = this.mTilesAdded / tilesHorizontal;
		TMXTile[][] tmxTiles = null;
		if (this.mAllocateTMXTiles) {
			tmxTiles = this.mTMXTiles;
		}

		final ITextureRegion tmxTileTextureRegion;

		if (pGlobalTileID == 0) {
			tmxTileTextureRegion = null;
		} else {
			tmxTileTextureRegion = tmxTiledMap.getTextureRegionFromGlobalTileID(pGlobalTileID);
		}

		if (tmxTileTextureRegion != null) {
			// Unless this is a transparent tile, setup the texture
			if (this.mTexture == null) {
				this.mTexture = tmxTileTextureRegion.getTexture();
				super.initBlendFunction(this.mTexture);
			} else {
				if (this.mTexture != tmxTileTextureRegion.getTexture()) {
					throw new AndEngineRuntimeException("All TMXTiles in a TMXLayer (" + mName
							+ ") need to be in the same TMXTileSet.");
				}
			}
		}
		TMXTile tmxTile = null;
		if (this.mAllocateTMXTiles) {
			tmxTile = new TMXTile(this.mTMXTiledMap.getOrientation(), pGlobalTileID, this.mTilesAdded, column, row,
					tileWidth, tileHeight, tmxTileTextureRegion);
		}

		if (this.mStoreGID) {
			this.mTileGID[row][column] = pGlobalTileID;
		}

		// Get the offset for the tileset and the tileset size
		/*
		 * element[0] is the X offset.
		 * element[1] is the Y offset.
		 * element[2] is the tile width.
		 * element[3] is the tile height.
		 */
		int[] offset_tilesize = { 0, 0, tileWidth, tileHeight };
		if (pGlobalTileID == 0) {
			// tile is transparent so there is no offset, and use default map
			// tile size
		} else {
			offset_tilesize = this.mTMXTiledMap.checkTileSetOffsetAndSize(pGlobalTileID);
		}

		/*
		 * Work out where the "perfect" isometric tile should go.
		 * Perfect meaning a tile from a tileset of the correct height and 
		 * width matching the map tile height and width.
		 */
		float xRealIsoPos = (this.mAddedTilesOnRow * this.mIsoHalfTileWidth);
		xRealIsoPos = xRealIsoPos - (this.mAddedRows * this.mIsoHalfTileWidth);
		float yRealIsoPos =- (this.mAddedTilesOnRow * this.mIsoHalfTileHeight);
		yRealIsoPos = yRealIsoPos - (this.mAddedRows * this.mIsoHalfTileHeight);
		float yOffsetPos = yRealIsoPos - ((offset_tilesize[3] - tileHeight) + offset_tilesize[1]);
		/*
		 * Fixes #1
		 */
		float xOffsetPos = 0;
		if (offset_tilesize[0] > 0) {
			xOffsetPos = xRealIsoPos + Math.abs(offset_tilesize[0]);
		} else {
			xOffsetPos = xRealIsoPos - Math.abs(offset_tilesize[0]);
		}
		float tileXIso = xOffsetPos;
		float tileYIso = yOffsetPos;
		
		if (this.mAllocateTMXTiles) {
			tmxTile.setTileXIso(xOffsetPos);
			tmxTile.setTileYIso(yOffsetPos);
		}
		float xCentre = xRealIsoPos + this.mIsoHalfTileWidth;
		float yCentre = yRealIsoPos - this.mIsoHalfTileHeight;
		if (this.mAllocateTMXTiles) {
			tmxTile.setTileXIsoCentre(xCentre);
			tmxTile.setTileYIsoCentre(yCentre);
			tmxTiles[row][column] = tmxTile;
			this.mTMXTiles[row][column] = tmxTile;
		}
		this.mAddedTilesOnRow++;

		if (this.mAddedTilesOnRow == this.mTMXTiledMap.getTileColumns()) {
			// Reset the tiles added to a row
			this.mAddedTilesOnRow = 0;
			// Increase the numbers of rows added
			this.mAddedRows++;
		}

		if (pGlobalTileID != 0) {
			this.setIndex(this.getSpriteBatchIndex(column, row));
			// Before we were drawing to the map tile size, not the tileset size
			this.drawWithoutChecks(tmxTileTextureRegion, tileXIso, tileYIso, offset_tilesize[2], offset_tilesize[3], Color.WHITE_ABGR_PACKED_FLOAT);
			
			// Notify the ITMXTilePropertiesListener if it exists.
			if (pTMXTilePropertyListener != null) {
				final TMXProperties<TMXTileProperty> tmxTileProperties = tmxTiledMap
						.getTMXTileProperties(pGlobalTileID);
				if (tmxTileProperties != null) {
					pTMXTilePropertyListener.onTMXTileWithPropertiesCreated(tmxTiledMap, this, tmxTile,
							tmxTileProperties);
				}
			}
		}
		this.mTilesAdded++;
	}
	
	protected int getSpriteBatchIndex(final int pColumn, final int pRow) {
		return pRow * this.mTileColumns + pColumn;
	}

	private int readGlobalTileID(final DataInputStream pDataIn) throws IOException {
		final int lowestByte = pDataIn.read();
		final int secondLowestByte = pDataIn.read();
		final int secondHighestByte = pDataIn.read();
		final int highestByte = pDataIn.read();

		if (lowestByte < 0 || secondLowestByte < 0 || secondHighestByte < 0 || highestByte < 0) {
			throw new IllegalArgumentException("Couldn't read global Tile ID.");
		}

		return lowestByte | secondLowestByte << 8 | secondHighestByte << 16 | highestByte << 24;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	// ===========================================================
	// Drawing options
	// ===========================================================

	/**
	 * Call if this if the map is Orthogonal <br>
	 * This is the original unmodified orthogonal render.
	 * 
	 * @param pGLState
	 * @param pCamera
	 */
	private void drawOrthogonal(final GLState pGLState, final Camera pCamera) {
		final int tileColumns = this.mTileColumns;
		final int tileRows = this.mTileRows;
		final int tileWidth = this.mTMXTiledMap.getTileWidth();
		final int tileHeight = this.mTMXTiledMap.getTileHeight();

		final float scaledTileWidth = tileWidth * this.mScaleX;
		final float scaledTileHeight = tileHeight * this.mScaleY;

		final float[] cullingVertices = this.mCullingVertices;

		final float layerMinX = cullingVertices[SpriteBatch.VERTEX_INDEX_X];
		final float layerMinY = cullingVertices[SpriteBatch.VERTEX_INDEX_Y];

		final float cameraMinX = pCamera.getXMin();
		final float cameraMinY = pCamera.getYMin();
		final float cameraWidth = pCamera.getWidth();
		final float cameraHeight = pCamera.getHeight();

		/* Determine the area that is visible in the camera. */
		final float firstColumnRaw = (cameraMinX - layerMinX) / scaledTileWidth;
		final int firstColumn = MathUtils.bringToBounds(0, tileColumns - 1, (int) Math.floor(firstColumnRaw));
		final int lastColumn = MathUtils.bringToBounds(0, tileColumns - 1,
				(int) Math.ceil(firstColumnRaw + cameraWidth / scaledTileWidth));

		final float firstRowRaw = (cameraMinY - layerMinY) / scaledTileHeight;
		final int firstRow = MathUtils.bringToBounds(0, tileRows - 1, (int) Math.floor(firstRowRaw));
		final int lastRow = MathUtils.bringToBounds(0, tileRows - 1,
				(int) Math.floor(firstRowRaw + cameraHeight / scaledTileHeight));

		for (int row = firstRow; row <= lastRow; row++) {
			for (int column = firstColumn; column <= lastColumn; column++) {
				this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP,
						this.getSpriteBatchIndex(column, row) * SpriteBatch.VERTICES_PER_SPRITE,
						SpriteBatch.VERTICES_PER_SPRITE);
			}
		}
	}

	/**
	 * Call this if the map is Isometric. <br>
	 * This calls the desired draw method that the user desires.
	 * 
	 * @param pGLState
	 * @param pCamera
	 */
	private void drawIsometric(final GLState pGLState, final Camera pCamera) {
		if (this.DRAW_METHOD_ISOMETRIC == TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_ALL) {
			this.drawIsometricAll(pGLState, pCamera);
		} else if (this.DRAW_METHOD_ISOMETRIC == TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_CULLING_SLIM) {
			this.drawIsometricCullingLoop(pGLState, pCamera);
		} else if (this.DRAW_METHOD_ISOMETRIC == TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_CULLING_PADDING) {
			this.drawIsometricCullingLoopExtra(pGLState, pCamera);
		} else if (this.DRAW_METHOD_ISOMETRIC == TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_CULLING_TILED_SOURCE) {
			this.drawIsometricCullingTiledSource(pGLState, pCamera);
		} else {
			Log.w(TAG,
					String.format(
							"Draw method %d is currently not supported or an unknown draw method. Will use the default draw method.",
							this.DRAW_METHOD_ISOMETRIC));
			this.DRAW_METHOD_ISOMETRIC = TMXIsometricConstants.DRAW_METHOD_ISOMETRIC_ALL;
			this.drawIsometricAll(pGLState, pCamera);
		}
	}

	/**
	 * This will draw all the tiles of an isometric map. <br>
	 * This is the most inefficient way to draw the tiles as no culling occurs
	 * e.g even if the tile isn't on the screen its still being drawn! <br>
	 * Using this will result in low FPS. So really unsuitable for large maps.
	 * 
	 * @param pGLState
	 *            {@link GLState}
	 * @param pCamera
	 *            {@link Camera}
	 */
	public void drawIsometricAll(final GLState pGLState, final Camera pCamera) {
		final int tileColumns = this.mTileColumns;
		final int tileRows = this.mTileRows;
		for (int j = 0; j < tileRows; j++) {
			for (int i = 0; i < tileColumns; i++) {
				this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP, this.getSpriteBatchIndex(i, j)
						* SpriteBatch.VERTICES_PER_SPRITE, SpriteBatch.VERTICES_PER_SPRITE);
			}
		}
	}

	/**
	 * This loops through all the tiles and checks if the centre location of the
	 * tile is within the screen space. <br>
	 * It doesn't loop through the TMXTile Arrays, instead it calculates the
	 * tile centre using maths. This is not the most efficient way to draw, but
	 * FPS is okish. <br>
	 * This calculates the tile location
	 * 
	 * @param pGLState
	 * @param pCamera
	 */
	public void drawIsometricCullingLoop(final GLState pGLState, final Camera pCamera) {
		final float cameraMinX = pCamera.getXMin();
		final float cameraMinY = pCamera.getYMin();
		final float cameraWidth = pCamera.getWidth();
		final float cameraHeight = pCamera.getHeight();
		final int tileColumns = this.mTileColumns;
		final int tileRows = this.mTileRows;

		final int yWholeMax = (int) (cameraMinY + cameraHeight);
		final int yWholeMin = (int) cameraMinY;
		final int xWholeMax = (int) (cameraMinX + cameraWidth);
		final int xWholeMin = (int) cameraMinX;

		for (int j = 0; j < tileRows; j++) {
			for (int i = 0; i < tileColumns; i++) {
				float[] isoCen = this.getIsoTileCentreAt(i, j);
				if (isoCen[1] < yWholeMax && isoCen[1] > yWholeMin) {
					if (isoCen[0] < xWholeMax && isoCen[0] > xWholeMin) {
						this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP,
								this.getSpriteBatchIndex(i, j) * SpriteBatch.VERTICES_PER_SPRITE,
								SpriteBatch.VERTICES_PER_SPRITE);
					}
				}
			}
		}
	}

	/**
	 * This loops through all the tiles and checks if the centre location of the
	 * tile is within or partly in the screen space. <br>
	 * It doesn't loop through the TMXTile Arrays, instead it calculates the
	 * tile centre using maths. <br>
	 * This is not the most efficient way to draw, but FPS is okish.
	 * 
	 * @param pGLState
	 * @param pCamera
	 */
	public void drawIsometricCullingLoopExtra(final GLState pGLState, final Camera pCamera) {
		final float cameraMinX = pCamera.getXMin();
		final float cameraMinY = pCamera.getYMin();
		final float cameraWidth = pCamera.getWidth();
		final float cameraHeight = pCamera.getHeight();
		final int tileColumns = this.mTileColumns;
		final int tileRows = this.mTileRows;
		final float tileHeight = this.mTMXTiledMap.getTileHeight();
		final float tileWidth = this.mTMXTiledMap.getTileWidth();

		final int yWholeMax = (int) (cameraMinY + cameraHeight);
		final int yWholeMin = (int) cameraMinY;
		final int yPartialMax = (int) (yWholeMax + tileHeight);
		final int yPartialMin = (int) (yWholeMin - tileHeight);

		final int xWholeMax = (int) (cameraMinX + cameraWidth);
		final int xWholeMin = (int) cameraMinX;
		final int xPartialMax = (int) (xWholeMax + tileWidth);
		final int xPartialMin = (int) (xWholeMin - tileWidth);

		final float[] cullingVertices = this.mCullingVertices;
		for (int j = 0; j < tileRows; j++) {
			for (int i = 0; i < tileColumns; i++) {
				float[] isoCen = this.getIsoTileCentreAt(i, j);
				if (isoCen[1] < yWholeMax && isoCen[1] > yWholeMin || isoCen[1] < yPartialMax
						&& isoCen[1] > yPartialMin) {
					if (isoCen[0] < xWholeMax && isoCen[0] > xWholeMin || isoCen[0] < xPartialMax
							&& isoCen[0] > xPartialMin) {
						this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP,
								this.getSpriteBatchIndex(i, j) * SpriteBatch.VERTICES_PER_SPRITE,
								SpriteBatch.VERTICES_PER_SPRITE);
					}
				}
			}
		}
	}

	/**
	 * Culling method taken from method paintLayer in IsoMapView.java from Tiled
	 * 0.7.2 source code. <br>
	 * Not to dissimilar to the function drawTileLayer in isometricrenderer.cpp
	 * from Tiled 0.8.0 source code. <br>
	 * Slight performance gain and draws tiles in a different order than the
	 * others, this does not appear to cause any problems. The tiles original Z
	 * order are unaffected. <br>
	 * Copyright 2009-2011, Thorbjorn Lindeijer <thorbjorn@lindeijer.nl>
	 * 
	 * @param pGLState
	 * @param pCamera
	 */
	public void drawIsometricCullingTiledSource(final GLState pGLState, final Camera pCamera) {
		/*
		 * Copyright 2009-2011, Thorbjorn Lindeijer <thorbjorn@lindeijer.nl>
		 * <br><a href="http://sourceforge.net/projects/tiled/files/Tiled/0.7.2/tiled-0.7.2-src.zip/">Tiled 0.7.2 source code zip</a>
		 * <br><a href="https://github.com/bjorn/tiled/blob/master/src/libtiled/isometricrenderer.cpp">Tiled 0.8.0 source code - isometricrenderer.cpp on Github</a>
		 * Copied across and changed slightly by Paul Robinson.
		 * Changes being using an int array rather than Point object, 
		 * The original Tiled Java source code used Point objects.
		 */
		final int tileWidth = this.mTMXTiledMap.getTileWidth();
		final int tileHeight = this.mTMXTiledMap.getTileHeight();
		/*
		 *  Since using AnchorCenter camera Y min is bottom left, so add the camera height
		 */
		final float cameraMinX = pCamera.getXMin();
		final float cameraMinY = pCamera.getYMin() + pCamera.getHeight();
		final float cameraWidth = pCamera.getWidth();
		final float cameraHeight = pCamera.getHeight();
		
		final float pY = cameraMinY < 0 ? Math.abs(cameraMinY) : 0 - cameraMinY;
		int[] rowItr = this.screenToTileCoords(cameraMinX, pY);
		rowItr[0]--;
		
		/*
		 *  Determine area to draw from clipping rectangle
		 *  Row calculation has changed
		 */
		int columns = (int) (cameraWidth / tileWidth + 3);
		int rows = (int) (cameraHeight / (tileHeight/2) + 4);
		// Draw this map layer
		for (int y = 0; y < rows; y++) {
			int[] columnItr = { rowItr[0], rowItr[1] };
			for (int x = 0; x < columns; x++) {
				if (columnItr[0] >= 0 && columnItr[0] < this.mTileColumns) {
					if (columnItr[1] >= 0 && columnItr[1] < this.mTileRows) {
						this.mSpriteBatchVertexBufferObject.draw(GLES20.GL_TRIANGLE_STRIP,
								this.getSpriteBatchIndex(columnItr[0], columnItr[1]) * SpriteBatch.VERTICES_PER_SPRITE,
								SpriteBatch.VERTICES_PER_SPRITE);
					}
				}
				// Advance to the next tile
				columnItr[0]++;
				columnItr[1]--;
			}
			if ((y & 1) > 0) {
				rowItr[0]++;
			} else {
				rowItr[1]++;
			}
		}
	}

	public int[] screenToTileCoords(float x, float y) {
		/*
		 * Copyright 2009-2011, Thorbjorn Lindeijer <thorbjorn@lindeijer.nl>
		 * <br><a href="http://sourceforge.net/projects/tiled/files/Tiled/0.7.2/tiled-0.7.2-src.zip/">Tiled 0.7.2 source code zip</a>
		 * <br><a href="https://github.com/bjorn/tiled/blob/master/src/libtiled/isometricrenderer.cpp">Tiled 0.8.0 source code - isometricrenderer.cpp on Github</a>
		 * Copied across and changed slightly by Paul Robinson.
		 * Changes being returning an int array rather than Point, 
		 * I didn't want to keep creating a Point object every time 
		 */

		int mx = (int) (y + (int) (x / this.tileratio));
		int my = (int) (y - (int) (x / this.tileratio));
		// be square in normal projection)
		return new int[] { (mx < 0 ? mx - this.mTMXTiledMap.getTileHeight() : mx) / this.mTMXTiledMap.getTileHeight(),
				(my < 0 ? my - this.mTMXTiledMap.getTileHeight() : my) / this.mTMXTiledMap.getTileHeight() };
	}
}
