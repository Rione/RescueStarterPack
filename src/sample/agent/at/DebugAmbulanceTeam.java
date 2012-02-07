package sample.agent.at;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rescuecore2.messages.Command;
import rescuecore2.misc.gui.ScreenTransform;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rione.agent.DebugAgent;
import rione.viewer.component.AdvancedViewComponent;
import rione.viewer.component.EntityExtension;

public class DebugAmbulanceTeam extends SampleAmbulanceTeam implements DebugAgent {

	private ChangeSet changed = null;
	
	/** 最後に呼び出されたメソッドを表す文字列 */
	private StringBuffer lastCommand = null;

	@Override
	public StandardWorldModel getWorld() {
		return model;
	}
	
	@Override
	public void postConnect() {
		super.postConnect();
		//ビューアに登録
		AdvancedViewComponent.addTeamAgent(this);
	}
	
	@Override
	protected void think(int time, ChangeSet changed, Collection<Command> heard) {
		this.changed = changed;
		lastCommand = new StringBuffer("think(");
		lastCommand.append(time);
		lastCommand.append(')');
		super.think(time, changed, heard);
	}

	@Override
	public Set<EntityID> getVisibleEntity() {
		if (changed != null) {
			return changed.getChangedEntities();
		}
		return null;
	}

	@Override
	public Map<EntityID, EntityExtension> customExtension() {
		// TODO Map<EntityID, EntityExtension> を生成してください
		return null;
	}

	@Override
	public void customRender(Graphics2D g, ScreenTransform t, int width,
			int height) {
		// TODO gに対して描画を行ってください
	}

	@Override
	public String getCommandsCall() {
		return lastCommand.toString();
	}
	
	@Override
	public void sendMove(int time, List<EntityID> path) {
		lastCommand.append("->sendMove");
		super.sendMove(time, path);
	}
	@Override
	public void sendLoad(int time, EntityID target) {
		lastCommand.append("->sendLoad");
		super.sendLoad(time, target);
	}
	@Override
	public void sendUnload(int time) {
		lastCommand.append("->sendLoad");
		super.sendUnload(time);
	}
	@Override
	public void sendRescue(int time, EntityID target) {
		lastCommand.append("->sendRescue");
		super.sendRescue(time, target);
	}

}
