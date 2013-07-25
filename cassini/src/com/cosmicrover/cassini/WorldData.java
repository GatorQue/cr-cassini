package com.cosmicrover.cassini;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.utils.Bag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.cassini.managers.GroupManager;
import com.cosmicrover.cassini.managers.OwnerManager;
import com.cosmicrover.cassini.managers.PersistenceManager;
import com.cosmicrover.cassini.managers.ViewportManager;
import com.cosmicrover.cassini.screens.MainMenuScreen;
import com.cosmicrover.cassini.screens.PlanetMapScreen;
import com.cosmicrover.cassini.screens.OptionsScreen;
import com.cosmicrover.cassini.systems.MapSystem;
import com.cosmicrover.core.GameData;
import com.cosmicrover.core.GameManager;
import com.cosmicrover.core.components.AbstractComponent;
import com.cosmicrover.core.screens.AbstractLoadingScreen;
import com.cosmicrover.core.screens.AbstractScreen;
import com.cosmicrover.core.screens.AssetLoadingScreen;

public class WorldData extends GameData {
	/// List of screenId values to be used by GameManager
	public static final int ASSET_LOADING_SCREEN       = 1;
	public static final int MAIN_MENU_SCREEN           = 2;
	public static final int OPTIONS_SCREEN             = 3;
	public static final int PLANET_MAP_SCREEN          = 4;

	/// Data file format version number 
	private static final int DATA_FORMAT_VERSION = 1;

	/// Keep track of our GameManager object provided at init
	private GameManager gameManager = null;
	
	/// Our single World object
	private final World world;
	
	/// An array of entities to be added to our World object during restoreGame() 
	private final Array<Entity> entities;

	/**
	 * Only the default constructor is allowed for deriving from GameData. This
	 * is the default constructor.
	 */
	public WorldData() {
		world = new World();
		entities = new Array<Entity>();
	}
	
	public World getWorld() {
		return world;
	}

	@Override
	public AbstractScreen getInitialScreen() {
		return screenManager.getScreen(ASSET_LOADING_SCREEN);
	}

	@Override
	public int getOptionsScreen() {
		return OPTIONS_SCREEN;
	}

	@Override
	public void init(GameManager gameManager) {
		// Keep track of the GameManager object provided
		this.gameManager = gameManager;
		
		// Add our TiledMap handler to our AssetManager
		gameManager.getAssetManager().setLoader(TiledMap.class, ".tmx", new TmxMapLoader());

		// Add our managers first
		world.setManager(new GroupManager());
		world.setManager(new OwnerManager());
		world.setManager(new PersistenceManager());
		world.setManager(new ViewportManager());
		
		//world.setManager(new GridZoneManager(tileWidth, tileHeight));
		
		// Add our systems next
		world.setSystem(new MapSystem(gameManager), true); // passive system
		//world.setSystem(new Velocity2DSystem());
		//world.setSystem(new PlayerInputSystem(camera));
		//world.setSystem(new CollisionSystem());
		//world.setSystem(new ExpiringSystem());
		//world.setSystem(new EntitySpawningTimerSystem(width, height));
		//world.setSystem(new ParallaxStarRepeatingSystem(height));
		//world.setSystem(new SpriteTintAnimationSystem());
		//world.setSystem(new ScaleAnimationSystem());
		//world.setSystem(new RemoveOffscreenShipsSystem(height));
	
		// Create our screens next
		screenManager.registerScreen(ASSET_LOADING_SCREEN, new AssetLoadingScreen(gameManager, 0.25f));
		screenManager.registerScreen(MAIN_MENU_SCREEN, new MainMenuScreen(gameManager));
		screenManager.registerScreen(OPTIONS_SCREEN, new OptionsScreen(gameManager, MAIN_MENU_SCREEN));
		screenManager.registerScreen(PLANET_MAP_SCREEN, new PlanetMapScreen(gameManager, MAIN_MENU_SCREEN));
		
		// Set the initial next screenId value for LoadingScreen to the Main Menu
		AbstractLoadingScreen.setNextScreenId(MAIN_MENU_SCREEN);
		
		// Initialize our World object after adding screens since some of them
		// might have added Systems and Managers too.
		world.initialize();
	}

