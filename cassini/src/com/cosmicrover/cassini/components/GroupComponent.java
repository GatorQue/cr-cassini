package com.cosmicrover.cassini.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class GroupComponent extends AbstractComponent {
	private Array<String> groups;
	
	public GroupComponent() {
		groups = new Array<String>();
	}
	
	public String[] getGroups() {
		return groups.toArray(String.class);
	}
	
	public void add(String group) {
		groups.add(group);
	}
	
	public void remove(String group) {
		groups.removeValue(group,  false);
	}
	
	public boolean isInGroup(String group) {
		return groups.contains(group, false);
	}
	
	public boolean isInAnyGroup() {
		return groups.size > 0;
	}

	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeValue("groups", groups.toArray(String.class));
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		String[] jsonGroups = json.readValue("groups", String[].class, String.class, jsonData);
		for(String group : jsonGroups) {
			groups.add(group);
		}
	}
}
