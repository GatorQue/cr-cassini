package com.cosmicrover.cassini.components;

import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.cosmicrover.core.components.AbstractComponent;

public class OwnerComponent extends AbstractComponent {
	public String myUuid;
	public String ownerUuid;
	public String groupPrefix;
	public String myOldUuid;
	public String ownerOldUuid;
	
	@Override
	public void write(Json json) {
		json.writeObjectStart(this.getClass().getName(), this.getClass(), this.getClass());
    	json.writeValue("my.uuid", myUuid);
    	json.writeValue("owner.uuid", ownerUuid);
    	json.writeValue("groupPrefix", groupPrefix);
    	json.writeObjectEnd();
	}

	@Override
	public void read(Json json, JsonValue jsonData) {
		myUuid = json.readValue("my.uuid", String.class, jsonData);
		ownerUuid = json.readValue("owner.uuid", String.class, jsonData);
    	groupPrefix = json.readValue("groupPrefix", String.class, jsonData);
	}

	@Override
	public void changeUuids(ArrayMap<String, String> uuidMap) {
		// Does the map have my old UUID value? then get my new UUID value
		if(uuidMap.containsKey(myUuid)) {
			// Keep track of the old UUID for reference
			myOldUuid = myUuid;
			myUuid = uuidMap.get(myUuid);
		}
		// Does the map have my old owner UUID value? then get my new owner UUID value
		if(uuidMap.containsKey(ownerUuid)) {
			// Keep track of the old UUID for reference
			ownerOldUuid = ownerUuid;
			ownerUuid = uuidMap.get(ownerUuid);
		}
	}
}
