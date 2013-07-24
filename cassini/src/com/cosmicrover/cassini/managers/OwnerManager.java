package com.cosmicrover.cassini.managers;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.annotations.Mapper;
import com.cosmicrover.cassini.components.OwnerComponent;

public class OwnerManager extends Manager {
	@Mapper ComponentMapper<OwnerComponent> ownerMapper;
	
	@Override
	protected void initialize() {
		ownerMapper = ComponentMapper.getFor(OwnerComponent.class, world);
	}

	@Override
	public void added(Entity theEntity) {
		// Retrieve the owner components first
		OwnerComponent owner = ownerMapper.getSafe(theEntity);
		
		// Does this Entity have a OwnerComponent? then update its my UUID value
		if(owner != null) {
			// Remove the old group first
			world.getManager(GroupManager.class).remove(theEntity, owner.groupPrefix+owner.ownerOldUuid);
			
			// Update the UUID from its Entity for this location
			owner.myUuid = theEntity.getUuid().toString();
			
			// Add the new group next
			world.getManager(GroupManager.class).add(theEntity, owner.groupPrefix+owner.ownerUuid);
		}
	}

	@Override
	public void changed(Entity theEntity) {
		// Call our added method to do the job
		added(theEntity);
	}
}
