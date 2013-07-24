package com.cosmicrover.cassini.managers;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.annotations.Mapper;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.utils.ArrayMap;
import com.cosmicrover.cassini.components.GroupComponent;

public class GroupManager extends Manager {
	@Mapper ComponentMapper<GroupComponent> groupMapper;
	private ArrayMap<String, Bag<Entity>> entitiesByGroup;
	private ArrayMap<Entity, Bag<String>> groupsByEntity;

	public GroupManager() {
	}
	

	@Override
	protected void initialize() {
		entitiesByGroup = new ArrayMap<String, Bag<Entity>>();
		groupsByEntity = new ArrayMap<Entity, Bag<String>>();
		groupMapper = ComponentMapper.getFor(GroupComponent.class, world);
	}

	
	/**
	 * Set the group of the entity.
	 * 
	 * @param theEntity to add into the group.
	 * @param theGroup to add theEntity into.
	 */
	public void add(Entity theEntity, String theGroup) {
		// Add the group to our Bag of groups for theEntity provided first
		Bag<String> groups = groupsByEntity.get(theEntity);
		if(groups == null) {
			groups = new Bag<String>();
			groupsByEntity.put(theEntity, groups);
		}
		groups.add(theGroup);

		// Attempt to retrieve the GroupComponent for theEntity provided
		GroupComponent groupComponent = groupMapper.getSafe(theEntity);

		// Group component doesn't exist, add the GroupComponent now
		if(groupComponent == null) {
			// Create the group component, add the group, and add the component
			// to the Entity and indicate that it has changed in the world
			groupComponent = new GroupComponent();
			groupComponent.add(theGroup);
			theEntity.addComponent(groupComponent);
			theEntity.changedInWorld();
		}
		// Otherwise just add the group to the existing GroupComponent
		else {
			// If its not already in theGroup then add it now
			if(!groupComponent.isInGroup(theGroup)) {
				groupComponent.add(theGroup);
			}
		}
		
		// Add theEntity to our Bag of entities for theGroup provided last
		Bag<Entity> entities = entitiesByGroup.get(theGroup);
		if(entities == null) {
			entities = new Bag<Entity>();
			entitiesByGroup.put(theGroup, entities);
		}
		entities.add(theEntity);
	}
	
	/**
	 * Remove the entity from the specified group.
	 * @param theEntity to remove from the group
	 * @param theGroup to remove theEntity from
	 */
	public void remove(Entity theEntity, String theGroup) {
		// Add GroupComponent to theEntity if it doesn't already have it
		GroupComponent groupComponent = groupMapper.getSafe(theEntity);

		// If group component exists then remove theGroup from it
		if(groupComponent != null) {
			groupComponent.remove(theGroup);
		}
		
		// Remove theEntity from our Bag of Entities by theGroup provided
		Bag<Entity> entities = entitiesByGroup.get(theGroup);
		if(entities != null) {
			entities.remove(theEntity);
		}
		
		// Remove theGroup from our Bag of Groups by theEntity provided
		Bag<String> groups = groupsByEntity.get(theEntity);
		if(groups != null) {
			groups.remove(theGroup);
		}
	}

	/**
	 * Add theEntity to all the groups specified by its GroupComponent if it
	 * has one.
	 * @param theEntity to add all the groups from its GroupComponent
	 */
	public void addToAllGroups(Entity theEntity) {
		// See if theEntity has a GroupComponent
		GroupComponent groupComponent = groupMapper.getSafe(theEntity);

		if(groupComponent != null) {
			String[] groups = groupComponent.getGroups();
			for(String group : groups) {
				// Call our other method to do the actual adding
				add(theEntity, group);
			}
		}
	}

	/**
	 * Remove theEntity provided from all the groups
	 * @param theEntity to be removed from all the groups
	 */
	public void removeFromAllGroups(Entity theEntity) {
		Bag<String> groups = groupsByEntity.get(theEntity);
		if(groups != null) {
			// The remove method will cause size to shrink as call it 
			while(groups.size() > 0) {
				// Retrieve the group name
				String group = groups.get(0);
				
				// Call the other method to do the work of removing the group
				remove(theEntity, group);
			}
			// Make sure our groups are cleared for theEntity provided
			groups.clear();
		}
	}
	
	/**
	 * Get all entities that belong to theGroup name provided.
	 * @param theGroup name of the group.
	 * @return read-only bag of entities belonging to the group.
	 */
	public ImmutableBag<Entity> getEntities(String theGroup) {
		Bag<Entity> entities = entitiesByGroup.get(theGroup);
		if(entities == null) {
			entities = new Bag<Entity>();
			entitiesByGroup.put(theGroup, entities);
		}
		return entities;
	}
	
	/**
	 * Retrieves the groups that theEntity provided belongs to
	 * @param theEntity to find the groups for
	 * @return the groups theEntity belongs to, null if none.
	 */
	public ImmutableBag<String> getGroups(Entity theEntity) {
		return groupsByEntity.get(theEntity);
	}
	
	/**
	 * Checks if theEntity belongs to any group.
	 * @param theEntity to check.
	 * @return true if it is in any group, false if none.
	 */
	public boolean isInAnyGroup(Entity theEntity) {
		return getGroups(theEntity) != null;
	}
	
	/**
	 * Check if theEntity is in theGroup supplied.
	 * @param theGroup to check in.
	 * @param theEntity to check for.
	 * @return true if theEntity is in theGroup supplied, false if not.
	 */
	public boolean isInGroup(Entity theEntity, String theGroup) {
		if(theGroup != null) {
			Bag<String> groups = groupsByEntity.get(theEntity);
			for(int i = 0; groups.size() > i; i++) {
				String group = groups.get(i);
				if(theGroup == group || theGroup.equals(group)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void added(Entity theEntity) {
		// See if theEntity has a GroupComponent
		GroupComponent group = groupMapper.getSafe(theEntity);

		// GroupComponent exists? then add its groups now
		if(group != null) {
			addToAllGroups(theEntity);
		}
	}

	@Override
	public void changed(Entity theEntity) {
		// See if theEntity has a GroupComponent
		GroupComponent group = groupMapper.getSafe(theEntity);
		Bag<String> groups = groupsByEntity.get(theEntity);

		// GroupComponent exists? then add its groups now
		if(group != null && (groups == null || groups.isEmpty())) {
			addToAllGroups(theEntity);
		}
		// GroupComponent was removed but existed previously? then remove all
		// groups for theEntity provided
		else if(group == null && groups != null && !groups.isEmpty()) {
			removeFromAllGroups(theEntity);
		}
	}

	@Override
	public void deleted(Entity theEntity) {
		removeFromAllGroups(theEntity);
	}
}
