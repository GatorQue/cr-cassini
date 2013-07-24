package com.cosmicrover.cassini.managers;

import java.util.HashMap;
import java.util.Map;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Manager;
import com.artemis.annotations.Mapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.cosmicrover.cassini.components.CameraComponent;
import com.cosmicrover.cassini.components.ViewportComponent;

public class ViewportManager extends Manager {
	@Mapper ComponentMapper<CameraComponent> cameraMapper;
	@Mapper ComponentMapper<ViewportComponent> viewportMapper;

	/// Map of entities by Viewport ID assigned
	private Map<Integer, Entity> entityById;

	/// Map of Viewport ID's by entity
	private Map<Entity, Integer> idByEntity;

	/// Size in X to divide screen width by
	private int sizeX = 1;
	
	/// Size in Y to divide screen height by
	private int sizeY = 1;
	
	/// Previous size of entityById
	private int prevSize = 0;
	
	/// Last size of entityById
	private int nextSize = 1;
	
	public ViewportManager() {
		entityById = new HashMap<Integer, Entity>();
		idByEntity = new HashMap<Entity, Integer>();
	}

	public void updateViewports() {
		// Loop through each entity in our map by ID and adjust the viewport
		// values according to sizeX and sizeY values adjusted above.
		for(Entity anEntity : entityById.values()) {
			// Retrieve the viewport component for this entity
			ViewportComponent viewport = viewportMapper.get(anEntity);

			// Retrieve the camera component for this entity
			CameraComponent camera = cameraMapper.get(anEntity);

			// Retrieve the ID for this entity (and subtract one)
			Integer anId = idByEntity.get(anEntity) - 1;
			
			// Adjust the width and height for each viewport
			viewport.width = Gdx.graphics.getWidth() / sizeX;
			viewport.height = Gdx.graphics.getHeight() / sizeY;
			
			// Adjust the x and y position using the ID value
			viewport.x = (anId%sizeX)*viewport.width;
			viewport.y = ((sizeY-1)-(anId/sizeX))*viewport.height;
			
			// If camera component exists and HUD camera exists then update now
			if(camera != null) {
				if (camera.hasHudCamera()) {
					// Make a note of the old camera position for repositioning the camera later
					Vector3 oldpos = new Vector3(camera.getHudCamera().position);
					   
					// Update our HUD camera component according to viewport width and height
					camera.getHudCamera().setToOrtho(false, viewport.width, viewport.height);
	
					// Translate the old camera position with the current camera position
					camera.getHudCamera().translate(
							oldpos.x-camera.getHudCamera().position.x,
							oldpos.y-camera.getHudCamera().position.y);
	
					// Apply the changes now to our HUD camera component
					camera.getHudCamera().update();
					camera.getHudCamera().apply(Gdx.gl10);
				}
				if(camera.hasWorldCamera()) {
					// Make a note of the old camera position for repositioning the camera later
					Vector3 oldpos = new Vector3(camera.getWorldCamera().position);
					   
					// Update our World camera component according to viewport width and height
					camera.getWorldCamera().setToOrtho(false, viewport.width, viewport.height);

					// Translate the old camera position with the current camera position
					camera.getWorldCamera().translate(
							oldpos.x-camera.getWorldCamera().position.x,
							oldpos.y-camera.getWorldCamera().position.y);

					// Apply the changes now to our World camera component
					camera.getWorldCamera().update();
					camera.getWorldCamera().apply(Gdx.gl10);
				}
			}

		} // for(Entity anEntity : entityById.values()) {
	}

	@Override
	protected void initialize() {
		cameraMapper = ComponentMapper.getFor(CameraComponent.class, world);
		viewportMapper = ComponentMapper.getFor(ViewportComponent.class, world);
	}

	@Override
	public void added(Entity theEntity) {
		// Retrieve the viewport component first
		ViewportComponent viewport = viewportMapper.getSafe(theEntity);
		
		// Does this Entity have a ViewportComponent? then add it to our list
		if(viewport != null && !idByEntity.containsKey(theEntity)) {
			// Find the next available ID we can use
			Integer anId = findId();
			
			// Add the new Entity to our list using the ID assigned
			entityById.put(anId, theEntity);
			
			// Add the new ID to our list using theEntity provided
			idByEntity.put(theEntity, anId);
		}
		
		// Do we need to recalculate the viewport regions?
		if(entityById.size() > nextSize) {
			// Update the size/number (expand them) of our viewports
			updateSize();
		}
		
		// Update the viewport components values for each registered entity
		updateViewports();

		// Call our parent class
		super.added(theEntity);
	}

