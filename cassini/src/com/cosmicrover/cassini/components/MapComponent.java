package com.cosmicrover.cassini.components;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class MapComponent extends AbstractComponent {
	/// Current level name
	public String levelFilename;
	
	/// Current map name
	public String mapFilename;
	
	/// Current maps width and height
	public int mapWidth = 0;
	public int mapHeight = 0;
	
	/// Current maps tile width and height
	public int mapTileWidth = 32;
	public int mapTileHeight = 32;
	
	/// List of background layers (first layer by default)
	public int[] mapBackgroundLayers = null;

	/// List of foreground layers
	public int[] mapForegroundLayers = null;
	
	/// List of maps previously loaded
	public final Array<String> mapsLoaded;
	
	/// Store our TiledMap address in this component
	public TiledMap tiledMap = null;

	public MapComponent() {
		mapsLoaded = new Array<String>();
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeValue("levelFilename", levelFilename);
    	json.writeValue("mapFilename", mapFilename);
    	json.writeValue("mapsLoaded", mapsLoaded.toArray(String.class));
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		levelFilename = json.readValue("levelFilename", String.class, jsonData);
		mapFilename = json.readValue("mapFilename", String.class, jsonData);
		String[] jsonMaps = json.readValue("mapsLoaded", String[].class, String.class, jsonData);
		for(String map : jsonMaps) {
			mapsLoaded.add(map);
		}
	}
}
