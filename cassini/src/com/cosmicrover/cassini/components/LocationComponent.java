package com.cosmicrover.cassini.components;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class LocationComponent extends AbstractComponent {
	private final Vector2   map;
	private final Rectangle mapBounds;
	private String          mapName;
	private final Vector3   level;
	private final Rectangle levelBounds;
	private final Vector2   levelGrid;
	private String          levelName;
	private final Vector2   levelOffsetCenter;
	
	public LocationComponent() {
		map = new Vector2();
		level = new Vector3();
		mapBounds = new Rectangle();
		levelBounds = new Rectangle();
		levelGrid = new Vector2(1,1);
		levelOffsetCenter = new Vector2(0.5f,0.5f);
	}
	
	public LocationComponent(LocationComponent clone, int x, int y) {
		this();
		// This order matters, don't rearrange it
		setMapName(clone.getMapName());
		setMapBounds(clone.getMapBounds());
		setMap(x, y);
		setLevelName(clone.getLevelName());
		setLevelGrid(clone.getLevelGrid());
	}
	
	public String getContextTag() {
		return levelName + mapName;
	}

	public Vector2 getMap() {
		return map;
	}
	
	public int getMapX() {
		return (int)map.x;
	}
	
	public int getMapY() {
		return (int)map.y;
	}
	
	public Vector3 getMapAsLevel(Vector2 map) {
		Vector2 mapLimit = getMapLimit(map);
		return getMapAsLevel((int)mapLimit.x, (int)mapLimit.y);
	}
	
	public Vector3 getMapAsLevel(int x, int y) {
		return new Vector3(x*levelGrid.x, y*levelGrid.y, 0);
	}

	public Vector2 getMapLimit(Vector2 map) {
		return getMapLimit(map.x, map.y);
	}
	
	public Vector2 getMapLimit(float x, float y) {
		return new Vector2(
				Math.min(mapBounds.width, Math.max(mapBounds.x, x)),
				Math.min(mapBounds.height, Math.max(mapBounds.y, y)) );
	}
	
	public Vector2 getLevelAsMap(Vector3 level) {
		Vector3 levelLimit = getLevelLimit(level);
		return getLevelAsMap(levelLimit.x, levelLimit.y);
	}
	
	public Vector2 getLevelAsMap(float x, float y) {
		return new Vector2((int)(x / levelGrid.x), (int)(y / levelGrid.y));
	}

	public Vector3 getLevelLimit(Vector3 level) {
		return getLevelLimit(level.x, level.y, level.z);
	}
	
	public Vector3 getLevelLimit(float x, float y, float z) {
		return new Vector3(
				Math.min(levelBounds.width, Math.max(levelBounds.x, x)),
				Math.min(levelBounds.height, Math.max(levelBounds.y, y)),
				z);
	}

	public Rectangle getMapBounds() {
		return mapBounds;
	}
	
	public String getMapName() {
		return mapName;
	}

	public Vector3 getLevel() {
		return level;
	}
	
	public Rectangle getLevelBounds() {
		return levelBounds;
	}

	public Vector2 getLevelGrid() {
		return levelGrid;
	}

	public String getLevelName() {
		return levelName;
	}
	
	public Vector2 getLevelOffsetCenter() {
		return levelOffsetCenter;
	}
	
	public void addMap(Vector2 map) {
		addMap((int)map.x, (int)map.y);
	}
	
	public void addMap(int x, int y) {
		setMap((int)map.x+x, (int)map.y+y);
	}

	public void setMap(Vector2 map) {
		setMap((int)map.x, (int)map.y);
    }

	public void setMap(int x, int y) {
		this.map.set(
			Math.min(mapBounds.width, Math.max(mapBounds.x, x)),
			Math.min(mapBounds.height, Math.max(mapBounds.y, y)) );
		// Update our level with the new map coordinates
		updateLevel();
	}
			  
	public void setMapBounds(int x, int y, int width, int height) {
		this.mapBounds.set(x,y,width,height);
		// Update our level bounds
		updateLevelBounds();
	}

	public void setMapBounds(Rectangle mapBounds) {
		this.mapBounds.set(mapBounds);
		// Update our level bounds
		updateLevelBounds();
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public void addLevel(Vector2 level) {
		addLevel(level.x, level.y, 0);
	}
	
	public void addLevel(Vector3 level) {
		addLevel(level.x, level.y, level.z);
	}
	
	public void addLevel(float x, float y) {
		addLevel(x, y, 0);
	}
	
	public void addLevel(float x, float y, float z) {
		setLevel(level.x+x, level.y+y, level.z+z);
	}

	public void setLevel(Vector2 level) {
		setLevel(level.x, level.y, 0);
	}

	public void setLevel(Vector3 level) {
		setLevel(level.x, level.y, level.z);
	}

	public void setLevel(float x, float y) {
		setLevel(x, y, 0);
	}
	
	public void setLevel(float x, float y, float z) {
		this.level.set(
				Math.min(levelBounds.width, Math.max(levelBounds.x, x)),
				Math.min(levelBounds.height, Math.max(levelBounds.y, y)),
				z);
		// Update map with the new level coordinates
		updateMap();
	}

	public void setLevelGrid(Vector2 grid) {
		setLevelGrid(grid.x, grid.y);
	}

	public void setLevelGrid(float x, float y) {
		if(x > 0.0f && y > 0.0f) {
			this.levelGrid.set(x,y);
			// Update our level bounds
			updateLevelBounds();
			// Update our level
			updateLevel();
			// Update our level offset to the center
			updateLevelOffsetCenter();
		}
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}
	
	private void updateMap() {
		this.map.set(level.x / levelGrid.x, level.y / levelGrid.y);
	}

	private void updateLevel() {
		this.level.set(map.x*levelGrid.x,map.y*levelGrid.y, 0);
	}
	
	private void updateLevelBounds() {
		this.levelBounds.set(
				mapBounds.x*levelGrid.x,          // Update minimum level x value
				mapBounds.y*levelGrid.y,          // Update minimum level y value
				mapBounds.width*levelGrid.x-1,    // Update maximum level width value (1 pixel short)
				mapBounds.height*levelGrid.y-1);  // Update maximum level height value (1 pixel short)
	}
	
	private void updateLevelOffsetCenter() {
		levelOffsetCenter.set(levelGrid.x*0.5f, levelGrid.y*0.5f);
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeValue("map.x", (int)map.x);
    	json.writeValue("map.y", (int)map.y);
    	json.writeValue("mapName", mapName);
    	json.writeValue("level.x", level.x);
    	json.writeValue("level.y", level.y);
    	json.writeValue("level.z", level.z);
    	json.writeValue("levelName", levelName);
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		map.x = json.readValue("map.x", Integer.class, jsonData);
		map.y = json.readValue("map.y", Integer.class, jsonData);
		mapName = json.readValue("mapName", String.class, jsonData);
		level.x = json.readValue("level.x", Float.class, jsonData);
		level.y = json.readValue("level.y", Float.class, jsonData);
		level.z = json.readValue("level.z", Float.class, jsonData);
		levelName = json.readValue("levelName", String.class, jsonData);
	}
}
