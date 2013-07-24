package com.cosmicrover.cassini.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class RoverEventComponent extends AbstractComponent {
	public class RoverEvent {
		// List of possible events that can be performed
		public static final int EVENT_WAIT    = 0; ///< Wait for <theTime> specified in milliseconds of game time
		public static final int EVENT_LOOK    = 1; ///< Scan around the current location for <theTime> specified
		public static final int EVENT_ROTATE  = 2; ///< Rotate the rover to <theDirection> specified
		public static final int EVENT_MOVE    = 3; ///< Move to <theDirection> specified
		public static final int EVENT_GRAB    = 4; ///< Grab <theItem> specified
		public static final int EVENT_DROP    = 5; ///< Drop <theItem> specified
		public static final int EVENT_SHOOT   = 6; ///< Shoot <theItem> specified with laser
		public static final int EVENT_TWEAK   = 7; ///< Tweak <theItem> specified with arm
		public static final int EVENT_DELIVER = 8; ///< Deliver <theItem> specified to base

		// List of possible event targets for theDirection
		public static final int DIR_E    =   0;  ///< theDirection is East or 0 degrees
		public static final int DIR_NE   =   1;  ///< theDirection is North East or 45 degrees
		public static final int DIR_N    =   2;  ///< theDirection is North or 90 degrees
		public static final int DIR_NW   =   3;  ///< theDirection is North West or 135 degrees
		public static final int DIR_W    =   4;  ///< theDirection is West or 180 degrees
		public static final int DIR_SW   =   5;  ///< theDirection is South West or 225 degrees
		public static final int DIR_S    =   6;  ///< theDirection is South or 270 degrees
		public static final int DIR_SE   =   7;  ///< theDirection is South East or 315 degrees
		public static final int DIR_UP   = 100;  ///< theDirection is Up (ramp, stairs, etc)
		public static final int DIR_DOWN = 101;  ///< theDirection is Down (ramp, stairs, etc)
		public static final int DIR_IN   = 102;  ///< theDirection is Inside (cave, box, container, etc)
		public static final int DIR_OUT  = 103;  ///< theDirection is Outside (cave, box, container, etc)
		
		// List of possible event targets for theTime
		public static final int WAIT_FOREVER = -1; ///< Wait forever for something to occur
		
		/// The event type specified above 
		public int id;
		
		/// The target or destination for the event type specified above
		public int target;
		
		public RoverEvent(int theId, int theTarget) {
			this.id = theId;
			this.target = theTarget;
		}
	};

	public Array<RoverEvent> events;

	// Current direction the rover is facing now (to compare to RoverEvent.target)
	public int direction = RoverEvent.DIR_N;

	/// Next time event will be processed from our queue
	public int nextEvent = 0;
	
	// List of move path points in grid coordinates
	public final Array<Vector2> movePath;

	// Indicates that a scan/look is in progress
	public boolean scanInProgress = false;
	
	// Scan/look angle 
	public float scanAngle = MathUtils.PI2;
	
	// Scan/look angle step
	public float scanAngleStep = 100.0f / MathUtils.PI2;
	
	public RoverEventComponent() {
		events = new Array<RoverEvent>();
		movePath = new Array<Vector2>();
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeValue("direction", direction);
    	json.writeValue("nextEvent", nextEvent);
    	json.writeValue("scanInProgress", scanInProgress);
    	json.writeValue("scanAngle", scanAngle);
    	json.writeValue("scanAngleStep", scanAngleStep);
    	json.writeValue("events", events.toArray(RoverEvent.class));
    	json.writeValue("movePath", movePath.toArray(Vector2.class));
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		direction = json.readValue("direction", Integer.class, jsonData);
		nextEvent = json.readValue("nextEvent", Integer.class, jsonData);
		scanInProgress = json.readValue("scanInProgress", Boolean.class, jsonData);
		scanAngle = json.readValue("scanAngle", Float.class, jsonData);
		scanAngleStep = json.readValue("scanAngleStep", Float.class, jsonData);
		JsonValue jsonEvents = jsonData.get("events");
		if(jsonEvents != null) {
			readEvents(json, jsonEvents);
		}
		Vector2[] jsonMovePath = json.readValue("movePath", Vector2[].class, Vector2.class, jsonData);
		for(Vector2 vector2 : jsonMovePath) {
			movePath.add(vector2);
		}
	}
	
	private void readEvents(Json json, JsonValue jsonData) {
		// Loop through each RoverEvent recorded and create them
		for(int i=0, iSize = jsonData.size; iSize > i; i++) {
			// Retrieve the next RoverEvent in our array
			JsonValue jsonEvent = jsonData.get(i);
			// Retrieve the event id and target
			int id = json.readValue("id", Integer.class, jsonEvent);
			int target = json.readValue("target", Integer.class, jsonEvent);
			// Now add this as a new event
			events.add(new RoverEvent(id, target));
		}
	}
}
