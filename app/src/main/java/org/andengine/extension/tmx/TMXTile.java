package org.andengine.extension.tmx;

import org.andengine.extension.tmx.util.constants.TMXConstants;
import org.andengine.opengl.texture.region.ITextureRegion;

import android.R.integer;

/**
 * (c) 2010 Nicolas Gramlich (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 10:39:48 - 05.08.2010
 */
public class TMXTile {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private int mGlobalTileID;
	private final int mTileRow;
	private final int mTileColumn;
	private final int mTileWidth;
	private final int mTileHeight;
	ITextureRegion mTextureRegion;

	private final int mTileZ;
	private final String mOrientation;
	private float mTileXIso = 0;
	private float mTileYIso = 0;
	private float mTileXIsoCentre = 0;
	private float mTileYIsoCentre = 0;
	private TMXObject mTMXObject = null;

	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * 
	 * @param pGlobalTileID
	 * @param pTileZ
	 *            Z {@link integer} of the Z index
	 * @param pTileColumn
	 * @param pTileRow
	 * @param pTileWidth
	 * @param pTileHeight
	 * @param pTextureRegion
	 */
	public TMXTile(final String pOrientation, final int pGlobalTileID, final int pTileZ, final int pTileColumn,
			final int pTileRow, final int pTileWidth, final int pTileHeight, final ITextureRegion pTextureRegion) {
		this.mOrientation = pOrientation;
		this.mGlobalTileID = pGlobalTileID;
		this.mTileZ = pTileZ;
		this.mTileRow = pTileRow;
		this.mTileColumn = pTileColumn;
		this.mTileWidth = pTileWidth;
		this.mTileHeight = pTileHeight;
		this.mTextureRegion = pTextureRegion;
	}

	/**
	 * Create a copy of a TMXTile
	 * 
	 * @param pTMXTile
	 *            {@link TMXTile} to copy
	 * @param pDeepCopyTextures
	 *            {@link Boolean} Should textures also be copied?
	 *            <code>true</code> for copy, <code>false</code> for no copy.
	 * @throws Exception If the passed {@link TMXTile} is null
	 */
	public TMXTile(final TMXTile pTMXTile, final boolean pDeepCopyTextures) throws Exception {
		if(pTMXTile == null){
			throw new Exception("Passed TMXTile to copy is null");
		}
		this.mGlobalTileID = pTMXTile.getGlobalTileID();
		this.mTileRow = pTMXTile.getTileRow();
		this.mTileColumn = pTMXTile.getTileColumn();
		this.mTileWidth = pTMXTile.getTileWidth();
		this.mTileHeight = pTMXTile.getTileHeight();
		if (pDeepCopyTextures) {
			this.mTextureRegion = pTMXTile.getTextureRegion().deepCopy();
		} else {
			this.mTextureRegion = null;
		}
		this.mTileZ = pTMXTile.getTileZ();
		this.mOrientation = pTMXTile.getOrientation();
		this.mTileXIso = pTMXTile.getTileXIso();
		this.mTileYIso = pTMXTile.getTileYIso();
		this.mTileXIsoCentre = pTMXTile.getTileXIsoCentre();
		this.mTileYIsoCentre = pTMXTile.getTileYIsoCentre();
		this.mTMXObject = new TMXObject(pTMXTile.getTMXObject());
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public int getGlobalTileID() {
		return this.mGlobalTileID;
	}

	public int getTileRow() {
		return this.mTileRow;
	}

	public int getTileColumn() {
		return this.mTileColumn;
	}

	public float getTileX() {
		if (this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)) {
			return this.mTileColumn * this.mTileWidth;
		} else if (this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)) {
			return this.mTileXIso;
		} else {
			return this.mTileColumn * this.mTileWidth;
		}
	}

	public float getTileY() {
		if (this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ORTHOGONAL)) {
			return this.mTileRow * this.mTileHeight;
		} else if (this.mOrientation.equals(TMXConstants.TAG_MAP_ATTRIBUTE_ORIENTATION_VALUE_ISOMETRIC)) {
			return this.mTileYIso;
		} else {
			return this.mTileRow * this.mTileHeight;
		}
	}

	public int getTileWidth() {
		return this.mTileWidth;
	}

	public int getTileHeight() {
		return this.mTileHeight;
	}

	public ITextureRegion getTextureRegion() {
		return this.mTextureRegion;
	}

	public int getTileZ() {
		return this.mTileZ;
	}

	public float getTileXIso() {
		return mTileXIso;
	}

	public void setTileXIso(float mTileXIso) {
		this.mTileXIso = mTileXIso;
	}

	public float getTileYIso() {
		return mTileYIso;
	}

	public void setTileYIso(float mTileYIso) {
		this.mTileYIso = mTileYIso;
	}

	public float getTileXIsoCentre() {
		return mTileXIsoCentre;
	}

	public void setTileXIsoCentre(float mTileXIsoCenter) {
		this.mTileXIsoCentre = mTileXIsoCenter;
	}

	public float getTileYIsoCentre() {
		return mTileYIsoCentre;
	}

	public void setTileYIsoCentre(float mTileYIsoCenter) {
		this.mTileYIsoCentre = mTileYIsoCenter;
	}

	/**
	 * If this tile is in fact an Object tile(used for collisions), then you can
	 * set its related TMXObject.
	 * 
	 * @param pTMXObject
	 *            {@link TMXObject} The parent TMXObject
	 */
	public void setTMXObject(final TMXObject pTMXObject) {
		this.mTMXObject = pTMXObject;
	}

	/**
	 * If this tile is in fact an Object tile(used for collisions), then you can
	 * get its related TMXObject.
	 * 
	 * @return {@link TMXObject} <b>OR</b> <code>NULL</code> if its not an
	 *         object tile. It might be a TMXObject based tile, but hasn't got
	 *         it parent TMXObject set.
	 */
	public TMXObject getTMXObject() {
		return this.mTMXObject;
	}

	/**
	 * Note this will also set the {@link ITextureRegion} with the associated
	 * pGlobalTileID of the {@link TMXTiledMap}.
	 * 
	 * @param pTMXTiledMap
	 * @param pGlobalTileID
	 */
	public void setGlobalTileID(final TMXTiledMap pTMXTiledMap, final int pGlobalTileID) {
		this.mGlobalTileID = pGlobalTileID;
		this.mTextureRegion = pTMXTiledMap.getTextureRegionFromGlobalTileID(pGlobalTileID);
	}

	/**
	 * You'd probably want to call
	 * {@link TMXTile#setGlobalTileID(TMXTiledMap, int)} instead.
	 * 
	 * @param pTextureRegion
	 */
	public void setTextureRegion(final ITextureRegion pTextureRegion) {
		this.mTextureRegion = pTextureRegion;
	}

	public TMXProperties<TMXTileProperty> getTMXTileProperties(final TMXTiledMap pTMXTiledMap) {
		return pTMXTiledMap.getTMXTileProperties(this.mGlobalTileID);
	}

	public String getOrientation() {
		return this.mOrientation;
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
