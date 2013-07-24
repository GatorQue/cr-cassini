package com.cosmicrover.cassini.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class CameraComponent extends AbstractComponent {
	/// Camera view in world coordinates
	private OrthographicCamera cameraHud = null;
	private OrthographicCamera cameraWorld = null;

	/**
	 * Default constructor does not create an OrthographicCamera object
	 */
	public CameraComponent() {
		cameraHud = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cameraWorld = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}
	
	/**
	 * Returns the world Orthographic camera object provided either at
	 * construction time or by calling the setCamera method.
	 * @return the Orthographic camera object or null if not available
	 */
	public OrthographicCamera getHudCamera() {
		return cameraHud;
	}
	
	/**
	 * Returns the world Orthographic camera object provided either at
	 * construction time or by calling the setCamera method.
	 * @return the Orthographic camera object or null if not available
	 */
	public OrthographicCamera getWorldCamera() {
		return cameraWorld;
	}
	
	/**
	 * Determine if the world camera object has been set for this entity.
	 * @return true if has been set, false otherwise
	 */
	public boolean hasHudCamera() {
		return cameraHud != null;
	}

	/**
	 * Determine if the world camera object has been set for this entity.
	 * @return true if has been set, false otherwise
	 */
	public boolean hasWorldCamera() {
		return cameraWorld != null;
	}

	/**
	 * Sets the hud Orthographic camera object provided.
	 * 
	 * @param theCamera object to set
	 */
	public void setHudCamera(OrthographicCamera theCamera) {
		// Keep track of the orthographic camera provided
		this.cameraHud = theCamera;
	}

	/**
	 * Sets the world Orthographic camera object provided.
	 * 
	 * @param theCamera object to set
	 */
	public void setWorldCamera(OrthographicCamera theCamera) {
		// Keep track of the orthographic camera provided
		this.cameraWorld = theCamera;
	}

	public void setWorldPosition(float x, float y) {
		cameraWorld.position.set(x, y, 0);
		cameraWorld.update();
		cameraWorld.apply(Gdx.gl10);
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeValue("world.x", cameraWorld.position.x);
    	json.writeValue("world.y", cameraWorld.position.y);
    	json.writeValue("world.z", cameraWorld.position.z);
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		cameraWorld.position.x = json.readValue("world.x", Float.class, jsonData);
		cameraWorld.position.y = json.readValue("world.y", Float.class, jsonData);
		cameraWorld.position.z = json.readValue("world.z", Float.class, jsonData);
	}
}
