package com.cosmicrover.cassini.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class SpriteComponent extends AbstractComponent {
	/// Defines the TextureRegion to use for this sprite
	public TextureRegion textureRegion = null;
	
	/// Defines the tint color to use when drawing this sprite
	public Color tint = null;

	/// Default constructor
	public SpriteComponent() {
		tint = new Color(1f,1f,1f,1f);
	}
	
	/// Constructor for provided TextureRegion
	public SpriteComponent(TextureRegion textureRegion) {
		this();
		this.textureRegion = textureRegion;
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeValue("tint.r", tint.r);
    	json.writeValue("tint.g", tint.g);
    	json.writeValue("tint.b", tint.b);
    	json.writeValue("tint.a", tint.a);
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		tint.r = json.readValue("tint.r", Float.class, jsonData);
		tint.g = json.readValue("tint.g", Float.class, jsonData);
		tint.b = json.readValue("tint.b", Float.class, jsonData);
		tint.a = json.readValue("tint.a", Float.class, jsonData);
	}
}
