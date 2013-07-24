package com.cosmicrover.cassini.managers;

import com.artemis.Entity;
import com.artemis.Manager;
import com.badlogic.gdx.utils.Array;

public class PersistenceManager extends Manager {
	private Array<Entity> entities;
	
	@Override
	protected void initialize() {
		entities = new Array<Entity>();
	}
	
	public Entity[] getEntities() {
		return entities.toArray(Entity.class);
	}
	
	public void removeAll() {
		// Get a local copy of every entity
		Entity[] anEntities = entities.toArray(Entity.class);

		// Delete every entity from the world
		for(Entity anEntity : anEntities) {
			anEntity.deleteFromWorld();
		}
		
		// Make sure our list of entities is empty
		entities.clear();
	}

	@Override
	public void added(Entity theEntity) {
		entities.add(theEntity);
		super.added(theEntity);
	}

	@Override
	public void deleted(Entity theEntity) {
		if(entities.contains(theEntity, true)) {
			entities.removeValue(theEntity, true);
		}
		super.deleted(theEntity);
	}
}
