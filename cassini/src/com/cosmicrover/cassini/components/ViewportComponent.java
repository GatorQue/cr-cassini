package com.cosmicrover.cassini.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class ViewportComponent extends AbstractComponent {
	public int x;
	public int y;
	public int width;
	public int height;
	
	public ViewportComponent() {
		x = 0;
		y = 0;
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
	}
	
	public ViewportComponent(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean intersects(int screenX, int screenY) {
		boolean anResult = false;

		if(screenX >= x && screenX < x + width &&
		   screenY >= Gdx.graphics.getHeight() - (y + height) && screenY < Gdx.graphics.getHeight() - y) {
			anResult = true;
		}
		
		return anResult;
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		// Default implementation is to do nothing
	}
}
