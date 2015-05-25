package org.andengine.extension.tmx;

import org.xml.sax.Attributes;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 18:48:41 - 12.10.2010
 */
public class TMXTiledMapProperty extends TMXProperty {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public TMXTiledMapProperty(final Attributes pAttributes) {
		super(pAttributes);
	}
	
	/**
	 * Copy constructor
	 * @param pTMXTiledMapProperty Original {@link TMXTiledMapProperty} to copy
	 */
	public TMXTiledMapProperty(final TMXTiledMapProperty pTMXTiledMapProperty){
		super(pTMXTiledMapProperty);
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

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
