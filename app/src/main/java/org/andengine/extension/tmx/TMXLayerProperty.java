package org.andengine.extension.tmx;

import org.xml.sax.Attributes;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 18:48:46 - 12.10.2010
 */
public class TMXLayerProperty extends TMXProperty {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public TMXLayerProperty(final Attributes pAttributes) {
		super(pAttributes);
	}
	
	/**
	 * Copy constructor
	 * @param pTMXLayerProperty {@link TMXLayerProperty} to copy
	 */
	public TMXLayerProperty(final TMXLayerProperty pTMXLayerProperty){
		super(pTMXLayerProperty);
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
