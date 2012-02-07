package rione.viewer.component.extension;

import java.util.Collection;
import java.util.HashMap;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

@SuppressWarnings("serial")
public class ExtensionMap extends HashMap<EntityID, EntityExtension> {
	
	public void putExtensionToAll(Collection<? extends StandardEntity> entities,
			EntityExtension extension) {
		for (StandardEntity se : entities) {
			this.put(se.getID(), extension);
		}
	}
	
	public void putExtensionToAll(Collection<? extends StandardEntity> entities,
			DecoratorFunction dFunc) {
		for (StandardEntity se : entities) {
			this.put(se.getID(), dFunc.create(this.get(se.getID())));
		}
	}
	
	public void putExtensionToAllID(Collection<EntityID> ids,
			EntityExtension extension) {
		for (EntityID id : ids) {
			this.put(id, extension);
		}
	}
	
	public void putExtensionToAllID(Collection<EntityID> ids,
			DecoratorFunction dFunc) {
		for (EntityID id : ids) {
			this.put(id, dFunc.create(this.get(id)));
		}
	}
	
}
