package com.cosmicrover.cassini.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.utils.ImmutableBag;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.cosmicrover.cassini.components.CameraComponent;
import com.cosmicrover.cassini.components.LocationComponent;
import com.cosmicrover.cassini.components.RoverEventComponent;
import com.cosmicrover.cassini.components.RoverEventComponent.RoverEvent;
import com.cosmicrover.cassini.components.RoverInputComponent;
import com.cosmicrover.cassini.components.ViewportComponent;

public class RoverInputSystem extends EntityProcessingSystem implements InputProcessor {
	@Mapper ComponentMapper<CameraComponent> cameraMapper;
	@Mapper ComponentMapper<RoverEventComponent> roverEventMapper;
	@Mapper ComponentMapper<RoverInputComponent> roverInputMapper;
	@Mapper ComponentMapper<LocationComponent> locationMapper;
	@Mapper ComponentMapper<ViewportComponent> viewportMapper;
	
	// Mouse/Touch Sensitivity between a touch and a touch and drag event
	private static final float TOUCH_SENSITIVITY = 2.0f;  // Two world units
	
	// Drag/Pan Camera velocity limit
	private static final float DRAG_LIMIT = 20.0f;
	
	// Keyboard pan step
	private static final float KEYBOARD_PAN_STEP = 4.0f;
	
	// Zoom minimum limit
	private static final float ZOOM_MINIMUM = 0.25f;
	
	// Zoom maximum limit
	private static final float ZOOM_MAXIMUM = 4.00f;
	
	// Zoom step size
	private static final float ZOOM_STEP_SIZE = 0.05f;
	
	// Last move position while not in newDown event
	public final Vector3 lastMovePos;
	
	@SuppressWarnings("unchecked")

	public RoverInputSystem() {
    	super(Aspect.getAspectForAll(
    			CameraComponent.class,
    			RoverEventComponent.class,
    			LocationComponent.class,
    			RoverInputComponent.class,
    			ViewportComponent.class));
    	lastMovePos = new Vector3();
    }
    
	@Override
	protected void process(Entity theEntity) {
		RoverInputComponent roverInput = roverInputMapper.get(theEntity);
		
		// Handle keyboard events (convert to mouse/touch events)
		handleKeyboard(theEntity);
		
		// Did the player just touch the screen (without moving around)?
		if(roverInput.newDown && roverInput.newUp &&
		   roverInput.lastDownPos.epsilonEquals(roverInput.lastUpPos, TOUCH_SENSITIVITY)) {
			// Handle tap related stuff
			handleTap(theEntity);

			// Reset our new down, key down, and up event flags now
			roverInput.newKeyDown = false;
			roverInput.newDown = false;
			roverInput.newUp = false;
		}
		// Did the player drag the mouse?
		else if(roverInput.newDrag && !roverInput.newUp) {
			// Handle drag related stuff
			handleDrag(theEntity);

			// Reset our new drag event flag now
			roverInput.newDrag = false;
		}
		// Handle newUp event occurring after drag has completed
		else if(roverInput.newUp) {
			// Handle up related stuff
			handleUp(theEntity);

			// Reset our new down, key down, and up event flags now
			roverInput.newKeyDown = false;
			roverInput.newDown = false;
			roverInput.newUp = false;
		}
		
		// Handle newZoom event
		if(roverInput.newZoom) {
			// Call handle zoom to handle the event
			handleZoom(theEntity);
			
			// Reset our new zoom event flag now
			roverInput.newZoom = false;
		}
	}
	
