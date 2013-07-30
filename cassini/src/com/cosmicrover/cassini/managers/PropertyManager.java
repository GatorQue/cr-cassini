package com.cosmicrover.cassini.managers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.annotations.Mapper;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.utils.ArrayMap;
import com.cosmicrover.cassini.components.PropertyComponent;
import com.cosmicrover.cassini.components.PropertyComponent.Type;

public class PropertyManager extends Manager {
	@Mapper ComponentMapper<PropertyComponent> propertyMapper;
	// A list of entities by property type
	private ArrayMap<Type, Bag<Entity>> entitiesByType;
	private Map<String, Entity> entitiesByTag;

	@Override
	protected void initialize() {
		entitiesByType = new ArrayMap<Type, Bag<Entity>>();
		entitiesByTag = new HashMap<String, Entity>();
		propertyMapper = ComponentMapper.getFor(PropertyComponent.class, world);
	}

	public void addTag(Entity theEntity, String theTag) {
		entitiesByTag.put(theTag, theEntity);		
	}

	/**
	 * Retrieves the Entity that has been assigned to theTag specified.
	 * @param theTag to find the Entity for
	 * @return the Entity assigned to theTag specified
	 */
	public Entity getEntityByTag(String theTag) {
		return entitiesByTag.get(theTag);
	}
	
	/**
	 * Get all entities that belong to theType name provided.
	 * @param theType to find all Entities for
	 * @return read-only bag of entities belonging to theType specified.
	 */
	public ImmutableBag<Entity> getEntitiesByType(Type theType) {
		Bag<Entity> entities = entitiesByType.get(theType);
		if(entities == null) {
			entities = new Bag<Entity>();
			entitiesByType.put(theType, entities);
		}
		return entities;
	}
	
	public void updateTag(Entity theEntity, String theOldTag) {
		// See if theEntity has a PropertyComponent
		PropertyComponent property = propertyMapper.getSafe(theEntity);
		
		// PropertyComponent exists? then add its tag now
		if(property != null) {
			// Remove the old tag
			removeTag(theOldTag);
			// Add the new tag
			addTag(theEntity, property.tag);
		}
	}

	public void updateType(Entity theEntity, Type theOldType) {
		// See if theEntity has a PropertyComponent
		PropertyComponent property = propertyMapper.getSafe(theEntity);
		
		// PropertyComponent exists? then add its type and tag now
		if(property != null) {
			// Remove the old type
			removeType(theEntity, theOldType);
			// Add the new type
			addType(theEntity, property.type);
		}
	}

	@Override
	public void added(Entity theEntity) {
		// See if theEntity has a PropertyComponent
		PropertyComponent property = propertyMapper.getSafe(theEntity);

		// PropertyComponent exists? then add its type and tag now
		if(property != null) {
			addTag(theEntity, property.tag);
			addType(theEntity, property.type);
		}
	}

	@Override
	public void changed(Entity theEntity) {
		// See if theEntity has a PropertyComponent
		PropertyComponent property = propertyMapper.getSafe(theEntity);
		
		// PropertyComponent exists? then add its type and tag now
		if(property != null) {
			addTag(theEntity, property.tag);
			addType(theEntity, property.type);
		}
		// Remove theEntity from any tags and types list
		else {
			removeFromAllTags(theEntity);
			removeFromAllTypes(theEntity);
		}
	}

	@Override
	public void deleted(Entity theEntity) {
		// See if theEntity has a PropertyComponent
		PropertyComponent property = propertyMapper.getSafe(theEntity);
		
		// PropertyComponent exists? then remove its tag now
		if(property != null) {
			removeTag(property.tag);
		}
		removeFromAllTags(theEntity);
		removeFromAllTypes(theEntity);
	}
	
	private void removeTag(String theTag) {
		entitiesByTag.remove(theTag);
	}

	/**
	 * Set the type for the entity
	 * 
	 * @param theEntity to add into the group.
	 * @param theGroup to add theEntity into.
	 */
	private void addType(Entity theEntity, Type theType) {
		// Add theEntity to our Bag of entities for theType provided last
		Bag<Entity> entities = entitiesByType.get(theType);
		if(entities == null) {
			entities = new Bag<Entity>();
			entitiesByType.put(theType, entities);
		}
		// Only add theEntity if it isn't already in this list
		if(!entities.contains(theEntity)) {
			entities.add(theEntity);
		}
	}
	
	/**
	 * Remove the entity from the specified type list.
	 * @param theEntity to remove from the type
	 * @param theType to remove theEntity from
	 */
	private void removeType(Entity theEntity, Type theType) {
		// Remove theEntity from our Bag of Entities by theType provided
		Bag<Entity> entities = entitiesByType.get(theType);
		if(entities != null) {
			entities.remove(theEntity);
		}
	}
	
	private void removeFromAllTags(Entity theEntity) {
	    Set<String> set = entitiesByTag.keySet();
	    Iterator<String> itr = set.iterator();
	    while (itr.hasNext())
	    {
	    	Entity anEntity = entitiesByTag.get(itr.next());
	    	if(anEntity == theEntity) {
	    		itr.remove();
	    	}
	    }
	}
	
	/**
	 * Remove theEntity provided from all the types
	 * @param theEntity to be removed from all the types
	 */
	public void removeFromAllTypes(Entity theEntity) {
		for(int i = 0, s = entitiesByType.size; s > i; i++) {
			Bag<Entity> entities = entitiesByType.getValueAt(i);
			if(entities != null && entities.contains(theEntity)) {
				entities.remove(theEntity);
			}
		}
	}
}
