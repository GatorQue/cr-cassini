package com.cosmicrover.cassini.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.IntervalEntityProcessingSystem;
import com.artemis.utils.ImmutableBag;
import com.badlogic.gdx.math.MathUtils;
import com.cosmicrover.cassini.EntityFactory;
import com.cosmicrover.cassini.SpriteConstants;
import com.cosmicrover.cassini.components.RoverEventComponent;
import com.cosmicrover.cassini.components.RoverEventComponent.RoverEvent;
import com.cosmicrover.cassini.components.LocationComponent;
import com.cosmicrover.cassini.components.SpriteComponent;
import com.cosmicrover.cassini.managers.GroupManager;

public class RoverEventSystem extends IntervalEntityProcessingSystem {
	@Mapper ComponentMapper<RoverEventComponent> roverEventMapper;
	@Mapper ComponentMapper<LocationComponent> locationMapper;
	@Mapper ComponentMapper<SpriteComponent> spriteMapper;

	// EventQueueSystem interval for processing Events
	public static final float EVENT_PROCESSING_INTERVAL = 1.0f / 25.0f; // 25 Hz
	
	// GroupManager which is used to retrieve the sprites to draw
	private GroupManager groupManager = null;

	@SuppressWarnings("unchecked")
	public RoverEventSystem() {
		super(Aspect.getAspectForAll(
				RoverEventComponent.class,
				LocationComponent.class,
				SpriteComponent.class),
			  EVENT_PROCESSING_INTERVAL);
	}
	
	@Override
	protected void initialize() {
		// Retrieve the GroupManager object now
		groupManager = world.getManager(GroupManager.class);
	}

	@Override
	protected void process(Entity theEntity) {
		RoverEventComponent roverEvent = roverEventMapper.get(theEntity);
		LocationComponent location = locationMapper.get(theEntity);
		SpriteComponent sprite = spriteMapper.get(theEntity);
		
		// Subtract from nextEvent counter if currently enabled
		if(roverEvent.nextEvent > 0) {
			// TODO: Subtract/Add to rover's battery strength
			roverEvent.nextEvent--;
			
			if(roverEvent.scanInProgress) {
				roverEvent.scanAngle -= roverEvent.scanAngleStep;
			}
		}
		// See if we have other events to process
		else {
			// Did we have a scan in progress? then reveal the squares that were scanned
			if(roverEvent.scanInProgress) {
				// Remove the mask at the newly scanned location
				for(int z = 0; z<=5; z++) {
					for(float x = location.getMap().x-(5-z); x <= location.getMap().x+(5-z); x++) {
						for(float y = location.getMap().y-z; y <= location.getMap().y+z; y++) {
							RemoveMask(theEntity, x, y);
						}
					}
				}

				// Clear our scan in progress flag
				roverEvent.scanInProgress = false;
			}
			
			// Do we have events to process? then grab the first event now
			if(roverEvent.events.size > 0) {
				// Retrieve and remove the next event at the head of the queue
				RoverEvent anEvent = roverEvent.events.removeIndex(0);
	
				switch(anEvent.id) {
				case RoverEvent.EVENT_WAIT:
					// TODO: Add to rover's battery strength due to waiting
					
					// Wait for the specified event units in time
					roverEvent.nextEvent = anEvent.target;
					break;
				case RoverEvent.EVENT_LOOK:
					// TODO: Subtract from rover's battery strength for this look
					
					// Indicate a scan should be displayed
					roverEvent.scanInProgress = true;
					
					// Reset our scan angle
					roverEvent.scanAngle = MathUtils.PI2;
					
					// Reset our scan angle step
					roverEvent.scanAngleStep = MathUtils.PI2 / (float)anEvent.target;
					
					// Wait for the specified event units in time
					roverEvent.nextEvent = anEvent.target;
					break;
				case RoverEvent.EVENT_ROTATE:
					// TODO: Subtract from rover's battery strength for this rotation
					
					// Change the rover sprite to be shown
					ChangeSprite(sprite, anEvent.target);
	
					// Update the current direction of the rover
					roverEvent.direction = anEvent.target;
	
					// Specify when to perform our next event
					roverEvent.nextEvent = 2;
					break;
				case RoverEvent.EVENT_MOVE:
					// Are we facing the right way? if not rotate our direction first
					if(anEvent.target != roverEvent.direction) {
						// Re-add our move event to process later
						roverEvent.events.insert(0, anEvent);
	
						// Add rotation events
						roverEvent.events.insert(0, roverEvent.new RoverEvent(RoverEvent.EVENT_ROTATE,
								GetNextRotate(roverEvent.direction, anEvent.target)));
					} else {
						// TODO: Subtract from rover's battery strength for this move
						
						// Change the rover sprite to be shown
						ChangeSprite(sprite, anEvent.target);
						
						// Assign when the next event can occur
						roverEvent.nextEvent = 5;
					
						// Add to our location according to target direction
						ChangeLocation(location, anEvent.target);

						// Remove the mask at the new location
						RemoveMask(theEntity, location.getMap().x, location.getMap().y);
					}
					break;
				case RoverEvent.EVENT_DROP:
					// TODO: Subtract from rover's battery strength for this drop
					
				case RoverEvent.EVENT_DELIVER:
					// TODO: Subtract from rover's battery strength for this delivery
					
				case RoverEvent.EVENT_GRAB:
					// TODO: Subtract from rover's battery strength for this pickup
					
				case RoverEvent.EVENT_SHOOT:
					// TODO: Subtract from rover's battery strength for this shooting
					
				case RoverEvent.EVENT_TWEAK:
					// TODO: Subtract from rover's battery strength for this tweaking
					
					System.err.println("Unhandled event id="+anEvent.id+" target="+anEvent.target);
					break;
				default:
					System.err.println("Unknown event id="+anEvent.id);
					break;
				} // switch(anEvent.id)
	
			} // if(eventQueue.events.size > 0)
		}
	}