	private void handleKeyboard(Entity theEntity) {
		// Retrieve the position component for our input player
		CameraComponent camera = cameraMapper.get(theEntity);
		LocationComponent location = locationMapper.get(theEntity);
		RoverEventComponent roverEvent = roverEventMapper.get(theEntity);
		RoverInputComponent roverInput = roverInputMapper.get(theEntity);
		ViewportComponent viewport = viewportMapper.get(theEntity);

		// Keep track of our repeat interval for keyboard input
		roverInput.keyRepeatAccumulator += world.getDelta();
		if(roverInput.keyRepeatAccumulator >= roverInput.keyRepeatInterval || roverInput.keyRepeatForce) {
			roverInput.keyRepeatAccumulator -= roverInput.keyRepeatInterval;
			roverInput.keyRepeat = true;
			roverInput.keyRepeatForce = false;
		} else {
			roverInput.keyRepeat = false;
		}
		
		// Center key will center the camera on the camera input position
		if(true == roverInput.keyCenter) {
			// No down event in progress? then create new down event (that we own)
			if(false == roverInput.newDown && false == roverInput.newKeyDown && roverEvent.events.size == 0) {
				// Create a down event using the input players position to start with
				roverInput.lastDownPos.set(location.getLevel().x, location.getLevel().y, 0);
				camera.getWorldCamera().project(roverInput.lastDownPos, viewport.x, viewport.y, viewport.width, viewport.height);
				roverInput.newDown = true;
				roverInput.newKeyDown = true;
				
				// Refocus our camera on the player
				camera.setWorldPosition(location.getLevel().x, location.getLevel().y);
			}
			// Down event in progress that belongs to us? then create new up event
			else if(true == roverInput.newDown && true == roverInput.newKeyDown) {
				// Only one or no movement path coordinates were added? then
				// treat this like a tap event on our input position
				if(roverEvent.movePath.size <= 1) {
					// Set our input players position as the up position
					roverInput.lastUpPos.set(location.getLevel().x, location.getLevel().y, 0);
				}
				// Otherwise use the last movement path coordinate as the up position
				else {
					roverInput.lastUpPos.set(roverEvent.movePath.peek().x, roverEvent.movePath.peek().y, 0);
				}
				camera.getWorldCamera().project(roverInput.lastUpPos, viewport.x, viewport.y, viewport.width, viewport.height);
				roverInput.newUp = true;
				roverInput.newKeyDown = false;
			}

			// Clear our key center event now
			roverInput.keyCenter = false;
		}
		
		// Process the other keys during key down events
		if((roverInput.keyLeft || roverInput.keyRight || roverInput.keyUp || roverInput.keyDown) &&
		   (true == roverInput.newKeyDown)) {
			// First time for the current key down event? then use the input players
			// position as the initial drag position
			if(roverEvent.movePath.size == 0) {
				roverInput.lastDragPos.set(location.getLevel().x, location.getLevel().y, 0);
				roverInput.keyRepeatForce = true;
			}
			// Otherwise use the last movement path coordinate as the drag position 
			else if (roverInput.keyRepeat) {
				roverInput.lastDragPos.set(location.getMapAsLevel(roverEvent.movePath.peek()));
				if(true == roverInput.keyLeft) {
					roverInput.lastDragPos.add(-location.getLevelGrid().x+location.getLevelOffsetCenter().x,
							location.getLevelOffsetCenter().y,0);
					roverInput.lastDragDelta.set(-location.getLevelGrid().x, 0, 0);
				}
				if(true == roverInput.keyRight) {
					roverInput.lastDragPos.add(location.getLevelGrid().x+location.getLevelOffsetCenter().x,
							location.getLevelOffsetCenter().y,0);
					roverInput.lastDragDelta.set(location.getLevelGrid().x, 0, 0);
				}
				if(true == roverInput.keyUp) {
					roverInput.lastDragPos.add(location.getLevelOffsetCenter().x,
							location.getLevelGrid().y+location.getLevelOffsetCenter().y,0);
					roverInput.lastDragDelta.set(0, location.getLevelGrid().y, 0);
				}
				if(true == roverInput.keyDown) {
					roverInput.lastDragPos.add(location.getLevelOffsetCenter().x,
							-location.getLevelGrid().y+location.getLevelOffsetCenter().y,0);
					roverInput.lastDragDelta.set(0, -location.getLevelGrid().y, 0);
				}
			}
			roverInput.newDrag = true;
		}
		// User wants to pan with the keyboard (didn't press space/5/DPAD Center first)
		else if((roverInput.keyLeft || roverInput.keyRight || roverInput.keyUp || roverInput.keyDown) &&
				(false == roverInput.newDown)) {
			// Treat this as a panning only drag event
			if(true == roverInput.keyLeft) {
				roverInput.lastDragDelta.x = -KEYBOARD_PAN_STEP;
			}
			else if(false == roverInput.keyLeft && roverInput.lastDragDelta.x < 0) {
				roverInput.lastDragDelta.x = 0;
			}
			if(true == roverInput.keyRight) {
				roverInput.lastDragDelta.x = KEYBOARD_PAN_STEP;
			}
			else if(false == roverInput.keyRight && roverInput.lastDragDelta.x > 0) {
				roverInput.lastDragDelta.x = 0;
			}
			if(true == roverInput.keyUp) {
				roverInput.lastDragDelta.y = KEYBOARD_PAN_STEP;
			}
			else if(false == roverInput.keyUp && roverInput.lastDragDelta.y > 0) {
				roverInput.lastDragDelta.y = 0;
			}
			if(true == roverInput.keyDown) {
				roverInput.lastDragDelta.y = -KEYBOARD_PAN_STEP;
			}
			else if(false == roverInput.keyDown && roverInput.lastDragDelta.y < 0) {
				roverInput.lastDragDelta.y = 0;
			}
			roverInput.newDrag = true;
		} // if(true == newKeyDown)

		// Handle keyboard zoom events
		if(true == roverInput.keyPlus && false == roverInput.newZoom) {
			roverInput.zoomValue = -1;
			roverInput.newZoom = true;
		}
		if(true == roverInput.keyMinus && false == roverInput.newZoom) {
			roverInput.zoomValue = 1;
			roverInput.newZoom = true;
		}
		if(true == roverInput.keyZero && false == roverInput.newZoom) {
			roverInput.zoomValue = 0;
			roverInput.newZoom = true;
		}
	}
	
