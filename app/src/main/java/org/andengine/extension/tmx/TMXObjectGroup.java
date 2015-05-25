package org.andengine.extension.tmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.andengine.extension.tmx.util.constants.TMXConstants;
import org.andengine.extension.tmx.util.constants.TMXObjectType;
import org.andengine.util.SAXUtils;
import org.xml.sax.Attributes;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 * 
 * @author Nicolas Gramlich
 * @since 11:20:49 - 29.07.2010
 */
public class TMXObjectGroup implements TMXConstants {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private final String mName;
	private final int mWidth;
	private final int mHeight;
	private final ArrayList<TMXObject> mTMXObjects = new ArrayList<TMXObject>();
	private final TMXProperties<TMXObjectGroupProperty> mTMXObjectGroupProperties = new TMXProperties<TMXObjectGroupProperty>();
	private TMXObjectType mObjectType = TMXObjectType.UNKNOWN;
	
	// ===========================================================
	// Constructors
	// ===========================================================

	public TMXObjectGroup(final Attributes pAttributes) {
		this.mName = pAttributes.getValue("", TMXConstants.TAG_OBJECTGROUP_ATTRIBUTE_NAME);
		this.mWidth = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_OBJECTGROUP_ATTRIBUTE_WIDTH);
		this.mHeight = SAXUtils.getIntAttributeOrThrow(pAttributes, TMXConstants.TAG_OBJECTGROUP_ATTRIBUTE_HEIGHT);
	}
	
	/**
	 * Copy Constructor
	 * @param pTMXObjectGroup {@link TMXObjectGroup} to copy
	 */
	public TMXObjectGroup(final TMXObjectGroup pTMXObjectGroup){
		this.mName = new String(pTMXObjectGroup.getName());
		this.mWidth = pTMXObjectGroup.getWidth();
		this.mHeight = pTMXObjectGroup.getHeight();
		for (TMXObject orignalObject : pTMXObjectGroup.getTMXObjects()) {
			this.mTMXObjects.add(new TMXObject(orignalObject));
		}
		for (TMXObjectGroupProperty originalTmxObjectGroupProperty : pTMXObjectGroup.getTMXObjectGroupProperties()) {
			this.mTMXObjectGroupProperties.add(new TMXObjectGroupProperty(originalTmxObjectGroupProperty));
		}
		this.mObjectType = pTMXObjectGroup.getObjectType();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public String getName() {
		return this.mName;
	}

	public int getWidth() {
		return this.mWidth;
	}

	public int getHeight() {
		return this.mHeight;
	}
	
	public TMXObjectType getObjectType(){
		return this.mObjectType;
	}

	void addTMXObject(final TMXObject pTMXObject) {
		this.mTMXObjects.add(pTMXObject);
	}

	public ArrayList<TMXObject> getTMXObjects() {
		return this.mTMXObjects ;
	}

	public void addTMXObjectGroupProperty(final TMXObjectGroupProperty pTMXObjectGroupProperty) {
		this.mTMXObjectGroupProperties.add(pTMXObjectGroupProperty);
	}

	public TMXProperties<TMXObjectGroupProperty> getTMXObjectGroupProperties() {
		return this.mTMXObjectGroupProperties;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Call to this to correctly know what this group consists of.
	 */
	public void checkType(){
		HashMap<TMXObjectType, Integer> counting = new HashMap<TMXObjectType, Integer>();

		for (TMXObject tmxObject : this.mTMXObjects) {
			TMXObjectType type = tmxObject.getObjectType();
			if(counting.containsKey(type)){
				int count = counting.get(type);
				count++;
				counting.put(type, count);
			}else{
				counting.put(type, 1);
			}
		}
		
		if(counting.size() > 1){
			this.mObjectType = TMXObjectType.MIXED;
		}else if(counting.size() <= 0){
			this.mObjectType = TMXObjectType.EMPTY;
		}else{
			//We can only have 1 object in the collection at this point!
			this.mObjectType = counting.entrySet().iterator().next().getKey();
		}
	}
	
	public int getSize(){
		return this.mTMXObjects.size();
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
