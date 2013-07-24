package com.cosmicrover.cassini.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.cosmicrover.cassini.EntityFactory;
import com.cosmicrover.cassini.components.LocationComponent;
import com.cosmicrover.cassini.components.MapComponent;
import com.cosmicrover.core.GameManager;

public class MapSystem extends EntityProcessingSystem {
	@Mapper ComponentMapper<LocationComponent> locationMapper;
	@Mapper ComponentMapper<MapComponent> mapMapper;

	// The GameManager object to use to retrieve various resources
	private final GameManager gameManager;

	// Indicates the parent screen needs to show a loading screen
	private boolean loadingRequired = false;
	
	@SuppressWarnings("unchecked")
	public MapSystem(GameManager gameManager) {
		super(Aspect.getAspectForAll(LocationComponent.class, MapComponent.class));
		this.gameManager = gameManager;
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
				GetLayerTypes(map, map.tiledMap);
				
				// Create player specific entities for this map for this player
				CreateMapEntities(theEntity);
			}
		}
	}

	private void CreateMapEntities(Entity theEntity) {
		MapComponent map = mapMapper.get(theEntity);
		LocationComponent location = locationMapper.get(theEntity);

		// Have we done this before for this player? then add player specific entities now
		if(!map.mapsLoaded.contains(map.mapFilename, false)) {
			// Create map mask entities for each map location to hide each square
			for(int row=0; row<map.mapHeight; row++) {
				for(int col=0; col<map.mapWidth; col++) {
					// FIXME: Replace this with starting position obtained from map properties
					// Is this our starting position? then skip creating a mask for it
					if(col==0 && row == 0) {
						continue;
					}
					LocationComponent locationMask = new LocationComponent(location);
					locationMask.setMap(col,row);
					EntityFactory.createMapMask(world, locationMask, theEntity.getUuid().toString()).addToWorld();
				}
			}
			
			// Add this entities UUID to our list of created map entities
			map.mapsLoaded.add(map.mapFilename);
		}
	}

	/**
	 * This method will determine which layers are background and which layers
	 * are foreground layers based on the foreground property being set to the
	 * value of "true". Once determined, the caller can retrieve these integer
	 * arrays and use them as part of the rendering of the map.
	 */
	private void GetLayerTypes(MapComponent map, TiledMap theTiledMap) {
		int layerCount = theTiledMap.getLayers().getCount();
		Array<Integer> aBackground = new Array<Integer>();
		Array<Integer> aForeground = new Array<Integer>();
		
		for(int iLayer=0; iLayer<layerCount; iLayer++) {
			MapLayer layer = theTiledMap.getLayers().get(iLayer);
			// Is foreground property set to "true" for this layer?
			if("true".equalsIgnoreCase(layer.getProperties().get("foreground", String.class))) {
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