	/**
	 * Handle tap related tasks such as clearing the previous movement points
	 * and activating the item action menu.
	 */
	private void handleTap(Entity theEntity) {
		// Use the camera component to unproject the up and down coordinates
		// into world coordinates to decide what to do about the tap event
		CameraComponent camera = cameraMapper.get(theEntity);
		LocationComponent location = locationMapper.get(theEntity);
		RoverEventComponent roverEvent = roverEventMapper.get(theEntity);
		RoverInputComponent roverInput = roverInputMapper.get(theEntity);
		ViewportComponent viewport = viewportMapper.get(theEntity);

		// Convert down and up positions into World coordinates
		camera.getWorldCamera().unproject(roverInput.lastDownPos, viewport.x, viewport.y, viewport.width, viewport.height);
		camera.getWorldCamera().unproject(roverInput.lastUpPos, viewport.x, viewport.y, viewport.width, viewport.height);

		Gdx.app.debug("RoverInputSystem:handleTap",
				"Tap down at ("+roverInput.lastDownPos.x+","+roverInput.lastDownPos.y+")");
		Gdx.app.debug("RoverInputSystem:handleTap",
				"Tap up at ("+roverInput.lastUpPos.x+","+roverInput.lastUpPos.y+")");

		// Grab the current position in map units
		Vector2 curPosition = location.getMap();
		
		// Convert world coordinates into map units (with limits)
		Vector2 newPosition = location.getLevelAsMap(roverInput.lastUpPos);
		
		// Add scan/look event to our rover on tap
		if(newPosition.dst(curPosition) < 0.1f) {
			// TODO: Replace with item action menu instead
			if(!roverEvent.scanInProgress) {
				roverEvent.events.add(roverEvent.new RoverEvent(RoverEvent.EVENT_LOOK, 360));
			}
		}
	}

