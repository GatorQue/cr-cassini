package com.cosmicrover.cassini.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class WidgetComponent extends AbstractComponent {
	public enum WidgetType {
		Button,
		Label,
		TextBox,
	};
	
	public WidgetType type = WidgetType.Button;
	
	public WidgetComponent() {
	}
	
	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
		json.writeValue("type", type);
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		type = json.readValue("type", WidgetType.class, jsonData);
	}
}
