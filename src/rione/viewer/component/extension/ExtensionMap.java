package rione.viewer.component.extension;

import java.util.Collection;
import java.util.HashMap;

import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * EntityIDにEntityExtensionを対応付けます．
 * 
 * @author utisam
 *
 */
@SuppressWarnings("serial")
public class ExtensionMap extends HashMap<EntityID, EntityExtension> {
	
	/**
	 * idに割り当てられた EntityExtension を取得する．
	 * MapのgetがObjectになっているのでバグ対策．
	 * @param id
	 * @return idに割り当てられた EntityExtension
	 */
	public EntityExtension get(EntityID id) {
		return super.get(id);
	}
	/**
	 * StandardEntity に割り当てられた EntityExtension を取得する．
	 * MapのgetがObjectになっているのでバグ対策．
	 * @param se
	 * @return idに割り当てられた EntityExtension
	 */
	public EntityExtension get(StandardEntity se) {
		return super.get(se.getID());
	}
	
	/**
	 * StandardEntity に EntityExtension を割り当てます．
	 * @param se
	 * @param v
	 * @return 関連付けられていた過去の値
	 */
	public EntityExtension put(StandardEntity se, EntityExtension v) {
		return super.put(se.getID(), v);
	}
	
	/**
	 * entities全てにextensionを設定します．
	 * @param entities
	 * @param extension
	 */
	public void putExtensionToAll(Collection<? extends StandardEntity> entities,
			EntityExtension extension) {
		if (entities == null) return;
		for (StandardEntity se : entities) {
			this.put(se.getID(), extension);
		}
	}
	
	/**
	 * entities全てに装飾を追加します．
	 * @param entities
	 * @param dFunc 装飾生成関数
	 */
	public void putExtensionToAll(Collection<? extends StandardEntity> entities,
			DecoratorFunction dFunc) {
		if (entities == null) return;
		for (StandardEntity se : entities) {
			this.put(se.getID(), dFunc.create(this.get(se.getID())));
		}
	}
	
	public void putExtensionToAllID(Collection<EntityID> ids,
			EntityExtension extension) {
		if (ids == null) return;
		for (EntityID id : ids) {
			this.put(id, extension);
		}
	}
	
	public void putExtensionToAllID(Collection<EntityID> ids,
			DecoratorFunction dFunc) {
		if (ids == null) return;
		for (EntityID id : ids) {
			this.put(id, dFunc.create(this.get(id)));
		}
	}
	
}