	private void handleUp(Entity theEntity) {
		// Retrieve the position component for our input player
		RoverEventComponent roverEvent = roverEventMapper.get(theEntity);
		
		// Only refocus the camera if this wasn't a drag event
		if(roverEvent.movePath.size > 1) {
			// Create events for each movement path coordinate
			Vector2 anPrevious = null;
			for(Vector2 anVector : roverEvent.movePath) {
				if(anPrevious != null) {
					if(anPrevious.x == anVector.x && anPrevious.y < anVector.y) {
						roverEvent.events.add(roverEvent.new RoverEvent(RoverEvent.EVENT_MOVE, RoverEvent.DIR_N));
					} else if(anPrevious.x == anVector.x && anPrevious.y > anVector.y) {
						roverEvent.events.add(roverEvent.new RoverEvent(RoverEvent.EVENT_MOVE, RoverEvent.DIR_S));
					} else if(anPrevious.y == anVector.y && anPrevious.x < anVector.x) {
						roverEvent.events.add(roverEvent.new RoverEvent(RoverEvent.EVENT_MOVE, RoverEvent.DIR_E));
					} else if(anPrevious.y == anVector.y && anPrevious.x > anVector.x) {
						roverEvent.events.add(roverEvent.new RoverEvent(RoverEvent.EVENT_MOVE, RoverEvent.DIR_W));
					} else if(anPrevious.x > anVector.x && anPrevious.y > anVector.y) {
						roverEvent.events.add(roverEvent.new RoverEvent(RoverEvent.EVENT_MOVE, RoverEvent.DIR_SW));
					} else if(anPrevious.x < anVector.x && anPrevious.y < anVector.y) {
						roverEvent.events.add(roverEvent.new RoverEvent(RoverEvent.EVENT_MOVE, RoverEvent.DIR_NE));
					} else if(anPrevious.x > anVector.x && anPrevious.y < anVector.y) {
						roverEvent.events.add(roverEvent.new RoverEvent(RoverEvent.EVENT_MOVE, RoverEvent.DIR_NW));
					} else if(anPrevious.x < anVector.x && anPrevious.y > anVector.y) {
						roverEvent.events.add(roverEvent.new RoverEvent(RoverEvent.EVENT_MOVE, RoverEvent.DIR_SE));
					}
				}

				// Keep track of the previous vector for the next loop iteration
				anPrevious = anVector;
			} // for(Vector2 anVector : movePath)
		} // if(movePath.size > 1)

		// Clear our movement path grid coordinates
		roverEvent.movePath.clear();
	}

	private void handleDrag(Entity theEntity) {
		// Use the camera, position, and world limits components to pan and add to the movement path
		CameraComponent camera = cameraMapper.get(theEntity);
		LocationComponent location = locationMapper.get(theEntity);
		RoverEventComponent roverEvent = roverEventMapper.get(theEntity);
		RoverInputComponent roverInput = roverInputMapper.get(theEntity);

		// Grab the current position in map units
		Vector2 curPosition = location.getMap();
		
		// Convert world coordinates into map units (with limits)
		Vector2 newPosition = location.getLevelAsMap(roverInput.lastDragPos);
		
		// Are we panning (touch drag position doesn't start on input player position or
		// there is no down event since we are using the keyboard/DPAD to move around or
		// the input player already has events in its queue)? then use the drag delta
		// values to move the camera around
		if ((roverEvent.movePath.size == 0 && (newPosition.dst(curPosition) > 0.1f ||
			false == roverInput.newDown || roverEvent.events.size > 0))) {
			// The x position is purposely backwards from the y direction
			camera.getWorldCamera().translate(roverInput.lastDragDelta);
		}
		// Not panning (touch drag position starts on input player position or
		// new position is within 1 orthogonal square away from last movement
		// coordinate and not the same as the last movement coordinate)? then
		// add this new position to our movement path list
		else if( (roverEvent.movePath.size == 0 && newPosition.dst(curPosition) < 0.1f) ||
		         (roverEvent.movePath.size > 0 && newPosition.dst(roverEvent.movePath.peek()) < 1.42f &&
		          !newPosition.equals(roverEvent.movePath.peek())) ) {
			// Add the new position and pan the camera if added with the keyboard
			roverEvent.movePath.add(newPosition);
			if(roverInput.newKeyDown) {
				camera.getWorldCamera().translate(roverInput.lastDragDelta);
			}
		}

		// Update our camera with the changes above (if any)
		camera.getWorldCamera().update();
		camera.getWorldCamera().apply(Gdx.gl10);
	}
	
