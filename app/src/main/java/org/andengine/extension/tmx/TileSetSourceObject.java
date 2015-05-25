package org.andengine.extension.tmx;

import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;

public class TileSetSourceObject {
	
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private String mTileSetImageSource;
	private AssetBitmapTextureAtlasSource mAssetBitmapTextureAtlasSource;
	private int mTilesHorizontal;
	private int mTilesVertical;
	// ===========================================================
	// Constructors
	// ===========================================================
	public TileSetSourceObject(String pTileSetImageSource, AssetBitmapTextureAtlasSource pAssetBitmapTextureAtlasSource, int pTilesHorizontal, int pTilesVertical){
		this.mTileSetImageSource = pTileSetImageSource;
		this.mAssetBitmapTextureAtlasSource = pAssetBitmapTextureAtlasSource;
		this.mTilesHorizontal = pTilesHorizontal;
		this.mTilesVertical = pTilesVertical;
	}
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public String getImageSource(){
		return this.mTileSetImageSource;
	}
	
	public AssetBitmapTextureAtlasSource getBitmapAssest(){
		return this.mAssetBitmapTextureAtlasSource;
	}
	
	public int getTilesHorizontal(){
		return this.mTilesHorizontal;
	}
	
	public int getTilesVertical(){
		return this.mTilesVertical;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
