package com.cosmicrover.cassini;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.cosmicrover.cassini.components.CameraComponent;
import com.cosmicrover.cassini.components.GroupComponent;
import com.cosmicrover.cassini.components.OwnerComponent;
import com.cosmicrover.cassini.components.PropertyComponent;
import com.cosmicrover.cassini.components.RoverEventComponent;
import com.cosmicrover.cassini.components.RoverInputComponent;
import com.cosmicrover.cassini.components.LocationComponent;
import com.cosmicrover.cassini.components.SpriteComponent;
import com.cosmicrover.cassini.components.MapComponent;
import com.cosmicrover.cassini.components.ViewportComponent;

public class EntityFactory {
	// List of GroupManager groups
	public static final String ALL_PLAYERS_GROUP = "all_players";
	public static final String LOCAL_PLAYERS_GROUP = "local_players";
	public static final String REMOTE_PLAYERS_GROUP = "remote_players";
	public static final String MASK_GROUP = "map_mask";
	public static final String SPRITE_GROUP = "sprites";
	public static final String WIDGET_GROUP = "widgets";
	
	// List of TagManager tags
	//private static int playerCount = 0;
	public static final String PLAYER_TAG = "PLAYER_";
	public static final String MOVE_INPUT_TAG = "MovePlayer";

	// List of Health appendages for our Rover player
	public static final int ROVER_GRABBER            = 1;
	public static final int ROVER_FRONT_LEFT_WHEEL   = 2;
	public static final int ROVER_FRONT_RIGHT_WHEEL  = 3;
	public static final int ROVER_CENTER_LEFT_WHEEL  = 4;
	public static final int ROVER_CENTER_RIGHT_WHEEL = 5;
	public static final int ROVER_BACK_LEFT_WHEEL    = 6;
	public static final int ROVER_BACK_RIGHT_WHEEL   = 7;

	public static Entity createLocalPlayer(World world) {
		// Use our private method to create this player object
		Entity anEntity = createPlayer(world);
		
		// Retrieve the Group component and add a custom group for this remote player
		GroupComponent group = anEntity.getComponent(GroupComponent.class);
		group.add(LOCAL_PLAYERS_GROUP);
		
		// Return the Entity created above
		return anEntity;
	}
	
	public static Entity createRemotePlayer(World world) {
		// Use our private method to create this player object
		Entity anEntity = createPlayer(world);
		
		// Retrieve the Group component and add a custom group for this remote player
		GroupComponent group = anEntity.getComponent(GroupComponent.class);
		group.add(REMOTE_PLAYERS_GROUP);
		
		// Return the Entity created above
		return anEntity;
	}
	
	private static Entity createPlayer(World world) {
		// Create a new Entity for this player
		Entity anEntity = world.createEntity();
		
		// Create and add a Position2D component for the player first
		LocationComponent location = new LocationComponent();
		anEntity.addComponent(location);
		
		// Create and add a Sprite component for the player next
		SpriteComponent sprite = new SpriteComponent();
		anEntity.addComponent(sprite);
		
		// Create the camera component using the world and hud cameras above
		anEntity.addComponent(new CameraComponent());
		
		// Add our event queue component for processing events
		anEntity.addComponent(new RoverEventComponent());
		
		// Add our level map component to this player
		MapComponent map = new MapComponent();
		map.mapFilename = "maps/test.tmx";
		anEntity.addComponent(map);
		
		// Create the observer component for this remote player
		anEntity.addComponent(new RoverInputComponent());
		
		// Create and add a Viewport component for this player
		ViewportComponent viewport = new ViewportComponent();
		anEntity.addComponent(viewport);
		
		// Create and add a BoundByRadius component for the player next
		//BoundByRadiusComponent radiusBound = new BoundByRadiusComponent(43);
		//anEntity.addComponent(radiusBound);
		
		// Create and add a Velocity2D component for the player next
		//Velocity2DComponent velocity = new Velocity2DComponent();
		//anEntity.addComponent(velocity);
		
		// Create and add a Health component for the player next
		//HealthComponent health = new HealthComponent();
		//health.add(ROVER_GRABBER);
		//health.add(ROVER_FRONT_LEFT_WHEEL);
		//health.add(ROVER_FRONT_RIGHT_WHEEL);
		//health.add(ROVER_CENTER_LEFT_WHEEL);
		//health.add(ROVER_CENTER_RIGHT_WHEEL);
		//health.add(ROVER_BACK_LEFT_WHEEL);
		//health.add(ROVER_BACK_RIGHT_WHEEL);
		//anEntity.addComponent(health);
		
		// Add a unique player tag for this player
		//world.getManager(TagManager.class).register(PLAYER_TAG + ++playerCount, anEntity);

		// Add Group component to keep track of this entity as a batch
		GroupComponent group = new GroupComponent();
		group.add(SPRITE_GROUP);
		anEntity.addComponent(group);
		
		//e.addComponent(new Player());
		return anEntity;
	}
	
	public static Entity createMapItem(World world, LocationComponent location, TiledMapTile tiledMapTile) {
		// Create a new Entity for this rover
		Entity anEntity = world.createEntity();

		// Add the location component provided to this entity
		anEntity.addComponent(location);
		
		// Create and add a Sprite component for the item next
		SpriteComponent sprite = new SpriteComponent(tiledMapTile.getTextureRegion());
		anEntity.addComponent(sprite);
		
		// Add Group component to keep track of this entity as a batch
		GroupComponent group = new GroupComponent();
		group.add(SPRITE_GROUP);
		anEntity.addComponent(group);
		
		// Add Property component for this item
		PropertyComponent property = new PropertyComponent(tiledMapTile);
		anEntity.addComponent(property);
		
		return anEntity;
	}
	
	public static Entity createMapMask(World world, LocationComponent locationMask, String theOwnerUuid) {
		// Create a new Entity for this rover
		Entity anEntity = world.createEntity();
		
		// Add Location component provided for this entity
		anEntity.addComponent(locationMask);
		
		// Add Owner component provided for this entity
		OwnerComponent owner = new OwnerComponent();
		owner.ownerUuid = theOwnerUuid;
		owner.groupPrefix = MASK_GROUP + locationMask.getContextTag();
		anEntity.addComponent(owner);
		
		return anEntity;
	}
}