	private void handleZoom(Entity theEntity) {
		// Adjust the zoom value of the world camera for this input player
		CameraComponent camera = cameraMapper.get(theEntity);
		RoverInputComponent roverInput = roverInputMapper.get(theEntity);

		if(roverInput.zoomValue > 0) {
			// Zoom out
			camera.getWorldCamera().zoom = Math.min(ZOOM_MAXIMUM, camera.getWorldCamera().zoom + ZOOM_STEP_SIZE);
		}
		else if(roverInput.zoomValue < 0) {
			// Zoom in
			camera.getWorldCamera().zoom = Math.max(ZOOM_MINIMUM, camera.getWorldCamera().zoom - ZOOM_STEP_SIZE);
		}
		else
		{
			// Zoom reset
			camera.getWorldCamera().zoom = 1.0f;
		}
		camera.getWorldCamera().update();
		camera.getWorldCamera().apply(Gdx.gl10);
	}
	
	/////////////////////////////////////////////////////////////////////////
	// Keyboard inputs
	/////////////////////////////////////////////////////////////////////////
	/**
	 * Called when the keycode is being applied
	 */
	@Override
	public boolean keyDown(int keycode) {
		boolean consumed = false;
		
		// Is the mouse within our viewport? then consume this event
		ImmutableBag<Entity> entities = getActives();
		for (int i = 0, s = entities.size(); s > i; i++) {
			Entity anEntity = entities.get(i);
			RoverInputComponent roverInput = roverInputMapper.get(anEntity);
			ViewportComponent viewport = viewportMapper.get(anEntity);
			if(viewport.intersects((int)lastMovePos.x, (int)lastMovePos.y)) {
				// Indicate specific keys are currently pressed (down)
				if(keycode == Input.Keys.DPAD_LEFT || keycode == Input.Keys.LEFT ||
				   keycode == Input.Keys.A || keycode == Input.Keys.NUM_4) {
					roverInput.keyRepeatForce = !roverInput.keyLeft;
					roverInput.keyLeft = true;
				}
				else if(keycode == Input.Keys.DPAD_RIGHT || keycode == Input.Keys.RIGHT ||
						keycode == Input.Keys.D || keycode == Input.Keys.NUM_6) {
					roverInput.keyRepeatForce = !roverInput.keyRight;
					roverInput.keyRight = true;
				}
				else if(keycode == Input.Keys.DPAD_UP || keycode == Input.Keys.UP || 
						keycode == Input.Keys.W || keycode == Input.Keys.NUM_8) {
					roverInput.keyRepeatForce = !roverInput.keyUp;
					roverInput.keyUp = true;
				}
				else if(keycode == Input.Keys.DPAD_DOWN || keycode == Input.Keys.DOWN ||
						keycode == Input.Keys.S || keycode == Input.Keys.NUM_2) {
					roverInput.keyRepeatForce = !roverInput.keyDown;
					roverInput.keyDown = true;
				}
				else if(keycode == Input.Keys.DPAD_CENTER || keycode == Input.Keys.SPACE ||
						keycode == Input.Keys.NUM_5) {
					roverInput.keyCenter = false; // purposely inverted
				}
				else if(keycode == Input.Keys.PLUS) {
					roverInput.keyPlus = true;
				}
				else if(keycode == Input.Keys.MINUS) {
					roverInput.keyMinus = true;
				}
				else if(keycode == Input.Keys.NUM_0) {
					roverInput.keyZero = true;
				}
				consumed = true;
			}
		}
		
		// Return the consumed state for this event
		return consumed;
	}