	@Override
	public void newGame() {
		// Clear our new game flag
		setNewGame(false);
		
		// Clear any existing entities from the world first
		world.getManager(PersistenceManager.class).removeAll();
		
		// Make sure our list of entities to be restored is clear
		entities.clear();

		// Create entities for a new game
    	entities.add(EntityFactory.createLocalPlayer(world));
		entities.add(EntityFactory.createRemotePlayer(world));
		entities.add(EntityFactory.createRemotePlayer(world));
		entities.add(EntityFactory.createRemotePlayer(world));
		//entities.add(EntityFactory.createRemotePlayer(world));
		//entities.add(EntityFactory.createRemotePlayer(world));
		//entities.add(EntityFactory.createRemotePlayer(world));
		//entities.add(EntityFactory.createRemotePlayer(world));
		//entities.add(EntityFactory.createRemotePlayer(world));

    	// Call our restoreGame method to make these entities active
    	restoreGame();
    	
		// Set our in progress flag
		setInProgress(true);

		// Switch to the Planet Map screen for now
		gameManager.setScreen(PLANET_MAP_SCREEN);
	}
	
	@Override
	public void resetGame() {
		// Clear our in progress flag
		setInProgress(false);

		// Delete our game data file
		gameManager.deleteData();
		
		// Set our new game flag to true
		setNewGame(true);
	}
	
	@Override
	public void resumeGame() {
		// Change to the correct screen
		gameManager.setScreen(PLANET_MAP_SCREEN);
		
		// Call our restoreGame method
		restoreGame();
		
		// Set our in progress flag
		setInProgress(true);
	}

	protected void restoreGame() {
		// Log the new game being created
		Gdx.app.log("WorldData", "Restoring game");
		
		// Clear any existing entities from the world first
		world.getManager(PersistenceManager.class).removeAll();
		
		// Loop through each entities in our array and add them to the world now
		for(int i = 0, s = entities.size; s > i; i++) {
			Entity anEntity = entities.get(i);
			anEntity.addToWorld();
		}
		
		// Clear our list of entities after they have been restored
		entities.clear();
		
		// The world needs a chance to process all these added entities
		// otherwise if we were to exit right now it would save an empty game
		// data file since the PersistanceManager wouldn't know about the
		// entities just added.
		world.setDelta(0.0f);
		world.process();
	}

	@Override
	public void write(Json json) {
		// Write the data format version number value first
		json.writeValue("format_version", DATA_FORMAT_VERSION);
		
		// TODO: Add other fields as needed.
		writeEntities(json);
	}

	private void writeEntities(Json json) {
    	PersistenceManager anPersistenceManager = world.getManager(PersistenceManager.class);
    	Entity[] anEntities = anPersistenceManager.getEntities();

		// Loop through each ID and persist each entity
    	json.writeArrayStart("Entities");
    	for(Entity anEntity : anEntities) {
        	json.writeObjectStart();
        	json.writeValue("oldId", anEntity.getId());
        	json.writeValue("oldUuid", anEntity.getUuid().toString());
        	writeComponents(json, anEntity);
        	json.writeObjectEnd();
    	}
		json.writeArrayEnd();
	}
	
	private void writeComponents(Json json, Entity theEntity) {
    	Bag<Component> anComponents = new Bag<Component>();
    	theEntity.getComponents(anComponents);
    	json.writeArrayStart("components");
		for(int i = 0, iSize = anComponents.size(); iSize > i; i++) {
			Component anComponent = anComponents.get(i);
			if(AbstractComponent.class.isInstance(anComponent)) {
				AbstractComponent abstractComponent = AbstractComponent.class.cast(anComponent);
	        	json.writeObjectStart();
				abstractComponent.write(json);
				json.writeObjectEnd();
			}
    	}
		json.writeArrayEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		// Read the data format version number first
		int formatVersion = json.readValue("format_version", Integer.class, jsonData);

		// Is this an old version of the data file that needs to be upgraded?
		if(formatVersion < DATA_FORMAT_VERSION) {
			// TODO: Add something for converting from old to new data files
		}
		
		// Attempt to read our array of entities from our data file
		JsonValue jsonEntities = jsonData.get("Entities");
		if(jsonEntities != null) {
			readEntities(formatVersion, json, jsonEntities);
		}
	}
	
