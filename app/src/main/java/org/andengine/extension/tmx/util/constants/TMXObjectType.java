package org.andengine.extension.tmx.util.constants;

import java.util.HashMap;
import java.util.Map;

import org.andengine.extension.tmx.TMXObject;
import org.andengine.extension.tmx.TMXObjectGroup;
import org.andengine.extension.tmx.util.exception.TMXObjectTypeException;

import android.R.integer;

/**
 * Stores what an object type is, helps to iterate through TMXObjects and understand 
 * what sort of object it is.
 * <br>The available types are:
 * <li>{@link #RECTANGLE}
 * <li>{@link #POLYGON}
 * <li>{@link #POLYLINE}
 * <li>{@link #TILEOBJECT}
 * <li>{@link #MIXED}
 * <li>{@link #UNKNOWN}
 * <li>{@link #EMPTY}
 * @author Paul Robinson
 *
 */
public enum TMXObjectType {
	RECTANGLE(0),
	POLYGON(1),
	POLYLINE(2),
	TILEOBJECT(3),
	/**
	 * For use in {@link TMXObjectGroup} as it can be mixed.
	 */
	MIXED(4),
	/**
	 * For use in {@link TMXObjectGroup} as a default value
	 */
	UNKNOWN(99),
	/**
	 * For use in {@link TMXObjectGroup} as it could be empty of {@link TMXObject}'s
	 */
	EMPTY(-1);

	/**
	 * This is used to find the ID of each TMXObjectType compared against a given ID
	 */
	private static final Map<Integer, TMXObjectType> lookup = new HashMap<Integer, TMXObjectType>();
	static{
		for (TMXObjectType pObject: TMXObjectType.values())
			lookup.put(pObject.getTMXObjectType(), pObject);
	}

	/**
	 * The TMXObjectType ID.
	 */
	private int mTMXObjectTpe;

	/**
	 * Constructor for the TMXObjectType 
	 * @param TMXObjectType the ID of the TMXObjectType.
	 */
	private TMXObjectType(int TMXObjectType){
		this.mTMXObjectTpe = TMXObjectType;
	}
	
	/**
	 * Get the ID of the current TMXObjectType.
	 * @return {@link integer} ID related to the current TMXObjectType
	 */
	public int getTMXObjectType() { 
		return mTMXObjectTpe;
	}

	/**
	 * Finds a TMXObjectType that is related to an ID, e.g ID 0 = SIMPLE
	 * @param pTMXObjectType {@link integer} The ID you wish to know, relates to which TMXObjectType.
	 * @return {@link TMXObjectType} The TMXObjectType of the given ID. 
	 * or NULL if the TMXObjectType does not exist.
	 * @throws TMXObjectTypeException When a given object (ID) is of the incorrect type
	 * and when if the given object (ID) is null;
	 */
	public static TMXObjectType get(int pTMXObjectType)throws TMXObjectTypeException{
		TMXObjectType value = null;
		try{
			value = lookup.get(pTMXObjectType);	
		}catch (ClassCastException CEE){
			throw new TMXObjectTypeException("Given value is incorect object type.  Should be integer", CEE);
		}catch (NullPointerException NPE){
			throw new TMXObjectTypeException("Given value is Null", NPE);
		}
		return value;
	}
}
