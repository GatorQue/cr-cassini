package com.cosmicrover.cassini.components;

import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class PropertyComponent extends AbstractComponent {
	public static final int PLAYER_ID_UNKNOWN = 0;
	/// Tile properties that might exist
	private static final String PROPERTY_PLAYER_ID   = "playerId";
	private static final String PROPERTY_TILE_ID     = "tileId";
	private static final String PROPERTY_ITEM_COLOR  = "itemcolor";
	private static final String PROPERTY_ITEM_MASS   = "itemmass";
	private static final String PROPERTY_ITEM_NAME   = "itemname";
	private static final String PROPERTY_ITEM_TYPE   = "itemtype";
	private static final String PROPERTY_ITEM_SHAPE  = "itemshape";
	private static final String PROPERTY_ITEM_SIZE   = "itemsize";
	private static final String PROPERTY_ITEM_SOUND  = "itemsound";
	private static final String PROPERTY_ITEM_VOLUME = "itemvolume";
	private static final String PROPERTY_ITEM_WORTH  = "itemworth";
	private static final String PROPERTY_ITEM_TAG    = "itemtag";

	public enum Type {
		Unknown,
		Artifact,
		Base,
		Gem,
		Life,
		Mineral,
		Rock,
		Rover
	};
	
	public enum Color {
		Unknown,
		Black,
		Blue,
		Bronze,
		Brown,
		Copper,
		Flashy,
		Gold,
		Gray,
		Green,
		Multicolor,
		Orange,
		Pink,
		Purple,
		Red,
		Silver,
		Transparent,
		Violet,
		White,
		Yellow,
	};
	
	public enum Intensity {
		Unknown,
		Bright,
		Dim,
		Low,
		High,
	};

	public enum Shape {
		Unknown,
		Circular,
		Cylindrical,
		Flat,
		Globular,
		Irregular,
		Rectangular,
		Spherical,
		Symmetrical,
	};
	
	public enum Size {
		Unknown,
		Small,
		Large,
		Medium,
	};
	
	public enum Surface {
		Unknown,
		Shiny,
		Reflective,
		Metallic
	};
	
	public enum Sound {
		Unknown,
		TickTock,
		Exterminate,
		BarkBark,
		HissHiss,
	};

	/// Defines the tileId in the tileset for which this sprite came from
	public int       playerId = PLAYER_ID_UNKNOWN;
	public int       tileId = 0;
	public String    name;
	public Type      type;
	public String    tag;
	public float     mass;
	public float     volume;
	public int       worth;
	public Color     color;
	public Intensity intensity;
	public Shape     shape;
	public Size      size;
	public Sound     sound;
	
	public PropertyComponent() {
		tileId = 0;
		name = "Unknown";
		tag = "";
		type = Type.Unknown;
		mass = 0.0f;
		volume = 0.0f;
		worth = 0;
		color = Color.Unknown;
		intensity = Intensity.Unknown;
		shape = Shape.Unknown;
		size = Size.Unknown;
		sound = Sound.Unknown;
	}
	
	public PropertyComponent(final TiledMapTile tiledMapTile) {
		// Load our default values and then parse from the TiledMapTile properties
		this();
		parseFrom(tiledMapTile);
	}

	public void parseFrom(TiledMapTile tiledMapTile) {
		// Retrieve the tiledId
		this.tileId = tiledMapTile.getId();
		
		// Retrieve the item name
		this.name = tiledMapTile.getProperties().get(PROPERTY_ITEM_NAME, String.class);
		
		// Retrieve the item group
		this.tag = tiledMapTile.getProperties().get(PROPERTY_ITEM_TAG, String.class);
		
		// Retrieve each type using the valueOf method of each Enum class
		try {
			this.type = Type.valueOf(tiledMapTile.getProperties().get(PROPERTY_ITEM_TYPE, String.class));
		} catch(IllegalArgumentException iae) {
			// Accept the default value of Unknown
		}
		try {
			this.color = Color.valueOf(tiledMapTile.getProperties().get(PROPERTY_ITEM_COLOR, String.class));
		} catch(IllegalArgumentException iae) {
			// Accept the default value of Unknown
		}
		try {
			this.shape = Shape.valueOf(tiledMapTile.getProperties().get(PROPERTY_ITEM_SHAPE, String.class));
		} catch(IllegalArgumentException iae) {
			// Accept the default value of Unknown
		}
		try {
			this.size = Size.valueOf(tiledMapTile.getProperties().get(PROPERTY_ITEM_SIZE, String.class));
		} catch(IllegalArgumentException iae) {
			// Accept the default value of Unknown
		}
		try {
			this.sound = Sound.valueOf(tiledMapTile.getProperties().get(PROPERTY_ITEM_SOUND, String.class));
		} catch(IllegalArgumentException iae) {
			// Accept the default value of Unknown
		}
		
		// Retrieve the numeric values from the properties
		try {
			this.mass = Float.parseFloat(tiledMapTile.getProperties().get(PROPERTY_ITEM_MASS, String.class));
		} catch(NumberFormatException nfe) {
			// Accept the default value from constructor
		}
		try {
			this.volume = Float.parseFloat(tiledMapTile.getProperties().get(PROPERTY_ITEM_VOLUME, String.class));
		} catch(NumberFormatException nfe) {
			// Accept the default value from constructor
		}
		try {
			this.worth = Integer.parseInt(tiledMapTile.getProperties().get(PROPERTY_ITEM_WORTH, String.class));
		} catch(NumberFormatException nfe) {
			// Accept the default value from constructor
		}
	}
	
	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
		json.writeValue(PROPERTY_PLAYER_ID, playerId);
    	json.writeValue(PROPERTY_TILE_ID, tileId);
    	json.writeValue(PROPERTY_ITEM_COLOR, color);
    	json.writeValue(PROPERTY_ITEM_MASS, mass);
    	json.writeValue(PROPERTY_ITEM_NAME, name);
    	json.writeValue(PROPERTY_ITEM_TAG, tag);
    	json.writeValue(PROPERTY_ITEM_TYPE, type.name());
    	json.writeValue(PROPERTY_ITEM_SHAPE, shape.name());
    	json.writeValue(PROPERTY_ITEM_SIZE, size.name());
    	json.writeValue(PROPERTY_ITEM_SOUND, sound.name());
    	json.writeValue(PROPERTY_ITEM_VOLUME, volume);
    	json.writeValue(PROPERTY_ITEM_WORTH, worth);
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		playerId = json.readValue(PROPERTY_PLAYER_ID, Integer.class, jsonData);
		tileId = json.readValue(PROPERTY_TILE_ID, Integer.class, jsonData);
		color = json.readValue(PROPERTY_ITEM_COLOR, Color.class, jsonData);
		mass = json.readValue(PROPERTY_ITEM_MASS, Float.class, jsonData);
		name = json.readValue(PROPERTY_ITEM_NAME, String.class, jsonData);
		tag = json.readValue(PROPERTY_ITEM_TAG, String.class, jsonData);
		type = json.readValue(PROPERTY_ITEM_TYPE, Type.class, jsonData);
		shape = json.readValue(PROPERTY_ITEM_SHAPE, Shape.class, jsonData);
		size = json.readValue(PROPERTY_ITEM_SIZE, Size.class, jsonData);
		sound = json.readValue(PROPERTY_ITEM_SOUND, Sound.class, jsonData);
		volume = json.readValue(PROPERTY_ITEM_VOLUME, Float.class, jsonData);
		worth = json.readValue(PROPERTY_ITEM_WORTH, Integer.class, jsonData);
		tileId = json.readValue(PROPERTY_TILE_ID, Integer.class, jsonData);
	}
}