	/**
	 * Called when the keycode has been released
	 */
	@Override
	public boolean keyUp(int keycode) {
		boolean consumed = false;

		// Is the mouse within our viewport? then consume this event
		ImmutableBag<Entity> entities = getActives();
		for (int i = 0, s = entities.size(); s > i; i++) {
			Entity anEntity = entities.get(i);
			RoverInputComponent roverInput = roverInputMapper.get(anEntity);
			ViewportComponent viewport = viewportMapper.get(anEntity);
			if(viewport.intersects((int)lastMovePos.x, (int)lastMovePos.y)) {
				// Indicate specific keys are currently released (up)
				if(keycode == Input.Keys.DPAD_LEFT || keycode == Input.Keys.LEFT ||
				   keycode == Input.Keys.A || keycode == Input.Keys.NUM_4) {
					roverInput.keyLeft = false;
				}
				else if(keycode == Input.Keys.DPAD_RIGHT || keycode == Input.Keys.RIGHT ||
						keycode == Input.Keys.D || keycode == Input.Keys.NUM_6) {
					roverInput.keyRight = false;
				}
				else if(keycode == Input.Keys.DPAD_UP || keycode == Input.Keys.UP ||
						keycode == Input.Keys.W || keycode == Input.Keys.NUM_8) {
					roverInput.keyUp = false;
				}
				else if(keycode == Input.Keys.DPAD_DOWN || keycode == Input.Keys.DOWN ||
						keycode == Input.Keys.S || keycode == Input.Keys.NUM_2) {
					roverInput.keyDown = false;
				}
				else if(keycode == Input.Keys.DPAD_CENTER || keycode == Input.Keys.SPACE ||
						keycode == Input.Keys.NUM_5) {
					roverInput.keyCenter = true; // purposely inverted
				}
				else if(keycode == Input.Keys.PLUS) {
					roverInput.keyPlus = false;
				}
				else if(keycode == Input.Keys.MINUS) {
					roverInput.keyMinus = false;
				}
				else if(keycode == Input.Keys.NUM_0) {
					roverInput.keyZero = false;
				}
				consumed = true;
			}
		}
		
		// Return the consumed state for this event
		return consumed;
	}

	/**
	 * Called after a key down and key up event has occurred and provides the
	 * key that was typed in unicode format (is this unicode 8 format?).
	 */
	@Override
	public boolean keyTyped(char character) {
		// Indicates that we didn't consume this input event
		return false;
	}

	/////////////////////////////////////////////////////////////////////////
	// Mouse/Touch inputs
	/////////////////////////////////////////////////////////////////////////
	/**
	 * Called any time the button is pressed or finger starts touching the
	 * screen.
	 */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		boolean consumed = false;
		
		// Is the mouse within our viewport? then consume this event
		ImmutableBag<Entity> entities = getActives();
		for (int i = 0, s = entities.size(); s > i; i++) {
			Entity anEntity = entities.get(i);
			RoverInputComponent roverInput = roverInputMapper.get(anEntity);
			ViewportComponent viewport = viewportMapper.get(anEntity);
			if(viewport.intersects(screenX, screenY)) {
				if(false == roverInput.newDown && false == roverInput.newDrag) {
					//roverInput.lastDownButton = button;
					roverInput.lastDownPos.set(screenX, screenY, 0);
					roverInput.newDown = true;
				}
				consumed = true;
			}
		}

		// Call our mouseMoved method too
		mouseMoved(screenX, screenY);

