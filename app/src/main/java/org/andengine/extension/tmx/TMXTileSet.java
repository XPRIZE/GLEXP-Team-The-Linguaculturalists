package org.andengine.extension.tmx;

import java.util.ArrayList;

import org.andengine.extension.tmx.util.TMXTileSetSourceManager;
import org.andengine.extension.tmx.util.constants.TMXConstants;
import org.andengine.extension.tmx.util.exception.TMXParseException;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.bitmap.source.decorator.ColorKeyBitmapTextureAtlasSourceDecorator;
import org.andengine.opengl.texture.atlas.bitmap.source.decorator.shape.RectangleBitmapTextureAtlasSourceDecoratorShape;
import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.util.SAXUtils;
import org.xml.sax.Attributes;

import android.R.integer;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.util.SparseArray;

/**
 * (c) 2010 Nicolas Gramlich (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 19:03:24 - 20.07.2010
 */
public class TMXTileSet implements TMXConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final int mFirstGlobalTileID;
	private final String mName;
	private final int mTileWidth;
	private final int mTileHeight;

	private String mImageSource;
	private ITexture mTexture;
	private final TextureOptions mTextureOptions;

	private int mTilesHorizontal;
	private int mTilesVertical;

	private final int mSpacing;
	private final int mMargin;

	private int mOffsetX = 0;
	private int mOffsetY = 0;

	private final SparseArray<TMXProperties<TMXTileProperty>> mTMXTileProperties = new SparseArray<TMXProperties<TMXTileProperty>>();
	/**
	 * Since there is no way of retriving the key set from a SparseArray we have
	 * to maintain our own, useful if we want to copy!
	 */
	private ArrayList<Integer> mTMXTilePropertiesKeys = new ArrayList<Integer>();
	private final TMXTileSetSourceManager mTMXTileSetSourceManager;

	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * Create a new {@link TMXTileSet}
	 * 
	 * @param pAttributes
	 *            {@link Attributes} from XML.
	 * @param pTextureOptions
	 *            {@link TextureOptions} to use
	 * @param pTMXTileSetSourceManager
	 *            {@link TMXTileSetSourceManager} to use. Can pass
	 *            <code>null</code> if you don't want to use one.
	 */
	TMXTileSet(final Attributes pAttributes, final TextureOptions pTextureOptions,
			TMXTileSetSourceManager pTMXTileSetSourceManager) {
		this(SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_TILESET_ATTRIBUTE_FIRSTGID, 1), pAttributes,
				pTextureOptions, pTMXTileSetSourceManager);
	}

	/**
	 * Create a new {@link TMXTileSet}
	 * 
	 * @param pFirstGlobalTileID
	 *            {@link Integer} of the first global tile id.
	 * @param pAttributes
	 *            {@link Attributes} from XML.
	 * @param pTextureOptions
	 *            {@link TextureOptions} to use
	 * @param pTMXTileSetSourceManager
	 *            {@link TMXTileSetSourceManager} to use. Can pass
	 *            <code>null</code> if you don't want to use one.
	 */
	TMXTileSet(final int pFirstGlobalTileID, final Attributes pAttributes, final TextureOptions pTextureOptions,
			TMXTileSetSourceManager pTMXTileSetSourceManager) {
		this.mFirstGlobalTileID = pFirstGlobalTileID;
		this.mName = pAttributes.getValue("", TMXConstants.TAG_TILESET_ATTRIBUTE_NAME);
		this.mTileWidth = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_TILESET_ATTRIBUTE_TILEWIDTH);
		this.mTileHeight = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_TILESET_ATTRIBUTE_TILEHEIGHT);
		this.mSpacing = SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_TILESET_ATTRIBUTE_SPACING, 0);
		this.mMargin = SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_TILESET_ATTRIBUTE_MARGIN, 0);
		this.mTextureOptions = pTextureOptions;
		this.mTMXTileSetSourceManager = pTMXTileSetSourceManager;
	}

	/**
	 * A copy constructor for a TMXTileSet, currently cannot copy textures,
	 * texture options and TMXTileSetSourceManager.
	 * 
	 * @param pTMXTileSet
	 *            {@link TMXTileSet} to copy
	 */
	public TMXTileSet(final TMXTileSet pTMXTileSet) {
		this.mFirstGlobalTileID = pTMXTileSet.getFirstGlobalTileID();
		this.mName = new String(pTMXTileSet.getName());
		this.mTileWidth = pTMXTileSet.getTileWidth();
		this.mTileHeight = pTMXTileSet.getTileHeight();
		this.mImageSource = new String(pTMXTileSet.getImageSource());
		this.mTexture = null; // no copy supported
		this.mTextureOptions = null; // no copy supported
		this.mTMXTileSetSourceManager = null; // no copy supported
		this.mTilesHorizontal = pTMXTileSet.getTilesHorizontal();
		this.mTilesVertical = pTMXTileSet.getTilesVertical();
		this.mSpacing = pTMXTileSet.getSpacing();
		this.mMargin = pTMXTileSet.getMargin();
		this.mOffsetX = pTMXTileSet.getOffsetX();
		this.mOffsetY = pTMXTileSet.getOffsetY();
		this.mTMXTilePropertiesKeys = pTMXTileSet.getTMXTilePropertiesKeySet();
		for (Integer key : this.mTMXTilePropertiesKeys) {
			/*
			 * TMXProperties is an array list which holds TMXTileProperty objects
			 * First get the original properties for the current key
			 * create a new properties array
			 * Iterate the original properties and create a new TMXTileProperty from the current iteration element 
			 * Add the new properties to the current object properties. 
			 */
			TMXProperties<TMXTileProperty> properties = pTMXTileSet.getTMXTilePropertiesFromGlobalTileID(key);
			TMXProperties<TMXTileProperty> newProperties = new TMXProperties<TMXTileProperty>();
			for (TMXTileProperty tmxTileProperty : properties) {
				newProperties.add(new TMXTileProperty(tmxTileProperty));
			}

			this.mTMXTileProperties.put(key, newProperties);
		}

	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public final int getFirstGlobalTileID() {
		return this.mFirstGlobalTileID;
	}

	public final String getName() {
		return this.mName;
	}

	public final int getTileWidth() {
		return this.mTileWidth;
	}

	public final int getTileHeight() {
		return this.mTileHeight;
	}

	/**
	 * Get the tile set size. <br>
	 * 
	 * @return {@link integer} array of tile size <br>
	 *         <i>element[0]</i> Tile width. <br>
	 *         <i>element[1]</i> Tile weight.
	 */
	public final int[] getTileSize() {
		return new int[] { this.mTileWidth, this.mTileHeight };
	}

	public int getTilesHorizontal() {
		return this.mTilesHorizontal;
	}

	public int getTilesVertical() {
		return this.mTilesVertical;
	}

	/**
	 * Get the last global tileID for this tileset.
	 * 
	 * @return {@link integer} of the last global tile ID for this tileset.
	 */
	public int getLastGlobalTileID() {
		/*
		 * Calculate how many tiles exist in the tile set
		 * The last ID, is the tile count, minus the first ID minus 1 (1 since we
		 * already know the first global id).
		 */
		return (this.getTilesHorizontal() * this.getTilesVertical()) - this.getFirstGlobalTileID() - 1;
	}

	/**
	 * Get the range of global IDs for this tile set
	 * 
	 * @return {@link integer} array of first and last global id. <br>
	 *         <i>element[0]</i> First global id. <br>
	 *         <i>element[1]</i> Last global id.
	 */
	public int[] getTileRangeID() {
		return new int[] { this.getFirstGlobalTileID(), this.getLastGlobalTileID() };
	}

	public ITexture getTexture() {
		return this.mTexture;
	}

	/**
	 * Get the offset for this tile set.
	 * 
	 * @return {@link integer} array of X and Y offset. <br>
	 *         <i>element [0]</i> X offset. <br>
	 *         <i>element [1]</i> Y offset.
	 */
	public int[] getOffset() {
		return new int[] { this.mOffsetX, this.mOffsetY };
	}

	/**
	 * Get the offset for the X axis for this tile set.
	 * 
	 * @return {@link integer} of X offset.
	 */
	public int getOffsetX() {
		return this.mOffsetX;
	}

	/**
	 * Get the offset for the Y axis for this tile set.
	 * 
	 * @return {@link integer} of Y offset.
	 */
	public int getOffsetY() {
		return this.mOffsetY;
	}

	/**
	 * If the tile set has an offset, pass the attributes related to offsets
	 * here to parse it.
	 * 
	 * @param pAttributes
	 *            {@link Attributes} from the xml.
	 */
	public void addTileOffset(final Attributes pAttributes) {
		this.mOffsetX = SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_OFFSET_X, 0);
		this.mOffsetY = SAXUtils.getIntAttribute(pAttributes, TMXConstants.TAG_OFFSET_Y, 0);
	}

	public void setImageSource(final AssetManager pAssetManager, final TextureManager pTextureManager,
			final Attributes pAttributes) throws TMXParseException {
		this.mImageSource = pAttributes.getValue("", TMXConstants.TAG_IMAGE_ATTRIBUTE_SOURCE);
		if (this.mTMXTileSetSourceManager != null) {
			// Find the texture mapped to the image source
			this.mTexture = this.mTMXTileSetSourceManager.getTileSetTexture(this.mImageSource);
			// Check texture is not null
			if (this.mTexture == null) {
				// No mapping so read in
				this.setImageSourceFromAttributes(pAssetManager, pTextureManager, pAttributes);
			} else {
				// Find the tiles horizontal and vertical sizes
				int[] foundSize = this.mTMXTileSetSourceManager.getTileSourceSizes(this.mImageSource);
				if (foundSize == null) {
					// no sizes found so scrap the this process
					this.setImageSourceFromAttributes(pAssetManager, pTextureManager, pAttributes);
				} else {
					// Found sizes
					this.mTilesHorizontal = foundSize[0];
					this.mTilesVertical = foundSize[1];
				}
			}
		} else {
			// Not using a texture manager so skip a lookup
			this.setImageSourceFromAttributes(pAssetManager, pTextureManager, pAttributes);
		}
	}

	public String getImageSource() {
		return this.mImageSource;
	}

	public SparseArray<TMXProperties<TMXTileProperty>> getTMXTileProperties() {
		return this.mTMXTileProperties;
	}

	public int getSpacing() {
		return this.mSpacing;
	}

	public int getMargin() {
		return this.mMargin;
	}

	/**
	 * Since a {@link SparseArray} has no useful methods for iteration, we
	 * maintain a keyset in an {@link ArrayList} of {@link Integer}.
	 * 
	 * @return {@link ArrayList} of {@link Integer} of keys
	 */
	public ArrayList<Integer> getTMXTilePropertiesKeySet() {
		return this.mTMXTilePropertiesKeys;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	/**
	 * 
	 * @param pAssetManager
	 * @param pTextureManager
	 * @param pAttributes
	 * @throws TMXParseException
	 */
	private void setImageSourceFromAttributes(final AssetManager pAssetManager, final TextureManager pTextureManager,
			final Attributes pAttributes) throws TMXParseException {
		final AssetBitmapTextureAtlasSource assetBitmapTextureAtlasSource = AssetBitmapTextureAtlasSource.create(
				pAssetManager, this.mImageSource);
		this.mTilesHorizontal = TMXTileSet.determineCount(assetBitmapTextureAtlasSource.getTextureWidth(),
				this.mTileWidth, this.mMargin, this.mSpacing);
		this.mTilesVertical = TMXTileSet.determineCount(assetBitmapTextureAtlasSource.getTextureHeight(),
				this.mTileHeight, this.mMargin, this.mSpacing);
		final BitmapTextureAtlas bitmapTextureAtlas = new BitmapTextureAtlas(pTextureManager,
				assetBitmapTextureAtlasSource.getTextureWidth(), assetBitmapTextureAtlasSource.getTextureHeight(),
				BitmapTextureFormat.RGBA_8888, this.mTextureOptions); // TODO
																		// Make
																		// TextureFormat
																		// variable

		final String transparentColor = SAXUtils
				.getAttribute(pAttributes, TMXConstants.TAG_IMAGE_ATTRIBUTE_TRANS, null);
		if (transparentColor == null) {
			BitmapTextureAtlasTextureRegionFactory.createFromSource(bitmapTextureAtlas, assetBitmapTextureAtlasSource,
					0, 0);
		} else {
			try {
				final int color = Color.parseColor((transparentColor.charAt(0) == '#') ? transparentColor : "#"
						+ transparentColor);
				BitmapTextureAtlasTextureRegionFactory.createFromSource(bitmapTextureAtlas,
						new ColorKeyBitmapTextureAtlasSourceDecorator(assetBitmapTextureAtlasSource,
								RectangleBitmapTextureAtlasSourceDecoratorShape.getDefaultInstance(), color), 0, 0);
			} catch (final IllegalArgumentException e) {
				throw new TMXParseException(
						"Illegal value: '" + transparentColor + "' for attribute 'trans' supplied!", e);
			}
		}
		/*
		 * Check we're using a manager, if so load in the texture and then map the text to source.
		 */
		if (this.mTMXTileSetSourceManager != null) {
			this.mTexture = bitmapTextureAtlas;
			this.mTexture.load();
			this.mTMXTileSetSourceManager.addTileSetTexture(this.mImageSource, bitmapTextureAtlas);
			this.mTMXTileSetSourceManager.addTileSourcesSize(this.mImageSource, new int[] { this.mTilesHorizontal,
					this.mTilesHorizontal });
		} else {
			// No manager so load
			this.mTexture = bitmapTextureAtlas;
			this.mTexture.load();
		}
	}

	public TMXProperties<TMXTileProperty> getTMXTilePropertiesFromGlobalTileID(final int pGlobalTileID) {
		final int localTileID = pGlobalTileID - this.mFirstGlobalTileID;
		return this.mTMXTileProperties.get(localTileID);
	}

	public void addTMXTileProperty(final int pLocalTileID, final TMXTileProperty pTMXTileProperty) {
		final TMXProperties<TMXTileProperty> existingProperties = this.mTMXTileProperties.get(pLocalTileID);
		if (existingProperties != null) {
			existingProperties.add(pTMXTileProperty);
		} else {
			final TMXProperties<TMXTileProperty> newProperties = new TMXProperties<TMXTileProperty>();
			newProperties.add(pTMXTileProperty);
			this.mTMXTileProperties.put(pLocalTileID, newProperties);
			this.mTMXTilePropertiesKeys.add(pLocalTileID);
		}
	}

	public ITextureRegion getTextureRegionFromGlobalTileID(final int pGlobalTileID) {
		final int localTileID = pGlobalTileID - this.mFirstGlobalTileID;
		final int tileColumn = localTileID % this.mTilesHorizontal;
		final int tileRow = localTileID / this.mTilesHorizontal;

		final int texturePositionX = this.mMargin + (this.mSpacing + this.mTileWidth) * tileColumn;
		final int texturePositionY = this.mMargin + (this.mSpacing + this.mTileHeight) * tileRow;

		return new TextureRegion(this.mTexture, texturePositionX, texturePositionY, this.mTileWidth, this.mTileHeight);
	}

	private static int determineCount(final int pTotalExtent, final int pTileExtent, final int pMargin,
			final int pSpacing) {
		int count = 0;
		int remainingExtent = pTotalExtent;

		remainingExtent -= pMargin * 2;

		while (remainingExtent > 0) {
			remainingExtent -= pTileExtent;
			remainingExtent -= pSpacing;
			count++;
		}

		return count;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
