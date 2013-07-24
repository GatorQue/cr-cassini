package com.cosmicrover.core.components;

import com.artemis.Component;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Json.Serializable;

public abstract class AbstractComponent extends Component implements Serializable {
	/**
	 * This method will be called after restoring all Entities from a file and
	 * allows each entity to correct any Entity relationships that depend on
	 * the Entity Id values. This can happen if a component represents an array
	 * of sub-entities using the Id values.
	 * @param idMap map of oldId to newId's
	 */
	public void changeIds(IntIntMap idMap) {
		// Default implementation is to do nothing
	};
	
	/**
	 * This method will be called after restoring all Entities from a file and
	 * allows each entity to correct any Entity relationships that depend on
	 * the Entity UUID values. This can happen if a component represents an
	 * array of sub-entities using the UUID values.
	 * @param uuidMap
	 */
	public void changeUuids(ArrayMap<String,String> uuidMap) {
		// Default implementation is to do nothing
	};
}