		// Return the consumed state for this event
		return consumed;
	}

	/**
	 * Called any time the button is released or finger stops touching the
	 * screen. 
	 */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		boolean consumed = false;
		
		// Is the mouse within our viewport? then consume this event
		ImmutableBag<Entity> entities = getActives();
		for (int i = 0, s = entities.size(); s > i; i++) {
			Entity anEntity = entities.get(i);
			RoverInputComponent roverInput = roverInputMapper.get(anEntity);
			ViewportComponent viewport = viewportMapper.get(anEntity);
			if(viewport.intersects(screenX, screenY)) {
				// No current up or key down event in progress? then start one now
				if(false == roverInput.newUp && false == roverInput.newKeyDown) {
					//lastUpButton = button;
					roverInput.lastUpPos.set(screenX, screenY, 0);
					roverInput.newUp = true;
				}
				consumed = true;
			}
		}

		// Call our mouseMoved method too
		mouseMoved(screenX, screenY);

		// Return the consumed state for this event
		return consumed;
	}

	/**
	 * Called any time the mouse is moved when a button is down.
	 */
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		boolean consumed = false;
		
		// Is the mouse within our viewport? then consume this event
		ImmutableBag<Entity> entities = getActives();
		for (int i = 0, s = entities.size(); s > i; i++) {
			Entity anEntity = entities.get(i);
			RoverInputComponent roverInput = roverInputMapper.get(anEntity);
			ViewportComponent viewport = viewportMapper.get(anEntity);
			if(viewport.intersects(screenX, screenY)) {
				// No current key down or drag event in progress? then start a drag event now
				if(false == roverInput.newDrag && false == roverInput.newKeyDown) {
					// Use the camera component to unproject screen coordinates into world coordinates
					CameraComponent camera = cameraMapper.get(anEntity);
					roverInput.lastDragPos.set(screenX, screenY, 0);
					roverInput.lastDragDelta.set(lastMovePos.x - roverInput.lastDragPos.x, roverInput.lastDragPos.y - lastMovePos.y, 0);
					if(roverInput.lastDragDelta.x > DRAG_LIMIT) {
						roverInput.lastDragDelta.x = DRAG_LIMIT;
					} else if(roverInput.lastDragDelta.x < -DRAG_LIMIT) {
						roverInput.lastDragDelta.x = -DRAG_LIMIT;
					}
					if(roverInput.lastDragDelta.y > DRAG_LIMIT) {
						roverInput.lastDragDelta.y = DRAG_LIMIT;
					} else if(roverInput.lastDragDelta.y < -DRAG_LIMIT) {
						roverInput.lastDragDelta.y = -DRAG_LIMIT;
					}
					camera.getWorldCamera().unproject(roverInput.lastDragPos, viewport.x, viewport.y, viewport.width, viewport.height);
					roverInput.newDrag = true;
				}
				consumed = true;
			}
		}
		
		// Call our mouseMoved method too
		mouseMoved(screenX, screenY);

		// Return the consumed state for this event
		return consumed;
	}

	/**
	 * Called any time the mouse is moved when a button is not down. 
	 */
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// Keep track of move position in screen coordinates
		lastMovePos.set(screenX, screenY, 0);
		
		// Never consume mouseMoved events
		return false;
	}

	/**
	 * Called when the mouse wheel has been scrolled.
	 */
	@Override
	public boolean scrolled(int amount) {
		boolean consumed = false;
		
		// Is the mouse within our viewport? then consume this event
		ImmutableBag<Entity> entities = getActives();
		for (int i = 0, s = entities.size(); s > i; i++) {
			Entity anEntity = entities.get(i);
			ViewportComponent viewport = viewportMapper.get(anEntity);
			RoverInputComponent roverInput = roverInputMapper.get(anEntity);
			if(viewport.intersects((int)lastMovePos.x, (int)lastMovePos.y)) {
				// Create a new zoom event using the scroll value provided
				roverInput.zoomValue = amount;
				roverInput.newZoom = true;
				consumed = true;
			}
		}
		
		// Return the consumed state for this event
		return consumed;
	}
}
