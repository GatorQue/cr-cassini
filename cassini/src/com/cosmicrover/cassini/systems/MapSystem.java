package com.cosmicrover.cassini.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.utils.Array;
import com.cosmicrover.cassini.EntityFactory;
import com.cosmicrover.cassini.WorldData;
import com.cosmicrover.cassini.components.CameraComponent;
import com.cosmicrover.cassini.components.LocationComponent;
import com.cosmicrover.cassini.components.MapComponent;
import com.cosmicrover.cassini.components.PropertyComponent;
import com.cosmicrover.cassini.managers.PropertyManager;
import com.cosmicrover.core.GameManager;

public class MapSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<CameraComponent> cameraMapper;
	@Mapper ComponentMapper<LocationComponent> locationMapper;
	@Mapper ComponentMapper<MapComponent> mapMapper;
	@Mapper ComponentMapper<PropertyComponent> propertyMapper;

	/// Layer properties that might exist
	private static final String LAYER_PROPERTY_ITEMS      = "items";
	private static final String LAYER_PROPERTY_FOREGROUND = "foreground";
	
	// The GameManager object to use to retrieve various resources
	private final GameManager gameManager;
	
	private final WorldData worldData;
	
	// Indicates the parent screen needs to show a loading screen
	private boolean loadingRequired = false;
	
	@SuppressWarnings("unchecked")
	public MapSystem(GameManager gameManager) {
		super(Aspect.getAspectForAll(LocationComponent.class, MapComponent.class, PropertyComponent.class));
		this.gameManager = gameManager;

		// Retrieve a copy of our WorldData object from our GameManager
		worldData = gameManager.getData(WorldData.class); 
	}
	
	public boolean isLoadingRequired() {
		return loadingRequired;
	}
	

	@Override
	protected void process(Entity theEntity) {
		// Clear our loading required flag
		loadingRequired = false;
		
		LocationComponent location = locationMapper.get(theEntity);
		MapComponent map = mapMapper.get(theEntity);
		
		// Has our map value changed from our current location? then load the map now
		if( map.tiledMap == null ||
			(map.mapFilename != null && !map.mapFilename.equalsIgnoreCase(location.getMapName())) ) {
			// Map not yet loaded? then load it now
			if(gameManager.getAssetManager().isLoaded(map.mapFilename)) {
				// Retrieve the TiledMap from our assetManager
				map.tiledMap = gameManager.getAssetManager().get(map.mapFilename);
			} else {
				// Tell our AssetManager to load it now
				gameManager.getAssetManager().load(map.mapFilename, TiledMap.class);
				
				// Clear our tiledMap until its loaded
				map.tiledMap = null;
				
				// Indicate we need to switch to our loading screen
				loadingRequired = true;
			}
			
			if(map.tiledMap != null) {
				// Next retrieve a few common properties from the map
				map.mapWidth = map.tiledMap.getProperties().get("width", Integer.class);
				map.mapHeight = map.tiledMap.getProperties().get("height", Integer.class);
				map.mapTileWidth = map.tiledMap.getProperties().get("tilewidth", Integer.class);
				map.mapTileHeight = map.tiledMap.getProperties().get("tileheight", Integer.class);

				// Set our boundaries according to the map just loaded
				location.setLevelGrid(map.mapTileWidth, map.mapTileHeight);
				location.setMapBounds(0,0,map.mapWidth, map.mapHeight);
				location.setMapName(map.mapFilename);
				
				// Call our GetLayers method to determine which layers are foreground layers and background layers
				getLayerTypes(map);
				
				// Create world entities for this map the first time its loaded
				createWorldMapEntities(map, location);

				// Create player specific entities for this map for this player
				createPlayerMapEntities(theEntity);
			}
		}
	}

	private void createWorldMapEntities(MapComponent map, LocationComponent location) {
		// Only create world entities for this map if this is the first time we have seen the map
		if(!worldData.mapsLoaded.contains(map.mapFilename, false)) {
			// Loop through each layer looking for items property
			for(MapLayer mapLayer : map.tiledMap.getLayers()) {
				// Look for layers with the "items" property and add the items found on them
				if("true".equalsIgnoreCase(mapLayer.getProperties().get(LAYER_PROPERTY_ITEMS, String.class)) ) {
					// Create World Map items for this map
					createWorldMapItems(mapLayer, location);
				}
			}
			
			// Add this map to the list of maps loaded
			worldData.mapsLoaded.add(map.mapFilename);
		}
	}
	
	private void createWorldMapItems(MapLayer mapLayer, LocationComponent location) {
		TiledMapTileLayer tiledMapLayer = TiledMapTileLayer.class.cast(mapLayer);
		for(int y=0; y<tiledMapLayer.getHeight(); y++) {
			for(int x=0; x<tiledMapLayer.getWidth(); x++) {
				Cell cell = tiledMapLayer.getCell(x, y);
				if(cell != null) {
					// Create a LocationComponent for this tile as a clone from the entity Location provided
					LocationComponent locationItem = new LocationComponent(location, x, y);
					// Now create the Entity for this item and add it to the world
					EntityFactory.createMapItem(world, locationItem, cell.getTile()).addToWorld();
				}
			}
		}
	}

	private void createPlayerMapEntities(Entity theEntity) {
		MapComponent map = mapMapper.get(theEntity);

		// Have we done this before for this player? then add player specific entities now
		if(!map.mapsLoaded.contains(map.mapFilename, false)) {
			LocationComponent location = locationMapper.get(theEntity);
			PropertyComponent property = propertyMapper.get(theEntity);
			
			// Retrieve the base for this player using our TagManager
			Entity anBase = world.getManager(PropertyManager.class).getEntityByTag(EntityFactory.BASE_TAG + property.playerId);
			LocationComponent anBaseLocation = locationMapper.get(anBase);
			// Assign our player to the same location as its base
			location.setMap(anBaseLocation.getMap());
			
			// See if we have a camera component, if so position it at the base location too
			CameraComponent camera = cameraMapper.getSafe(theEntity);
			if(camera != null) {
				camera.setWorldPosition(anBaseLocation.getLevel().x, anBaseLocation.getLevel().y);
			}

			// Create map mask entities for each map location to hide each square
			for(int row=0; row<map.mapHeight; row++) {
				for(int col=0; col<map.mapWidth; col++) {
					// Skip the location of where we are now on the new map
					if(col == location.getMapX() && row == location.getMapY()) {
						continue;
					}
					// Create a mask entity for this location
					LocationComponent locationMask = new LocationComponent(location, col, row);
					EntityFactory.createMapMask(world, locationMask, theEntity.getUuid().toString()).addToWorld();
				}
			}
			
			// Add this mapFilename to our list of maps loaded for this entity
			map.mapsLoaded.add(map.mapFilename);
		}
	}
	
	/**
	 * This method will determine which layers are background and which layers
	 * are foreground layers based on the foreground property being set to the
	 * value of "true". Once determined, the caller can retrieve these integer
	 * arrays and use them as part of the rendering of the map.
	 */
	private void getLayerTypes(MapComponent map) {
		int layerCount = map.tiledMap.getLayers().getCount();
		Array<Integer> aBackground = new Array<Integer>();
		Array<Integer> aForeground = new Array<Integer>();
		
		for(int iLayer=0; iLayer<layerCount; iLayer++) {
			MapLayer layer = map.tiledMap.getLayers().get(iLayer);
			// Is foreground property set to "true" for this layer?
			if("true".equalsIgnoreCase(layer.getProperties().get(LAYER_PROPERTY_FOREGROUND, String.class))) {
				// Add iLayer to foreground layer list
				aForeground.add(iLayer);
			} else {
				// Add iLayer to background layer list
				aBackground.add(iLayer);
			}
		}
		// Create a new backgroundLayers array
		map.mapBackgroundLayers = new int[aBackground.size];
		
		// Add the items from our array list to the new backgroundLayers array
		for(int i=0; i<aBackground.size; i++) {
			map.mapBackgroundLayers[i] = aBackground.get(i).intValue();
		}
		
		// Create a new foregroundLayers array
		map.mapForegroundLayers = new int[aForeground.size];

		// Add the items from our array list to the new foregroundLayers array
		for(int i=0; i<aForeground.size; i++) {
			map.mapForegroundLayers[i] = aForeground.get(i).intValue();
		}
	}
}