	private int GetNextRotate(int theCurDirection, int theNextDirection) {
		// Normalize the current direction to be the 0th direction
		// (so we only need to test one case later)
		int anTarget = -(((8 - theCurDirection) + theNextDirection) % 8);

		// Using the single case, see which direction to change direction
		if( anTarget < 0 && anTarget > -5) {
			anTarget = (theCurDirection + 1) % 8;
		} else {
			anTarget = (8 + theCurDirection - 1) % 8;
		}

		// Return the direction we should be rotating to next
		return anTarget;
	}
	
	// Change the sprite for the rover
	private void ChangeSprite(SpriteComponent sprite, int direction) {
		switch(direction) {
		case RoverEvent.DIR_N:
			sprite.name = SpriteConstants.ROVER_NORTH;
			break;
		case RoverEvent.DIR_E:
			sprite.name = SpriteConstants.ROVER_EAST;
			break;
		case RoverEvent.DIR_W:
			sprite.name = SpriteConstants.ROVER_WEST;
			break;
		case RoverEvent.DIR_S:
			sprite.name = SpriteConstants.ROVER_SOUTH;
			break;
		case RoverEvent.DIR_NE:
			sprite.name = SpriteConstants.ROVER_NORTH_EAST;
			break;
		case RoverEvent.DIR_SE:
			sprite.name = SpriteConstants.ROVER_SOUTH_EAST;
			break;
		case RoverEvent.DIR_NW:
			sprite.name = SpriteConstants.ROVER_NORTH_WEST;
			break;
		case RoverEvent.DIR_SW:
			sprite.name = SpriteConstants.ROVER_SOUTH_WEST;
			break;
		}
	}
	
	// Change the location for the rover
	private void ChangeLocation(LocationComponent location, int direction) {
		// Should be similar to this:
		// location.addMap(1.25f*cos(direction*MathUtils.PI / 4), 1.25f*sin(direction*MathUtils.PI / 4));
		switch(direction) {
		case RoverEvent.DIR_N:
			location.addMap(0,1);
			break;
		case RoverEvent.DIR_E:
			location.addMap(1,0);
			break;
		case RoverEvent.DIR_W:
			location.addMap(-1,0);
			break;
		case RoverEvent.DIR_S:
			location.addMap(0,-1);
			break;
		case RoverEvent.DIR_NE:
			location.addMap(1, 1);
			break;
		case RoverEvent.DIR_SE:
			location.addMap(1, -1);
			break;
		case RoverEvent.DIR_NW:
			location.addMap(-1, 1);
			break;
		case RoverEvent.DIR_SW:
			location.addMap(-1, -1);
			break;
		}
	}
	
	// Remove the mask at the location specified if it exists
	private void RemoveMask(Entity theEntity, float x, float y) {
		// Retrieve the location component for this entity
		LocationComponent location = locationMapper.get(theEntity);

		// Retrieve the masks for theEntity here
		ImmutableBag<Entity> masks = groupManager.getEntities(
				EntityFactory.MASK_GROUP+
				location.getContextTag()+
				theEntity.getUuid());

		// Clear the mask for the location we are about to move to
		for(int i = 0, s = masks.size(); s > i; i++) {
			// Retrieve the Entity by index
			Entity anMaskEntity = masks.get(i);
			
			// Retrieve the position and sprite components for this sprite
			LocationComponent locationMask = locationMapper.get(anMaskEntity);
			
			if((int)locationMask.getMap().x == (int)x &&
			   (int)locationMask.getMap().y == (int)y) {
				// Remove this mask from the world
				anMaskEntity.deleteFromWorld();

				// TODO: Add to exploration score for each mask removed
			}
		}
	}
}
