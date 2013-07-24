package com.cosmicrover.cassini.components;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class RoverInputComponent extends AbstractComponent {
	// Keyboard events currently in progress
	public boolean keyUp = false;
	public boolean keyDown = false;
	public boolean keyLeft = false;
	public boolean keyRight = false;
	public boolean keyCenter = false;
	public boolean keyPlus = false;
	public boolean keyMinus = false;
	public boolean keyZero = false;
	
	// Indicates a Keyboard repeat event is allowed
	public boolean keyRepeat = false;

	// Force the key repeat event on new press of key
	public boolean keyRepeatForce = false;
	
	// Key repeat accumulator for key repeat events
	public float keyRepeatAccumulator = 0.0f;

	// Key repeat interval in seconds
	public float keyRepeatInterval = 0.125f;
	
	// Indicates the Keyboard owns the current down event
	public boolean newKeyDown = false;
	
	// Keyboard Center toggle/Mouse button down event
	public boolean newDown = false;
	
	// Keyboard Center toggle/Mouse button up event
	public boolean newUp = false;
	public boolean newDrag = false;

	// Keyboard + or -/Mouse scroll wheel event
	public boolean newZoom = false;
	
    // Zoom value to use for newZoom event
	public int zoomValue = 0;
    
	// Last down position for newDown event above
	public final Vector3 lastDownPos;
	
	// Last drag position after newDown event and keyboard/mouse move event
	public final Vector3 lastDragPos;

	// Delta between the last mouse move event and the current mouse drag event
	// or the keyboard drag step value
	public final Vector3 lastDragDelta;
    
    // Last up position after newDown event above
	public final Vector3 lastUpPos;

    public RoverInputComponent() {
		lastDownPos = new Vector3();
		lastDragPos = new Vector3();
		lastDragDelta = new Vector3();
		lastUpPos = new Vector3();
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		// Default implementation is to do nothing
	}
}