	private void readEntities(int formatVersion, Json json, JsonValue jsonData) {
		// List of oldId's to newId's
		IntIntMap idMap = new IntIntMap();
		ArrayMap<String, String> uuidMap = new ArrayMap<String, String>();
		
		// Loop through each entity recorded and create them
		for(int i=0, iSize = jsonData.size; iSize > i; i++) {
			// Loop through each entity in our array
			JsonValue jsonEntity = jsonData.get(i);
			int oldId = json.readValue("oldId", Integer.class, jsonEntity);
			String oldUuid = json.readValue("oldUuid", String.class, jsonEntity);
			Entity anEntity = world.createEntity();
			
			// Keep track of the oldId's and newId's in a map for processing later
			idMap.put(oldId, anEntity.getId());
			
			// Keep track of the oldUuid's and newUuid's in a map for processing later
			uuidMap.put(oldUuid, anEntity.getUuid().toString());
			
			// Retrieve the array of components for this entity
			JsonValue jsonComponents = jsonEntity.get("components");
			if(jsonComponents != null) {
				readComponents(formatVersion, json, jsonComponents, anEntity);
			}
			
			// Add this entity to our list of entities to be restored later
			entities.add(anEntity);
		} // for(int i=0, s=jsonEntities.size; i<s; i++)
		
		// Now give each entity a chance to update its Id's to the new Id's
    	for(Entity anEntity : entities) {
        	Bag<Component> anComponents = new Bag<Component>();
        	anEntity.getComponents(anComponents);
    		for(int i = 0, iSize = anComponents.size(); iSize > i; i++) {
    			// Retrieve the next component
    			Component anComponent = anComponents.get(i);
    			// Is this an AbstractComponent type? then let it know about the renumbering
    			if(AbstractComponent.class.isInstance(anComponent)) {
    				AbstractComponent abstractComponent = AbstractComponent.class.cast(anComponent);
    				abstractComponent.changeIds(idMap);
    				abstractComponent.changeUuids(uuidMap);
    			}
        	} // for(int i = 0, iSize = anComponents.size(); iSize > i; i++)
    	} // for(Entity anEntity : entities)
	}
	
	private void readComponents(int formatVersion, Json json, JsonValue jsonData, Entity theEntity) {
		// Loop through each Component recorded and create them
		for(int i=0, iSize = jsonData.size; iSize > i; i++) {
			// Loop through each entity in our array
			JsonValue jsonComponent = jsonData.get(i);

			// Attempt to create and add the component to theEntity provided
			try {
				// Something bad is likely to happen here
				Class<?> componentType = Class.forName(jsonComponent.child().name());
				AbstractComponent abstractComponent = AbstractComponent.class.cast(componentType.newInstance());
				abstractComponent.read(json, jsonComponent.child());

				// Everything good to this point? then add the component class to our new entity
				theEntity.addComponent(abstractComponent);
			} catch(ClassCastException e) {
            	Gdx.app.error( "CosmicRover:WorldData:read()",
            			"Unable to cast component '" + jsonComponent.name() + "' as AbstractComponent", e);
			} catch (ClassNotFoundException e) {
            	Gdx.app.error( "CosmicRover:WorldData:read()",
            			"Unable find class for component '" + jsonComponent.name() + "'", e);
			} catch (InstantiationException e) {
            	Gdx.app.error( "CosmicRover:WorldData:read()",
            			"Unable to create component '" + jsonComponent.name() + "'", e);
			} catch (IllegalAccessException e) {
            	Gdx.app.error( "CosmicRover:WorldData:read()",
            			"Class '" + jsonComponent.name() + "' doesn't have an default constructor", e);
			}
		} // for(int i=0, iSize = jsonData.size; iSize > i; i++)
	}

}
