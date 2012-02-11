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

	/** 自由に装飾を設定できるExtensionマップを返す */
	ExtensionMap customExtension();
	
	/** 自由に描画を行う */
	void customRender(Graphics2D g, ScreenTransform t, int width, int height);

	/**
	 * メソッド呼び出しの文字列を返す．<br>
	 * コマンドの送信に関わるメソッドをオーバーライドすることにより，StringBuilderに文字列を追加し，
	 * その結果を返してください．
	 */
	String getCommandsCall();
}
