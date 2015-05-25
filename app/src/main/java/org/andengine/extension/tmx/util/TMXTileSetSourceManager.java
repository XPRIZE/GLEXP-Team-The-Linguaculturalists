package org.andengine.extension.tmx.util;

import java.util.HashMap;

import org.andengine.extension.tmx.TMXTileSet;
import org.andengine.extension.tmx.TSXParser;
import org.andengine.opengl.texture.ITexture;

import android.util.Log;

/**
 * This class helps manage {@link TMXTileSet} Texture resources. It stores
 * {@link TMXTileSet} {@link ITexture} in a key value pair. If we load the same
 * map twice and attach the two maps at different locations, then we've loaded
 * the same {@link TMXTileSet} image source twice. Using this class we can do a
 * lookup in the {@link TSXParser} on the tileset image source path and detect
 * if we've already read the image in, if so we can use the same texture thus
 * reducing system usage.
 * 
 * @author Paul Robinson
 * @since 28 Aug 2012 21:27:46
 */
public class TMXTileSetSourceManager {
	// ===========================================================
	// Constants
	// ===========================================================
	private final String TAG = "TMXTileSetSourceManager";
	// ===========================================================
	// Fields
	// ===========================================================
	/**
	 * This is where the key value pair is stored.<br>
	 * <b>Key: </b> {@link String} of image source path. <br>
	 * <b>Value: </b> {@link ITexture} of image loaded into memory
	 */
	private HashMap<String, ITexture> mTextureMappedToTileSetAddress;
	private HashMap<String, int[]> mTextureMappedDimensions;

	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * Constructor for a default {@link TMXTileSetSourceManager} This initialize
	 * the {@link HashMap} without java default capacity of 16. If you know how
	 * many unique tilesets you have overall, then use
	 * {@link #TMXTileSetSourceManager(int)} instead.<br>
	 * Setting the capacity before hand may increase performances if use many
	 * tilesets, if you don't then the {@link HashMap} may have to resize itself
	 * possibly give a performance hit.
	 * 
	 * @see TMXTileSetSourceManager#TMXTileSetSourceManager(int)
	 */
	public TMXTileSetSourceManager() {
		this.mTextureMappedToTileSetAddress = new HashMap<String, ITexture>();
		this.mTextureMappedDimensions = new HashMap<String, int[]>();
	}

	/**
	 * Constructor for a default {@link TMXTileSetSourceManager} This initialize
	 * the {@link HashMap} with a given capacity. <br>
	 * Setting the capacity before hand may increase performances if use many
	 * tilesets, if you don't then the {@link HashMap} may have to resize itself
	 * possibly give a performance hit.
	 * 
	 * @param pCapacity
	 *            Integer of {@link HashMap} to use.
	 */
	public TMXTileSetSourceManager(int pCapacity) {
		this.mTextureMappedToTileSetAddress = new HashMap<String, ITexture>(pCapacity);
		this.mTextureMappedDimensions = new HashMap<String, int[]>();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	/**
	 * Get the {@link ITexture} mapped to a given tileset image source.<br>
	 * If a source is mapped to a texture which is null, the mapping will be
	 * removed and a warning printed to the log.
	 * 
	 * @param pTileSetImageSource
	 *            {@link String} of tileset image source
	 * @return {@link ITexture} to use <b>OR</b> <code>null</code> if no mapping
	 *         exists.
	 */
	public ITexture getTileSetTexture(String pTileSetImageSource) {
		// Check if a mapping exists
		if (this.mTextureMappedToTileSetAddress.containsKey(pTileSetImageSource)) {
			// Check if the found mapping is null
			ITexture found = this.mTextureMappedToTileSetAddress.get(pTileSetImageSource);
			if (found == null) {
				// Mapping out of date, as texture is null.
				Log.w(TAG, "It appears the image source texture is null, removing mapping " + pTileSetImageSource);
				this.removeMapping(pTileSetImageSource);
				return null;
			} else {
				// Got a texture which is not null so return
				return found;
			}
		} else {
			return null;
		}
	}

	/**
	 * Add an {@link ITexture} of a given tileset image source to be mapped. <br>
	 * May print a warning to the log if there was a previous mapping with the
	 * same key.
	 * 
	 * @param pTileSetImageSource
	 *            {@link String} of tileset image source
	 * @param pTexture
	 *            {@link ITexture} to be mapped to the source.
	 */
	public void addTileSetTexture(String pTileSetImageSource, ITexture pTexture) {
		if (this.mTextureMappedToTileSetAddress.containsKey(pTileSetImageSource)) {
			Log.w(TAG, "Already contains a texture for image source: " + pTileSetImageSource);
		}
		this.mTextureMappedToTileSetAddress.put(pTileSetImageSource, pTexture);
	}

	/**
	 * Get how many tiles exists in the image source. <br>
	 * 
	 * @param pTileSetImageSource
	 *            {@link String} of tileset image source
	 * @return {@link Integer} array of tiles horizontal and vertical count<br>
	 *         <b>Element[0]:</b> Tiles Horizontal <br>
	 *         <b>Element[1]:</b> Tiles Vertical <br>
	 */
	public int[] getTileSourceSizes(String pTileSetImageSource) {
		// Check if a mapping exists
		if (this.mTextureMappedDimensions.containsKey(pTileSetImageSource)) {
			// Check if the found mapping is null
			int[] found = this.mTextureMappedDimensions.get(pTileSetImageSource);
			if (found == null) {
				// Mapping out of date, as TextureMappedDimensions is null.
				Log.w(TAG, "It appears the image source dimensions is null, removing mapping " + pTileSetImageSource);
				this.removeMapping(pTileSetImageSource);
				return null;
			} else {
				// Got a mapping which is not null so return
				return found;
			}
		} else {
			return null;
		}
	}

	/**
	 * Set how many tiles exist in the image source.<br>
	 * 
	 * @param pTileSetImageSource
	 *            {@link String} of tileset image source
	 * @param pValues
	 *            {@link Integer} array of tiles horizontal and vertical count
	 *            <b>Element[0]:</b> Tiles Horizontal <br>
	 *            <b>Element[1]:</b> Tiles Vertical <br>
	 */
	public void addTileSourcesSize(String pTileSetImageSource, int[] pValues) {
		if (this.mTextureMappedDimensions.containsKey(pTileSetImageSource)) {
			Log.w(TAG, "Already contains dimensions for image source: " + pTileSetImageSource);
		}
		this.mTextureMappedDimensions.put(pTileSetImageSource, pValues);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void removeMapping(String pTileSetImageSource){
		this.mTextureMappedDimensions.remove(pTileSetImageSource);
		this.mTextureMappedToTileSetAddress.remove(pTileSetImageSource);
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
