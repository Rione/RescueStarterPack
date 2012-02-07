package rione.agent;

import java.awt.Graphics2D;
import java.util.Set;

import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;
import rione.viewer.component.extension.ExtensionMap;


/**
 * デバッガが各エージェントを統一的に処理するためのインターフェイス
 * 要するに密告用
 * @author utisam
 *
 */
public interface DebugAgent {

	/** そのAgentのWorld */
	StandardWorldModel getWorld();

	/** そのAgentのID */
	EntityID getID();

	/** 視界内のEntityID */
	Set<EntityID> getVisibleEntities();

	/** 自由に設定できるExtensionマップ */
	ExtensionMap customExtension();
	
	void customRender(Graphics2D g, ScreenTransform t, int width, int height);

	String getCommandsCall();
}