	@Override
	public void changed(Entity theEntity) {
		// Retrieve the viewport component first
		ViewportComponent viewport = viewportMapper.getSafe(theEntity);

		// No viewport but use to have one as shown by being in our map, then
		// remove it by calling our deleted method now
		if(viewport == null && idByEntity.containsKey(theEntity)) {
			deleted(theEntity);
		}
		// Has a viewport but we don't have any record of it, then add it by
		// calling our added method now.
		else if(viewport != null && !idByEntity.containsKey(theEntity)) {
			added(theEntity);
		}

		// Update the viewport components values for each registered entity
		updateViewports();

		// Call our parent class
		super.changed(theEntity);
	}

	@Override
	public void deleted(Entity theEntity) {
		if(idByEntity.containsKey(theEntity)) {
			// Retrieve the ID assigned to this Entity
			Integer anId = idByEntity.get(theEntity);
			
			// Remove theEntity using the ID as the key
			entityById.remove(anId);
			
			// Remove the ID using theEntity as the key
			idByEntity.remove(theEntity);
		}
		
		// Do we need to recalculate the viewport regions?
		if(entityById.size() <= prevSize && prevSize > 0) {
			// Update the size/number (shrink them) of our viewports
			updateSize();
			
			// When shrinking, we need to update our ID's
			updateIds();
		}

		// Update the viewport components values for each registered entity
		updateViewports();

		// Call our parent class
		super.deleted(theEntity);
	}

	private Integer findId() {
		// Default to an unknown/invalid ID value of 0
		Integer anId = 0;

		// Find the ID to use for this viewport
		for(int i = 1; i <= entityById.size(); i++) {
			// This ID is available? then claim it now
			if(!entityById.containsKey(i)) {
				// Claim the missing ID
				anId = i;
			}
		}
		
		// Did we not come up with an ID, then just take the next ID available
		if(anId == 0) {
			// Find the next available ID
			anId = entityById.size() + 1;
		}
		
		// Return the ID found above
		return anId;
	}
	
	private void updateIds() {
		// Next expected ID
		Integer anNextId = 1;
		
		// Loop through each entity in our map by ID and adjust the viewport
		// values according to sizeX and sizeY values adjusted above.
		for(Entity anEntity : entityById.values()) {
			// Retrieve the ID for this entity (and subtract one)
			Integer anId = idByEntity.get(anEntity);
			
			// Is this entities ID bigger than the one expected?
			if(anId > anNextId) {
				// First remove this Entity from our lists
				entityById.remove(anId);
				idByEntity.remove(anEntity);
				
				// Find the next available ID for this entity
				anId = findId();
				
				// Add this entity back into our lists
				entityById.put(anId, anEntity);
				idByEntity.put(anEntity, anId);
			}
			
			// Increment our next expected ID
			anNextId++;
		}
	}

	private void updateSize() {
		// Do we have less than we did before? then shrink our sizeX and sizeY values
		if(entityById.size() <= prevSize && prevSize > 0) {
			// Archive our current previous size as the next size
			nextSize = prevSize;
			
			// Bigger? then subtract from sizeX, otherwise subtract from sizeY
			if(sizeX > sizeY) {
				// Decrease size X first
				sizeX--;
				
				// Compute the next prevSize to use
				prevSize = sizeX*(sizeY-1);
			} else {
				// Decrease size Y first
				sizeY--;

				// Compute the next prevSize to use
				prevSize = sizeY*(sizeX-1);
			}
		}
		// Otherwise increase our sizeX and sizeY values
		else if (entityById.size() > nextSize) {
			// Equal? then add to sizeX, otherwise add to sizeY
			if(sizeX == sizeY) {
				// Expand sizeX before sizeY
				sizeX++;
			} else {
				// Expand sizeY to equal sizeX
				sizeY++;
			}

			// Archive our current next size as the previous size
			prevSize = nextSize;

			// Set our nextSize value according to the number of viewports available now
			nextSize = sizeX*sizeY;
		}
	}
}
